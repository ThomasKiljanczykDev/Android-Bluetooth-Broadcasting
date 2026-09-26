# :transport:ble

Connectionless BLE transport. Server advertises the current message; clients scan. No pairing, no connection, no Play Services.

## Classes

| Class                      | Role                                                        |
|----------------------------|-------------------------------------------------------------|
| `BleBroadcastServer`       | Extended advertising set; message → advertising data        |
| `BleListener`              | Scans all sessions; `servers` via `toHeardServers`           |
| `BleScanner`               | Scan → decoded `AdvertPayload` flow                          |
| `BleTransportRequirements` | Permissions, radios, extended advertising support           |
| `AdvertPayload`            | Payload encode/decode                                       |

## Protocol

* Advert: extended, non-connectable, non-scannable, `INTERVAL_LOW` (~100 ms), `TX_POWER_MEDIUM`, 1M PHY
* AD structure: service data, 128-bit UUID `2f58e6c0-5ccf-4d2f-afec-65a2d98e2141`
* Service data:

  | Field      | Bytes | Notes                                       |
  |------------|-------|---------------------------------------------|
  | version    | 1     | `1`; other versions ignored                 |
  | sessionId  | 4     | Random per `start()`                        |
  | seq        | 2     | `0` = no message; `1..65535`, wraps to `1`  |
  | nameLength | 1     |                                             |
  | name       | ≤ 24  | UTF-8, `Settings.Global.DEVICE_NAME` or model, cut at code point |
  | message    | rest  | UTF-8                                       |

* Advertising data budget: 200 bytes → message ≤ 174 − name bytes (150–174)
* `broadcast()`: `setAdvertisingData` with next `seq`; set not restarted, session and address unchanged
* Over-limit `broadcast()` dropped; `maxMessageBytes` public on `BleBroadcastServer`

## Listener

* Not a `BroadcastClient`: `start()`, `stop()`, `state` (`Stopped` / `Listening`), `servers`
* `servers`: one `HeardServer` per `sessionId`, first-heard order
  * `device.id`: `sessionId` as 8 hex digits; MAC not used
  * `device.name`: from payload; null if empty
  * `message`: current message; null while `seq` = 0
* Server dropped after 10 s without advert
* Scan cannot start (Bluetooth off): `Stopped`

## Quirks

| Quirk                                                                                   | Handling                                                        | Source |
|-----------------------------------------------------------------------------------------|-----------------------------------------------------------------|--------|
| Advertiser address is a resolvable private address, rotated ~15 min                     | Server identified by `sessionId` in payload                     | Bluetooth Core spec (LE privacy) |
| Legacy adverts: 31 bytes total                                                          | Extended advertising only; `minSdk` 26                          | [AdvertisingSetParameters](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetParameters) |
| Data > 1 PDU is chained (`AUX_CHAIN_IND`); receivers may report `DATA_TRUNCATED`         | 200-byte budget; non-`DATA_COMPLETE` results ignored            | [ScanResult](https://developer.android.com/reference/android/bluetooth/le/ScanResult#getDataStatus()) |
| Periodic advertising sync is not public API                                             | Not used                                                        | — |
| `isLeExtendedAdvertisingSupported()` is false while Bluetooth is off                    | Treated as available; unsupported hardware fails `start()`      | [BluetoothAdapter](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#isLeExtendedAdvertisingSupported()) |
| > 5 scan starts per 30 s per app: scan silently returns nothing                          | Not handled; one scan per **Listen**                              | AOSP `AppScanStats` |
| API ≤ 30: scan results need location permission and location services on               | `ACCESS_FINE_LOCATION`; `Radio.Location` required                | [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions) |
| `neverForLocation` filters beacon-format adverts                                        | Custom service data unaffected                                  | [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions) |
| Listeners are invisible to the advertiser                                               | `events` never emits; hint shown on server screen               | — |

## Permissions

* API ≤ 30: `BLUETOOTH`, `BLUETOOTH_ADMIN` (install-time), `ACCESS_FINE_LOCATION` (runtime)
* API ≥ 31: `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH_CONNECT` (for `ACTION_REQUEST_ENABLE`) (runtime)
