# 🦆 Ducky Mail Alias Manager

A simple Android app for managing your **DuckDuckGo Email Protection aliases** in one place.

## 💡 Why I Built This

There are several email alias services available, but many useful features require a paid plan.

**DuckDuckGo Email Protection provides private `@duck.com` aliases for free**, but managing multiple generated aliases can become difficult.

I built **Ducky Mail Alias Manager** to make those aliases easier to view, organize, and manage from an Android device.

## ✨ Features

- View your DuckDuckGo email aliases
- Create new aliases
- Manage existing aliases
- Keep your aliases organized
- Simple Android interface
- No paid alias service required

## 🔑 Getting Your DuckDuckGo Bearer Token

The app requires your DuckDuckGo Email Protection **Bearer Token** to access your aliases.

### Using a Desktop Browser

1. Sign in to **DuckDuckGo Email Protection** in your browser.
2. Open Developer Tools:
   - Chrome / Edge: `F12` or `Ctrl + Shift + I`
   - Firefox: `F12`
3. Open the **Network** tab.
4. Refresh the DuckDuckGo Email Protection page.
5. Look for a request made to the DuckDuckGo Email Protection API.
6. Open the request and check **Request Headers**.
7. Find:

```text
Authorization: Bearer YOUR_TOKEN
```

8. Copy **only the token after `Bearer`**.
9. Open Ducky Mail Alias Manager and paste it into the Bearer Token field.

> ⚠️ **Keep your Bearer Token private.** Anyone with the token may be able to access or manage your DuckDuckGo Email Protection data. Never post it in GitHub issues, screenshots, logs, or messages.

## 📱 How to Use

1. Download the latest APK from **GitHub Releases**.
2. Install the APK on your Android device.
3. Open **Ducky Mail Alias Manager**.
4. Enter your DuckDuckGo Bearer Token.
5. Start viewing and managing your aliases.

## ⚠️ Disclaimer

This is an **unofficial, independent project** and is not affiliated with, endorsed by, or maintained by DuckDuckGo.

DuckDuckGo may change its internal API or authentication system at any time, which could temporarily or permanently break functionality.

## 🔐 Privacy

Your Bearer Token is sensitive authentication information. Treat it like a password and never share it with anyone.
