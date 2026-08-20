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

`TransferSession` serializes concurrent transition attempts and exposes state through a read-only `StateFlow`. It is not yet coupled to `ProtocolSender` or `ProtocolReceiver`: the current sender has no acknowledgement proving that the remote receiver verified the payload. That integration requires completion/error protocol messages in a later milestone.
