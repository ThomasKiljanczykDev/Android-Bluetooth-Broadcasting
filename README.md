# Android-Bluetooth-Broadcasting

## Overview

Android app broadcasting text messages from one server to many clients.

* Two interchangeable transports, selected at build time:
  * **bluetooth** (default) — hand-rolled classic Bluetooth RFCOMM, see [transport/bluetooth](transport/bluetooth/README.md)
  * **nearby** — [Google Nearby Connections](https://developers.google.com/nearby/connections/overview)
* Kotlin, MVVM, Jetpack Compose
* Minimum Android 7.1 (API 25)

## Transports

|                  | bluetooth                                    | nearby                                    |
|------------------|----------------------------------------------|-------------------------------------------|
| Module           | `:transport:bluetooth`                       | `:transport:nearby`                       |
| Play Services    | Not required                                 | Required                                  |
| Pairing          | Required (system Bluetooth settings)         | Not required                              |
| Radios           | Bluetooth                                    | Bluetooth + Wi-Fi                         |
| Discovery        | Bonded devices, SDP service UUID lookup      | Nearby advertising/discovery (P2P_STAR)   |
| Link             | RFCOMM socket                                | Nearby-managed (Bluetooth, BLE, Wi-Fi)    |
| Clients          | ~4–7 (piconet limit)                         | Many                                      |
| Runtime perms    | `BLUETOOTH_CONNECT` (API 31+)                | See [Permissions](#permissions)           |

## Building

* Android Studio: **Build Variants** → `bluetoothDebug` / `nearbyDebug`
* CLI:
  ```
  ./gradlew assembleBluetoothDebug
  ./gradlew assembleNearbyDebug
  ./gradlew testBluetoothDebugUnitTest testNearbyDebugUnitTest :transport:bluetooth:testDebugUnitTest
  ```
* Application IDs: `….bluetooth`, `….nearby` — both install side by side.

## Architecture

```
:app ─────────────────────────────────────► :transport:core
 ├─ bluetoothImplementation ─► :transport:bluetooth ─► :transport:core
 └─ nearbyImplementation ────► :transport:nearby ────► :transport:core
```

* `:transport:core` — interfaces, `RemoteDevice`, state/event types, `TransportRequirements`, fakes (`testFixtures`)
  * `BroadcastServer` — `start()`, `stop()`, `broadcast(String)`, `state`, `events`
  * `BroadcastClient` — `connect(RemoteDevice)`, `disconnect()`, `state`, `messages`
  * `ServerDiscovery` — `discover()`: cold `Flow`, discovery runs while collected
  * `TransportRequirements` — runtime permissions, required radios, availability check
* Transport modules: implementations, Hilt bindings, manifest permissions (merged into the app)
* `:app` — Compose UI and ViewModels; depends on `:transport:core` only
* Implementations are `@ViewModelScoped`; resources released when the owning ViewModel is cleared

## Usage

### Server

1. **Server** → **Start server**
2. Type message → **Send message**: broadcast to all connected clients
3. **Stop server**: disconnects all clients

### Client

1. bluetooth only: pair client and server devices in system Bluetooth settings
2. **Client** → **Connect to server** → pick server
3. Received messages are displayed
4. **Disconnect** ends the session

## Permissions

Declared by the transport module; requested at app start.

**bluetooth**

| Permission                        | SDK range | Type        |
|-----------------------------------|-----------|-------------|
| `BLUETOOTH`, `BLUETOOTH_ADMIN`    | ≤ API 30  | Install     |
| `BLUETOOTH_CONNECT`               | ≥ API 31  | Runtime     |

**nearby** ([source](https://developers.google.com/nearby/connections/android/get-started))

| Permission                                                   | SDK range   | Type    |
|--------------------------------------------------------------|-------------|---------|
| `ACCESS_WIFI_STATE`                                          | all         | Install |
| `CHANGE_WIFI_STATE`                                          | ≤ API 31    | Install |
| `BLUETOOTH`, `BLUETOOTH_ADMIN`                               | ≤ API 30    | Install |
| `ACCESS_COARSE_LOCATION`                                     | ≤ API 28    | Runtime |
| `ACCESS_FINE_LOCATION`                                       | API 29–31   | Runtime |
| `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` | ≥ API 31    | Runtime |
| `NEARBY_WIFI_DEVICES`                                        | ≥ API 33    | Runtime |
| `ACCESS_LOCAL_NETWORK`                                       | ≥ API 37    | Runtime |

## Libraries Used

* [Kotlin Coroutines / Flow](https://kotlinlang.org/docs/coroutines-overview.html)
* [Jetpack Compose](https://developer.android.com/jetpack/compose), [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
* [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* [Google Nearby Connections](https://developers.google.com/nearby/connections/overview) — nearby flavor only

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
