# Android-Bluetooth-Broadcasting

## Overview

Android app broadcasting text messages from one server to many clients.

* Three transports in one app, one card each on the main screen:
  * **BLE advertising** — connectionless BLE extended advertising, see [transport/ble](transport/ble/README.md)
  * **Bluetooth** — hand-rolled classic Bluetooth RFCOMM, see [transport/rfcomm](transport/rfcomm/README.md)
  * **Nearby** — [Google Nearby Connections](https://developers.google.com/nearby/connections/overview)
* Kotlin, MVVM, Jetpack Compose
* Minimum Android 8.0 (API 26)

## Transports

|                  | BLE advertising                              | Bluetooth                                    | Nearby                                    |
|------------------|----------------------------------------------|----------------------------------------------|-------------------------------------------|
| Module           | `:transport:ble`                             | `:transport:rfcomm`                          | `:transport:nearby`                       |
| Play Services    | Not required                                 | Not required                                 | Required                                  |
| Pairing          | Not required                                 | Required (system Bluetooth settings)         | Not required                              |
| Radios           | Bluetooth; + Location on API ≤ 30            | Bluetooth                                    | Bluetooth + Wi-Fi                         |
| Discovery        | None; client lists all servers in range      | Bonded devices, SDP service UUID lookup      | Nearby advertising/discovery (P2P_STAR)   |
| Link             | None; message carried in advert              | RFCOMM socket                                | Nearby-managed (Bluetooth, BLE, Wi-Fi)    |
| Clients          | Unlimited; not reported to server            | ~4–7 (piconet limit)                         | Many                                      |
| Message size     | 150–174 bytes UTF-8                          | Unlimited                                    | Unlimited                                 |
| Hardware         | BLE extended advertising (Bluetooth 5)       | Classic Bluetooth                            | Any                                       |

## Building

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

* Single variant, application ID `dev.thomas_kiljanczyk.bluetoothbroadcasting`

## Architecture

```
:app ─┬─► :transport:ble ────┐
      ├─► :transport:rfcomm ─┼─► :transport:core
      └─► :transport:nearby ─┘
```

* `:transport:core` — interfaces, `RemoteDevice`, state/event types, `TransportRequirements`, `Radio`, fakes (`testFixtures`)
  * `BroadcastServer` — `start()`, `stop()`, `broadcast(String)`, `state`, `events`
  * `BroadcastClient` — `connect(RemoteDevice)`, `disconnect()`, `state`, `messages`
  * `ServerDiscovery` — `discover()`: cold `Flow`, discovery runs while collected
  * `TransportRequirements` — runtime permissions, required radios, availability check
* Transport modules: concrete `@ViewModelScoped` implementations, no interface bindings; manifest permissions merged into the app
  * ble: `BleBroadcastServer` implements `BroadcastServer`; `BleListener` is its own API (no `BroadcastClient`)
* `:app` — Compose UI; composes each transport from explicit pieces, no build-time or runtime switching:
  * `ui/server`, `ui/client` — shared screens and open base ViewModels (`ServerViewModel`, `ClientViewModel`, `PickDeviceDialogViewModel`)
  * `ui/ble`, `ui/rfcomm`, `ui/nearby` — per-transport `@HiltViewModel` subclasses injecting concrete classes, routes, `TransportEntry`, `NavGraphBuilder.xxxDestinations()`
  * `ui/ble/BleClientScreen` — BLE-only client screen
  * `ui/main` — `MainScreen` (cards), `TransportCard`, `TransportGate`
* Resources released when the owning ViewModel is cleared

## Usage

### Main screen

* One card per transport: title, subtitle, **Client**, **Server**
* Unavailable transport: buttons disabled, reason shown
* **Client** / **Server**: requests that transport's permissions, prompts for disabled radios, then opens the screen

### Server

1. **Start server**
2. Type message → **Send message**: broadcast to all clients; BLE: **Send** disabled over byte limit
3. **Stop server**: disconnects all clients; BLE: server leaves clients' lists after 10 s

### Client — Bluetooth, Nearby

1. Bluetooth only: pair client and server devices in system Bluetooth settings
2. **Connect to server** → pick server
3. Received messages are displayed
4. **Disconnect** ends the session

### Client — BLE advertising

1. **Listen**: no server pick, no connection
2. One card per server in range: name, current message; dropped after 10 s without adverts
3. **Stop listening**

## Permissions

Declared by each transport module, merged into the app manifest. Requested per transport on first **Client** / **Server** tap.

**BLE advertising**

| Permission                                                   | SDK range   | Type    |
|--------------------------------------------------------------|-------------|---------|
| `BLUETOOTH`, `BLUETOOTH_ADMIN`                               | ≤ API 30    | Install |
| `ACCESS_FINE_LOCATION`                                       | ≤ API 30    | Runtime |
| `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`                   | ≥ API 31    | Runtime |
| `BLUETOOTH_SCAN` (`neverForLocation`)                        | ≥ API 31    | Runtime |

**Bluetooth**

| Permission                        | SDK range | Type        |
|-----------------------------------|-----------|-------------|
| `BLUETOOTH`, `BLUETOOTH_ADMIN`    | ≤ API 30  | Install     |
| `BLUETOOTH_CONNECT`               | ≥ API 31  | Runtime     |

**Nearby** ([source](https://developers.google.com/nearby/connections/android/get-started))

| Permission                                                   | SDK range   | Type    |
|--------------------------------------------------------------|-------------|---------|
| `ACCESS_WIFI_STATE`                                          | all         | Install |
| `CHANGE_WIFI_STATE`                                          | ≤ API 31    | Install |
| `BLUETOOTH`, `BLUETOOTH_ADMIN`                               | ≤ API 30    | Install |
| `ACCESS_COARSE_LOCATION`                                     | ≤ API 28, 31 | Runtime |
| `ACCESS_FINE_LOCATION`                                       | API 29–31   | Runtime |
| `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` | ≥ API 31    | Runtime |
| `NEARBY_WIFI_DEVICES`                                        | ≥ API 33    | Runtime |
| `ACCESS_LOCAL_NETWORK`                                       | ≥ API 37    | Runtime |

Merged manifest:
* `ACCESS_FINE_LOCATION` `maxSdkVersion` 31 (widest; set in app manifest)
* `BLUETOOTH_SCAN` carries `neverForLocation` for all transports

## Libraries Used

* [Kotlin Coroutines / Flow](https://kotlinlang.org/docs/coroutines-overview.html)
* [Jetpack Compose](https://developer.android.com/jetpack/compose), [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
* [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* [Google Nearby Connections](https://developers.google.com/nearby/connections/overview) — Nearby transport

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
