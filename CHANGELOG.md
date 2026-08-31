# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.5.0] - 2026-08-31

### Added

- Session recovery orchestrator for automated snapshot retrieval and transfer resumption (`SessionRecoveryOrchestrator`).
- Sequential multi-payload batch transfer controller (`PayloadBatchTransfer`).

## [0.4.0] - 2026-08-31

### Added

- Network service discovery advertisement model and binary codec (`NetworkServiceAdvertisement`, `NetworkDiscoveryCodec`).
- Resumable session state persistence models and thread-safe store (`SessionSnapshot`, `SessionStore`, `InMemorySessionStore`).

## [0.3.0] - 2026-08-28

### Added

- Bluetooth Low Energy (BLE) bootstrap payload binary codec (`BleBootstrapCodec`).
- Android platform transport foundation for BLE L2CAP socket connections (`AndroidBleTransport`).
- iOS platform transport foundation for Multipeer Connectivity (`IosMultipeerTransport`).

## [0.2.0] - 2026-08-26

### Added

- Binary NDEF NFC bootstrap codec (`NfcBootstrapCodec`) formatting `application/vnd.proximitytransfer.bootstrap+bin` records.
- Multi-item transfer manifest domain models and binary codec (`MultiItemManifest`, `ManifestItem`, `MultiItemManifestCodec`).
- Resumable transfer protocol with compact chunk reception bitmaps (`ChunkBitmap`, `ResumeRequest`, `ResumeRequestCodec`).
- Resumable protocol sender and receiver implementations (`ResumableProtocolSender`, `ResumableProtocolReceiver`).
- Session frame security with sequence-derived keystreams and HMAC-SHA256 authentication (`SessionKeySpec`, `FrameEncryptionCodec`).
- Handshake key exchange nonces and session key derivation (`HandshakeMessage`, `HandshakeMessageCodec`).
- High-level multiplatform facade interface and implementation (`ProximityTransferEngine`, `DefaultProximityTransferEngine`).
- Maven Central and GitHub Packages publication configuration (`maven-publish`) in Gradle library module.

## [0.1.0] - 2026-08-24

### Added

- Initial project vision and contribution documentation.
- Android and iPhone capability and interoperability strategy.
- Experimental protocol version model and deterministic binary frame codec.
- Transport and connection abstractions with a bidirectional in-memory implementation.
- Basic protocol sender and receiver for a single binary payload.
- Deterministic payload chunking, chunk metadata codec, and validated reconstruction.
- Transfer manifests with payload size and end-to-end SHA-256 verification.
- Observable, concurrency-safe transfer session state machine with validated transitions.
- Runtime transport capabilities, binary capability codec, and deterministic negotiation.
- Bidirectional capability exchange over an established connection with transfer-session reuse.
- Runtime registry that resolves negotiated data capabilities to concrete transport implementations.
- Control-to-data connection orchestration with transfer-session continuity.
- Explicit peer confirmation of negotiated routes before opening a data transport.
- Receiver `COMPLETE`/`ERROR` acknowledgements integrated with sender and receiver session states.
- Deterministic failure behavior when either transfer endpoint disconnects.
- Observable sender and receiver byte progress through multiplatform `StateFlow` APIs.
- Configurable payload-size and chunk-count limits enforced before transfer processing.
- Deterministic length-prefixed stream codec (`StreamFramingCodec`) for continuous byte streams over network sockets.
- `LocalNetworkTransport` and `SocketConnection` implementations backing the `LOCAL_NETWORK` transport capability.
- Connection bootstrapping domain model (`BootstrapPayload`) and binary codec (`BootstrapPayloadCodec`).
- Compact QR code URI codec (`QrBootstrapCodec`) formatting `proximity://v1?data=...` payloads.

### Changed

- Replaced the upstream template identity with the Proximity Transfer project identity.
- Split capability models, codecs, negotiation, and peer exchange into focused packages.
- Set the release version to `0.1.0`.
- Organized classes, interfaces, and enums into individual files.
- Grouped connection contracts under the dedicated `transport.connection` package.
- Grouped multiplatform implementation under `io.github.ezer_mackenzie.proximitytransfer.core` while retaining a single library module.
- Aligned Kotlin packages, Android namespace, and Gradle group with the `ezer-mackenzie` GitHub identity.

### Removed

- Fibonacci sample implementation and tests.
- Incomplete Maven Central publishing configuration inherited from the template.

