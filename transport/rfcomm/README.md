# :transport:rfcomm

Hand-rolled classic Bluetooth (BR/EDR) transport. No Play Services, no BLE.

## Classes

| Class                           | Role                                                        |
|---------------------------------|-------------------------------------------------------------|
| `RfcommBroadcastServer`         | SDP service record, accepts RFCOMM clients, broadcasts      |
| `RfcommBroadcastClient`         | Connects to server, receives messages, sends heartbeats     |
| `RfcommServerDiscovery`         | Finds bonded devices exposing the service UUID              |
| `RfcommTransportRequirements`   | Permissions, required radio, adapter presence               |
| `MessageFramer`                 | Frame encode/decode                                         |

## Protocol

* Service: SDP record `Broadcast Service`, UUID `2f58e6c0-5ccf-4d2f-afec-65a2d98e2141`
* Server: `listenUsingRfcommWithServiceRecord`; one `accept()` loop, one reader per client
* Client: `createRfcommSocketToServiceRecord(uuid).connect()`
* Server → client: UTF-8 message + `0x04` terminator; messages must not contain U+0004
* Client → server: `0x00` heartbeat every 1 s
* Disconnect detection:
  * Client: failed heartbeat write or read EOF/`IOException`
  * Server: read EOF/`IOException`
  * `BluetoothSocket.isConnected` reflects local state only; not used

## Discovery

1. `bondedDevices` → `fetchUuidsWithSdp()` for each, every 10 s while collected
2. `ACTION_UUID` receiver keeps devices whose `EXTRA_UUID` contains the service UUID
3. Null `EXTRA_UUID` (SDP timeout) removes the device

## Quirks

| Quirk                                                                                         | Handling                                                    | Source |
|-----------------------------------------------------------------------------------------------|-------------------------------------------------------------|--------|
| SDP on API 23–27 reports 128-bit UUIDs byte-reversed                                          | Match UUID or its byte-reversed form                        | [issuetracker 37075233](https://issuetracker.google.com/issues/37075233) |
| `fetchUuidsWithSdp` falls back to cached UUIDs after ~6 s timeout; unreachable devices listed | Results are candidates; failed `connect()` → `ConnectionFailed` | [BluetoothDevice](https://developer.android.com/reference/android/bluetooth/BluetoothDevice#fetchUuidsWithSdp()) |
| `ACTION_UUID` sent by the Bluetooth app, not system UID; `RECEIVER_NOT_EXPORTED` misses it     | Registered with `RECEIVER_EXPORTED` (protected broadcast)   | [Broadcasts](https://developer.android.com/develop/background-work/background-tasks/broadcasts) |
| API ≤ 30 delivers `ACTION_UUID` only to `BLUETOOTH_ADMIN` holders                             | `BLUETOOTH_ADMIN` declared (`maxSdkVersion=30`)             | AOSP `RemoteDevices.java` |
| Blocking `accept()`/`read()`/`connect()` ignore coroutine cancellation and interrupts         | `useCancellable` closes the socket on cancellation          | [Connect devices](https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices) |
| Guide recommends `cancelDiscovery()` before `connect()`; requires `BLUETOOTH_SCAN`            | Not called: keeps runtime permissions to `BLUETOOTH_CONNECT`; connect may be slow during another app's inquiry | [Connect devices](https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices) |
| Piconet limit: ~4–7 simultaneous clients in practice                                          | Excess clients get `ConnectionFailed`                        | Bluetooth Core spec |
| Server need not be discoverable for bonded clients                                            | No `ACTION_REQUEST_DISCOVERABLE`, no `BLUETOOTH_ADVERTISE`  | [Find devices](https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices) |

## Permissions

* API ≤ 30: `BLUETOOTH`, `BLUETOOTH_ADMIN` (install-time)
* API ≥ 31: `BLUETOOTH_CONNECT` (runtime)
* No location: no inquiry or BLE scan ([Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions))
