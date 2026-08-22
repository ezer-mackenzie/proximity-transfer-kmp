# Session state machine

Status: experimental.

A transfer session follows explicit, validated state transitions:

```text
IDLE
 ├─→ DISCOVERING → NEGOTIATING → CONNECTED
 ├─→ NEGOTIATING → CONNECTED    │
 └─→ CONNECTED                  │
                                      ↓
                                TRANSFERRING
                                      │
                                      ↓
                                  VERIFYING
                                      │
                                      ↓
                                  COMPLETED
```

The direct `IDLE → NEGOTIATING` path supports capability exchange over a bootstrap connection established outside the library. The direct `IDLE → CONNECTED` transition supports connections that do not require discovery or negotiation, including basic in-memory tests.

Every nonterminal state may transition to `FAILED`. Both `COMPLETED` and `FAILED` are terminal. Invalid transitions throw `InvalidSessionTransitionException` and do not modify the current state.

`TransferSession` serializes concurrent transition attempts and exposes state through a read-only `StateFlow`. `CapabilityExchange`, `ProtocolSender`, and `ProtocolReceiver` accept and expose a session so the same lifecycle can continue from negotiation into transfer. The sender enters `COMPLETED` only after receiving a `COMPLETE` frame containing the expected SHA-256 digest. A local validation error or remote `ERROR` moves the corresponding session to `FAILED`.
