# Session state machine

Status: experimental.

A transfer session follows explicit, validated state transitions:

```text
IDLE
 ├─→ DISCOVERING → NEGOTIATING → CONNECTED
 └─→ CONNECTED                     │
                                      ↓
                                TRANSFERRING
                                      │
                                      ↓
                                  VERIFYING
                                      │
                                      ↓
                                  COMPLETED
```

The direct `IDLE → CONNECTED` transition supports connections established outside discovery and negotiation, including the in-memory transport.

Every nonterminal state may transition to `FAILED`. Both `COMPLETED` and `FAILED` are terminal. Invalid transitions throw `InvalidSessionTransitionException` and do not modify the current state.

`TransferSession` serializes concurrent transition attempts and exposes state through a read-only `StateFlow`. `ProtocolSender` and `ProtocolReceiver` each expose their session. The sender enters `COMPLETED` only after receiving a `COMPLETE` frame containing the expected SHA-256 digest. A local validation error or remote `ERROR` moves the corresponding session to `FAILED`.
