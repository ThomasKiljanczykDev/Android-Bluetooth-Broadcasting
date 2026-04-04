# Android-Bluetooth-Broadcasting

## Overview

Android app for broadcasting messages via Bluetooth using Google Nearby Connections API.

* Written in Kotlin with MVVM pattern
* UI built with Jetpack Compose
* Minimum Android 7.0 (API 25)

## Features

* **Server mode** — advertises a service and broadcasts text messages to all connected clients
* **Client mode** — discovers nearby servers, connects, and displays received messages

## How it works

The app uses
the [Google Nearby Connections API](https://developers.google.com/nearby/connections/overview) for
device discovery and communication over Bluetooth/Wi-Fi.

### Server

1. Launch the app and tap **Server**
2. Tap **Start server** — the device starts advertising
3. Type a message and tap **Send message** — it is broadcast to all connected clients
4. Tap **Stop server** to stop advertising and disconnect all clients

### Client

1. Launch the app and tap **Client**
2. Tap **Connect to server** — the device starts discovering nearby servers
3. Select a server from the list
4. Received messages are displayed on screen
5. Tap **Disconnect** to end the session

## Libraries Used

* [Kotlin Coroutines / Flow](https://kotlinlang.org/docs/coroutines-overview.html) — reactive state
  management with `StateFlow` and `SharedFlow`
* [Jetpack Compose](https://developer.android.com/jetpack/compose) — declarative UI toolkit
* [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) — in-app navigation
* [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) — UI state that
  survives configuration changes
* [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) — dependency
  injection
* [Google Nearby Connections](https://developers.google.com/nearby/connections/overview) —
  Bluetooth/Wi-Fi device discovery and communication

## Permissions

The app requests the following permissions at runtime depending on the Android version:

| Permission                                                   | SDK range |
|--------------------------------------------------------------|-----------|
| `BLUETOOTH`, `BLUETOOTH_ADMIN`                               | ≤ API 30  |
| `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`             | ≤ API 32  |
| `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` | ≥ API 31  |
| `NEARBY_WIFI_DEVICES`                                        | ≥ API 33  |

## Stay in touch

- Author - Tomasz Kiljańczyk
- Mail - [thomas.kiljanczyk.dev@gmail.com](mailto:thomas.kiljanczyk.dev@gmail.com)
- LinkedIn - [https://www.linkedin.com/in/thomas-kiljanczyk-dev/](www.linkedin.com/in/thomas-kiljanczyk-dev)

## License

Copyright (c) 2021 Tomasz Kiljańczyk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
