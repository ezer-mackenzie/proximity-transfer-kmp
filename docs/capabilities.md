# Capabilities and negotiation

Status: experimental.

Each peer advertises the transports currently available at runtime. Availability may depend on platform, hardware, operating-system version, permissions, application state, and user consent.

| Code | Capability | Bootstrap | Data transfer |
|---:|---|:---:|:---:|
| 1 | `MEMORY` | Yes | Yes |
| 2 | `BLE` | Yes | Yes |
| 3 | `LOCAL_NETWORK` | No | Yes |
| 4 | `WIFI_AWARE` | No | Yes |
| 5 | `WIFI_DIRECT` | No | Yes |
| 6 | `MULTIPEER_CONNECTIVITY` | No | Yes |
| 7 | `NFC_BOOTSTRAP` | Yes | No |
| 8 | `QR_BOOTSTRAP` | Yes | No |

`MEMORY` exists for deterministic tests and local protocol development. Advertising a capability does not imply that another platform implements a compatible API; negotiation only selects capabilities present on both peers.

## Selection

`TransportNegotiator` intersects the local and remote sets. It independently selects:

1. a required data transport;
2. an optional bootstrap transport.

The default data preference is:

```text
LOCAL_NETWORK
WIFI_AWARE
WIFI_DIRECT
MULTIPEER_CONNECTIVITY
BLE
MEMORY
```

The default bootstrap preference is:

```text
NFC_BOOTSTRAP
QR_BOOTSTRAP
BLE
MEMORY
```

Applications may provide a different deterministic preference. If no common data transport exists, negotiation throws `NoCompatibleTransportException`; it never silently chooses a platform-exclusive capability.

`CapabilityExchange` sends each peer's encoded capabilities through an already established `Connection`, validates the remote `CAPABILITIES` frame, and applies this selection. Its `TransferSession` can then be passed to `ProtocolSender` or `ProtocolReceiver`, preserving the negotiated lifecycle through transfer completion.

This milestone exchanges and selects capability identifiers only. It does not open, switch, or configure a concrete radio transport.
