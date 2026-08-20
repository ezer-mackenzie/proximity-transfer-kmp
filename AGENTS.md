# Agent instructions

## Project scope

This repository contains a Kotlin Multiplatform library for offline, proximity-based peer-to-peer transfer of arbitrary binary data.

The library is domain-agnostic. Business models, application workflows, remote services, and content-specific validation belong to consuming applications.

## Primary goal

Build a transport-agnostic protocol that can transfer bytes between nearby Android and iOS devices, reconstruct the exact payload, and verify it with SHA-256.

The first milestone must work entirely with an in-memory transport. Do not implement hardware transports before the protocol core is proven by automated tests.

## Architecture rules

- Keep protocol and transport separated through interfaces.
- Keep protocol models, framing, codec, chunking, reconstruction, integrity, negotiation, and session state in common Kotlin whenever platform APIs are not required.
- Isolate Android and iOS APIs in their respective source sets or platform modules.
- Model platform differences as capabilities. Never claim that a transport exists on a platform when its public APIs do not support it.
- Negotiate the intersection of peer capabilities and provide an explicit failure when no compatible transport exists.
- Treat files as arbitrary byte streams; metadata belongs in a manifest.
- Keep the wire format binary, deterministic, versioned, documented, and independently decodable.
- Do not implement cryptographic primitives manually.
- Do not freeze a public API or create empty modules prematurely.

## Platform strategy

The common protocol must not depend on NFC, BLE, Wi-Fi Aware, Wi-Fi Direct, Multipeer Connectivity, QR, or any other concrete mechanism.

Expected capability families are:

- Android and iOS: memory transport, BLE, QR bootstrap, and standards-based local-network connections where a common network can be established.
- Android-specific candidates: NFC peer/bootstrap features, Wi-Fi Aware, and Wi-Fi Direct.
- iOS-specific candidate: Multipeer Connectivity.

Platform-specific transports must interoperate only when both peers expose compatible semantics. A platform-exclusive transport is not a cross-platform fallback.

## Development order

1. Protocol models and versioning.
2. Frame representation and codec.
3. Memory transport.
4. Basic sender and receiver.
5. Chunking and reconstruction.
6. SHA-256 verification.
7. Session state machine.
8. Capabilities and negotiation.
9. Cross-platform transport experiments.
10. Hardware-specific transports.
11. Security, recovery, resumable transfers, and multiple payloads.

## First definition of done

Automated tests must transfer random payloads of multiple sizes through `MemoryTransport` and prove:

```text
input.size == output.size
SHA-256(input) == SHA-256(output)
```

Tests must not require Android or iOS hardware for core protocol behavior.

## Kotlin and testing

- Keep each class, interface, and enum in its own file, named after the type. Small type aliases and extension functions may share a focused file when they do not define an independent type.
- Organize packages by cohesive responsibility as they grow; for example, connection contracts belong under `transport.connection` and concrete transports under their own packages.
- Prefer immutable models, explicit domain types, sealed hierarchies, coroutines, and dependency inversion.
- Avoid global mutable state, giant manager classes, unnecessary inheritance, Android dependencies in common code, and speculative abstractions.
- Cover encode/decode round trips, chunk reconstruction, valid and invalid state transitions, corrupt data, missing and duplicate chunks, unsupported versions, unknown messages, hash mismatch, and interruptions as those features are introduced.

## Documentation

Keep architectural and interoperability decisions synchronized with the public documentation. Do not copy this file into tool-specific instruction files; those files should point here.

Priority when choosing an implementation:

```text
Correctness > Security > Interoperability > Reliability > Simplicity > Performance > Novelty
```

## Git conventions

- Use Conventional Commits: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, and `chore`.
- Keep commits focused and do not mix unrelated documentation, refactors, or features.
- Follow Semantic Versioning. Release tags must use `vMAJOR.MINOR.PATCH`, for example `v0.1.0`.
- Add a `Co-authored-by: Name <email>` trailer for every agent or person whose concrete contribution is included in a commit. Do not add coauthors who did not contribute to that commit.
- Update `CHANGELOG.md` for user-visible changes.

## Out of scope for early versions

Do not add cloud synchronization, accounts, backend services, analytics, domain-specific models, application UI, or hardware transports that bypass the common protocol unless explicitly requested.
