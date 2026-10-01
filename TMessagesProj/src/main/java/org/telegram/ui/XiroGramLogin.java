package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import org.telegram.messenger.UserConfig;
import org.telegram.owpengram.OwpengramServer;
import org.telegram.owpengram.OwpengramServers;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Connects an account slot to XiroGram and opens the login screen.
 *
 * XiroGram is the app's only backend, so there is nothing to pick: the account is
 * bound to XiroGram and the user goes straight to the phone/email screen. The MTProto
 * handshake runs in the background — an unreachable server must never keep the user
 * from reaching the login screen (each login request shows its own spinner).
 *
 * Two entry points:
 *   - {@link #startLogin(BaseFragment, int)} for callers that are already on screen
 *     (intro, add-account, profile, settings…). Used directly; nothing is pushed.
 *   - {@link XiroGramLogin.Fragment} for LaunchActivity, which needs an actual
 *     BaseFragment to install as the root of an empty stack before one exists.
 */
public final class XiroGramLogin {

    /**
     * ActionBarLayout.presentFragment() silently refuses while a transition animation
     * is running, so a queued attempt can be dropped. Retry on a short timer until it
     * lands or the deadline passes — the layout force-completes any animation older
     * than 1.5s, so waiting longer than that is pointless.
     */
    private static final long RETRY_INTERVAL_MS = 100;
    private static final long RETRY_TIMEOUT_MS = 2000;

    private XiroGramLogin() {
    }

    /**
     * Binds the slot to XiroGram and presents the login screen over {@code from}.
     *
     * @param accountSlot account to log into, or -1 to use the calling fragment's own
     *                   account (the first-launch / intro flow)
     */
    public static void startLogin(BaseFragment from, int accountSlot) {
        if (from == null || from.isFinished) {
            return;
        }
        LoginActivity login = connectAndBuildLogin(accountSlot >= 0 ? accountSlot : from.getCurrentAccount(), accountSlot < 0);
        if (login != null) {
            // removeLast: the caller's row/button is not a screen worth going back to.
            // forceWithoutAnimation: appear exactly where the current screen is.
            from.presentFragment(login, true, true);
        }
    }

    /**
     * Applies the XiroGram server config to the account slot and builds the login
     * screen for it. Shared by both entry points above.
     *
     * @param targetAccount account slot to bind and log into
     * @param useSlotAccount true when {@code targetAccount} was passed explicitly as an
     *                       add-account slot, false when it is the current account
     * @return the login screen, or null if there is no usable account
     */
    static LoginActivity connectAndBuildLogin(int targetAccount, boolean useSlotAccount) {
        if (targetAccount < 0) {
            return null;
        }

        OwpengramServer server = OwpengramServers.xirogramServer();
        boolean alreadyOnXiroGram = OwpengramServers.ID_XIROGRAM.equals(
                OwpengramServers.getServerIdForAccount(targetAccount));
        OwpengramServers.setServerForAccount(server.id, targetAccount);
        // A slot that was bound to another backend (or was just logged out of) has to
        // redo the key handshake against the XiroGram endpoint; a slot already on
        // XiroGram keeps its keys and simply reconnects.
        OwpengramServers.applyServerToAccount(server, targetAccount, !alreadyOnXiroGram);
        ConnectionsManager.getInstance(targetAccount).resumeNetworkMaybe();

        // LoginActivity(n) sets newAccount=true and later calls switchToAccount(n),
        // which early-returns when n == selectedAccount, leaving an empty stack — so
        // only pass the slot when it differs from the currently selected account.
        LoginActivity login = (useSlotAccount && targetAccount != UserConfig.selectedAccount)
                ? new LoginActivity(targetAccount)
                : new LoginActivity();
        // Prime the cached email-signup flag so the right first view is built right
        // away; LoginActivity re-asks the server if the cache is missing.
        login.setEmailSignupEnabled(OwpengramServers.serverHasEmailSignup(targetAccount));
        return login;
    }

    /**
     * Root-stack entry point: LaunchActivity has nothing to present from yet, so the
     * work is deferred one loop iteration until the fragment is in its layout, then
     * retried until presentFragment() accepts it.
     */
    static void presentLoginFromRoot(BaseFragment from, int accountSlot) {
        if (from == null || from.isFinished || from.getParentLayout() == null) {
            return;
        }
        int targetAccount = accountSlot >= 0 ? accountSlot : from.getCurrentAccount();
        LoginActivity login = connectAndBuildLogin(targetAccount, accountSlot >= 0);
        if (login == null) {
            return;
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        final long deadline = SystemClock.elapsedRealtime() + RETRY_TIMEOUT_MS;
        final Runnable[] attempt = new Runnable[1];
        attempt[0] = () -> {
            if (from.isFinished || from.getParentLayout() == null) {
                return;
            }
            if (from.presentFragment(login, true, true)) {
                return;
            }
            // Refused — most likely a transition animation is still in flight, which
            // ActionBarLayout presents silently as a no-op. Try again shortly.
            if (SystemClock.elapsedRealtime() < deadline) {
                handler.postDelayed(attempt[0], RETRY_INTERVAL_MS);
            }
        };
        handler.post(attempt[0]);
    }

    /**
     * Headless fragment used only by LaunchActivity, which must hand ActionBarLayout
     * a real BaseFragment to install as the root of an empty navigation stack. It
     * binds XiroGram, swaps itself for the login screen, and draws nothing while it
     * waits (transparent, so the screen underneath stays visible).
     */
    public static class Fragment extends BaseFragment {

        private final int loginAccount;

        public Fragment() {
            this.loginAccount = -1;
        }

        public Fragment(int loginAccount) {
            this.loginAccount = loginAccount;
        }

        @Override
        public boolean onFragmentCreate() {
            // Nothing here on purpose: onFragmentCreate() runs before setParentLayout(),
            // and presentFragment() needs a parent layout to replace this fragment.
            return true;
        }

        @Override
        public View createView(Context context) {
            // Transparent and content-free: this fragment exists only to hand over to
            // LoginActivity, so no empty page should ever be visible.
            setHasOwnBackground(true);
            View view = new View(context);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            fragmentView = view;
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            // The fragment is now in the layout, so presentFragment() can replace it.
            presentLoginFromRoot(this, loginAccount);
        }
    }
}
