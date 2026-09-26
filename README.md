# Android-Bluetooth-Broadcasting

## Overview

Android app broadcasting text messages from one server to many clients.

* Three interchangeable transports, selected at build time:
  * **bluetooth** (default) — hand-rolled classic Bluetooth RFCOMM, see [transport/bluetooth](transport/bluetooth/README.md)
  * **nearby** — [Google Nearby Connections](https://developers.google.com/nearby/connections/overview)
  * **ble** — connectionless BLE extended advertising, see [transport/ble](transport/ble/README.md)
* Kotlin, MVVM, Jetpack Compose
* Minimum Android 7.1 (API 25); ble: Android 8.0 (API 26)

## Transports

|                  | bluetooth                                    | nearby                                    | ble                                          |
|------------------|----------------------------------------------|-------------------------------------------|----------------------------------------------|
| Module           | `:transport:bluetooth`                       | `:transport:nearby`                       | `:transport:ble`                             |
| Play Services    | Not required                                 | Required                                  | Not required                                 |
| Pairing          | Required (system Bluetooth settings)         | Not required                              | Not required                                 |
| Radios           | Bluetooth                                    | Bluetooth + Wi-Fi                         | Bluetooth; + Location on API ≤ 30            |
| Discovery        | Bonded devices, SDP service UUID lookup      | Nearby advertising/discovery (P2P_STAR)   | BLE scan for service data                    |
| Link             | RFCOMM socket                                | Nearby-managed (Bluetooth, BLE, Wi-Fi)    | None; message carried in advert              |
| Clients          | ~4–7 (piconet limit)                         | Many                                      | Unlimited; not reported to server            |
| Message size     | Unlimited                                    | Unlimited                                 | 150–174 bytes UTF-8                          |
| Hardware         | Classic Bluetooth                            | Any                                       | BLE extended advertising (Bluetooth 5)       |
| Runtime perms    | `BLUETOOTH_CONNECT` (API 31+)                | See [Permissions](#permissions)           | See [Permissions](#permissions)              |

## Building

* Android Studio: **Build Variants** → `bluetoothDebug` / `nearbyDebug` / `bleDebug`
* CLI:
  ```
  ./gradlew assembleBluetoothDebug
  ./gradlew assembleNearbyDebug
  ./gradlew assembleBleDebug
  ./gradlew testBluetoothDebugUnitTest testNearbyDebugUnitTest testBleDebugUnitTest \
      :transport:bluetooth:testDebugUnitTest :transport:ble:testDebugUnitTest
  ```
* Application IDs: `….bluetooth`, `….nearby`, `….ble` — all install side by side.

## Architecture

```
:app ─────────────────────────────────────► :transport:core
 ├─ bluetoothImplementation ─► :transport:bluetooth ─► :transport:core
 ├─ nearbyImplementation ────► :transport:nearby ────► :transport:core
 └─ bleImplementation ───────► :transport:ble ───────► :transport:core
```

* `:transport:core` — interfaces, `RemoteDevice`, state/event types, `TransportRequirements`, fakes (`testFixtures`)
  * `BroadcastServer` — `start()`, `stop()`, `broadcast(String)`, `state`, `events`, `maxMessageBytes`, `hint`
  * `BroadcastClient` — `connect(RemoteDevice)`, `disconnect()`, `state`, `messages`
  * `ServerDiscovery` — `discover()`: cold `Flow`, discovery runs while collected
  * `TransportRequirements` — runtime permissions, required radios, availability check
* Transport modules: implementations, Hilt bindings, manifest permissions (merged into the app)
* `:app` — Compose UI and ViewModels; depends on `:transport:core` only
* Implementations are `@ViewModelScoped`; resources released when the owning ViewModel is cleared

## Usage

### Server

1. **Server** → **Start server**
2. Type message → **Send message**: broadcast to all connected clients; ble: to all listening clients, **Send** disabled over byte limit
3. **Stop server**: disconnects all clients; ble: clients disconnect after 10 s

### Client

1. bluetooth only: pair client and server devices in system Bluetooth settings
2. **Client** → **Connect to server** → pick server
3. Received messages are displayed; ble: current message shown on connect
4. **Disconnect** ends the session; ble: also ends after 10 s without adverts

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

**ble**

| Permission                                                   | SDK range   | Type    |
|--------------------------------------------------------------|-------------|---------|
| `BLUETOOTH`, `BLUETOOTH_ADMIN`                               | ≤ API 30    | Install |
| `ACCESS_FINE_LOCATION`                                       | API 26–30   | Runtime |
| `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`                   | ≥ API 31    | Runtime |
| `BLUETOOTH_SCAN` (`neverForLocation`)                        | ≥ API 31    | Runtime |

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
