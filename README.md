# Proximity Transfer

Proximity Transfer is an experimental Kotlin Multiplatform library for transferring arbitrary binary data directly between nearby devices. Its design is offline-first, transport-agnostic, and independent of application-specific business models.

The repository is currently in its foundation stage. It can negotiate compatible capabilities and transfer a chunked, SHA-256-verified binary payload through the in-memory transport. Hardware transports are not implemented yet.

The current release version is `0.4.0`.

## Goal

The intended developer experience is deliberately small:

```kotlin
transfer.send(payload)
```

Internally, the library will discover a peer, compare capabilities, select a mutually supported transport, transfer framed chunks, reconstruct the payload, and verify its SHA-256 digest.

```text
Application
    ↓
Common transfer protocol
    ↓
Capability negotiation
    ↓
Compatible platform transport
    ↓
Verified bytes
```

## Android and iPhone support

The protocol is multiplatform; radio APIs are not. Shared behavior belongs in Kotlin `commonMain`, while each platform supplies transport adapters around its public APIs.

| Capability | Android | iPhone | Intended role |
|---|---|---|---|
| Protocol, framing, chunking, integrity and negotiation | Yes | Yes | Shared Kotlin implementation |
| Memory transport | Yes | Yes | Deterministic development and tests |
| QR | Yes | Yes | Bootstrap and exchange of small session data |
| Bluetooth Low Energy | Yes | Yes | Cross-platform discovery, bootstrap, and potentially transfer |
| Standards-based local network | Yes | Yes | Cross-platform high-throughput candidate when a common network is available |
| NFC application-to-application flow | Platform APIs available | Restricted and not symmetric with Android | Optional bootstrap; never assumed universal |
| Wi-Fi Aware / Wi-Fi Direct | Android-specific APIs | No directly compatible public API | Android-only capability |
| Multipeer Connectivity | No | Yes | Apple-platform capability, not an Android fallback |

This matrix describes the architectural intent, not completed implementations.

### How unlike devices connect

Every peer advertises a capability set. The session negotiator computes their intersection and selects the best compatible route:

```text
Android: BLE, QR, Wi-Fi Aware, local network
iPhone:  BLE, QR, Multipeer, local network
                         ↓
Common:  BLE, QR, local network
                         ↓
Select a compatible bootstrap and data transport
```

A likely cross-platform flow is:

```text
QR or BLE bootstrap
        ↓
HELLO + CAPABILITIES
        ↓
BLE transfer, or establish/use a common local network
        ↓
DATA + VERIFY + COMPLETE
```

If the devices share no usable transport, the library reports a structured incompatibility instead of attempting a platform-exclusive protocol. Android may use Wi-Fi Aware with another compatible Android device; Apple devices may use Multipeer Connectivity with compatible Apple peers. Those optimizations remain behind the same transport abstraction.

Actual availability also depends on device hardware, operating-system version, permissions, application state, and user consent. Capability detection therefore happens at runtime.

## First milestone

The first release target is a hardware-independent proof:

```text
random bytes → sender → protocol → MemoryTransport → receiver → identical bytes
```

Automated tests must verify multiple payload sizes with:

```text
input.size == output.size
SHA-256(input) == SHA-256(output)
```

Only after this works reliably will the project add real proximity transports.

### Current progress

- [x] Versioned binary frame and codec.
- [x] Transport and connection abstractions.
- [x] Bidirectional memory transport.
- [x] Basic sender and receiver for one payload.
- [x] Chunking and reconstruction.
- [x] SHA-256 verification.
- [x] Session state machine.
- [x] Capability negotiation.
- [x] Capability exchange over an established connection.
- [x] Negotiated data-transport registry and resolution.
- [x] Control-to-data connection negotiation flow.
- [x] Receiver completion/error acknowledgement.
- [x] Connection interruption failure handling.
- [x] Observable payload-byte transfer progress.
- [x] Configurable payload and chunk resource limits.
- [x] QR code URI bootstrap codec (`proximity://v1?data=...`).
- [x] Binary NDEF NFC bootstrap codec (`NfcBootstrapCodec`).
- [x] Multi-item payload manifests (`MultiItemManifestCodec`).
- [x] Resumable transfers & missing chunk bitmap recovery (`ChunkBitmap`, `ResumeRequestCodec`).
- [x] Frame payload security & authenticated encryption (`FrameEncryptionCodec`, `HandshakeMessageCodec`).
- [x] High-level engine facade (`ProximityTransferEngine`).

## Planned architecture

- `protocol-core`: sessions, messages, frames, manifests, chunking, reconstruction, capabilities, negotiation, acknowledgements, and integrity.
- `transport-api`: binary connection contracts used by the protocol.
- `transport-memory`: deterministic transport and failure simulation for tests.
- Platform transports: created only after an implementation has meaningful behavior.
- `codec`: deterministic binary representation of protocol messages.
- `crypto`: established cryptographic implementations when session security is designed.

The exact Gradle module structure may evolve. Empty modules will not be created merely to reserve names.

For the MVP, these responsibilities remain in the single `library` Gradle module. Shared Kotlin code is grouped under `io.github.ezer_mackenzie.proximitytransfer.core`, with focused `protocol`, `transfer`, and `transport` subpackages. New Gradle modules will only be introduced when isolation provides a concrete build or distribution benefit.

The Kotlin and Android namespace uses `_` because hyphens are not valid Kotlin identifiers. The Gradle/Maven group retains the GitHub account spelling: `io.github.ezer-mackenzie`.

The experimental binary header is documented in [docs/wire-format.md](docs/wire-format.md).
The validated transfer lifecycle is documented in [docs/state-machine.md](docs/state-machine.md).
Runtime capabilities and deterministic selection are documented in [docs/capabilities.md](docs/capabilities.md).

## Non-goals

Early versions do not provide an application UI, cloud synchronization, accounts, backend services, domain-specific models, analytics, or custom cryptographic primitives.

## Versioning

The project follows [Semantic Versioning](https://semver.org/). Git release tags use the form `vMAJOR.MINOR.PATCH`, such as `v0.1.0`.

Until `v1.0.0`, the protocol and API are experimental and may change incompatibly. A version is released only when its changelog and tests are complete; ordinary development remains under the `Unreleased` section of the changelog.

## Contributing and security

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development workflow, [SECURITY.md](SECURITY.md) for vulnerability reporting, and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community expectations.

## License

Licensed under the [Apache License 2.0](LICENSE).
