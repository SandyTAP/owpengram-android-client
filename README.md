<p align="center">
  <img src="media/readme/owpengram_splash.png" alt="OwpenGram" width="440">
</p>

# 🤖 OwpenGram for Android

**One familiar app. Any server you trust.**

OwpenGram for Android is a multi-server messenger built on a fast, familiar
experience. Use the official network, your own private server, or any community
node — each account independent, all in one app. Private by design, comfortable
to use, and free from lock-in.

> 🤖 **Available now for Android.** iOS and a web client are planned.

> 🔗 Built on **MTProto API layer 225**.

<p align="center">
  <img src="media/readme/android_hero.png" alt="OwpenGram for Android — chats and calls" width="820">
</p>

---

## ✨ Why you'll like it

- 🌐 **No server picking** — the app connects to XiroGram on its own, every time.
- 🏠 **Your own infrastructure** — XiroGram is self-hosted and fully under your control.
- 🧠 **Familiar & comfortable** — the experience you already know, no learning curve.
- 🔒 **Private** — talk on infrastructure you trust, away from the cloud.
- 🛡️ **Censorship-resistant** — your own server stays reachable when others are blocked.
- 🆓 **Open source** — read it, audit it, build it yourself.

## 🌐 Connecting

There is no server to choose. XiroGram is the app's only backend, so every
account slot is bound to it automatically as soon as you start signing in — first
launch, adding a second account, or logging back in after a sign-out. The MTProto
handshake happens in the background; an unreachable server never blocks the login
screen.

Add several accounts and they stay cleanly separated — different identities,
different data, one app.

<p align="center">
  <img src="media/readme/android_multiserver.png" alt="Accounts grouped by server" width="620">
</p>

The endpoint is built in (`150.241.85.49:2398`, MTProto port `2398`) with the
server's RSA public key, so no configuration is needed.

Run your own instance of the server:
👉 [owpengram-server](https://github.com/owpengram/owpengram-server)

## 🛠️ Build (Windows)

Run the interactive build script — double-click it or run from a terminal:

```bat
build-android.bat
```

It guides you through API credentials, the keystore, SDK setup and the Gradle
build, and remembers your answers in `.owpengram-build.local.json` (gitignored).

**Requirements:** JDK 17, Android SDK (API 35, build-tools 35.0.0,
NDK 21.4.7075529), Git.

## 📦 Part of the OwpenGram project

- 🚀 [Server](https://github.com/owpengram/owpengram-server)
- 💻 [Desktop client](https://github.com/owpengram/owpengram-desktop-client)
- 🌐 [GitHub organization](https://github.com/owpengram)

## 💬 Community

- 📢 Channel: [@owpengram](https://t.me/owpengram)
- 💬 Chat: [Join the discussion](https://t.me/+sVB6Ymv70jEwNTAy)

## 📄 License

Based on [Telegram for Android](https://github.com/DrKLO/Telegram) — licensed
under **GNU GPL v2 or later** ([LICENSE](LICENSE)).

---

⭐ If OwpenGram is useful to you, a star on GitHub helps a lot.
