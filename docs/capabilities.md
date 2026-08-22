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

After local selection, peers exchange a `TRANSPORT_SELECTION` frame. Both the data and optional bootstrap choices must match exactly. Different preference configurations therefore fail with `NegotiationDisagreementException` instead of opening incompatible transports.

`CapabilityExchange` sends each peer's encoded capabilities through an already established `Connection`, validates the remote `CAPABILITIES` frame, and applies this selection. Its `TransferSession` can then be passed to `ProtocolSender` or `ProtocolReceiver`, preserving the negotiated lifecycle through transfer completion.

`DataTransportRegistry` binds data-capability identifiers to concrete `Transport` implementations available at runtime. Its advertised capabilities are derived from those registrations, and `open` fails explicitly with `MissingTransportImplementationException` if a negotiated implementation is unavailable.

`DataConnectionNegotiator` coordinates the next layer: it uses an existing control connection for capability exchange, resolves the selected implementation, opens a separate data connection, and preserves the same `TransferSession` for `ProtocolSender` or `ProtocolReceiver`.

The registry can open an already configured transport, but it does not configure a radio or establish a network. Those responsibilities belong to future platform adapters.
