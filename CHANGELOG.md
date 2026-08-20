# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Initial project vision and contribution documentation.
- Android and iPhone capability and interoperability strategy.
- Experimental protocol version model and deterministic binary frame codec.
- Transport and connection abstractions with a bidirectional in-memory implementation.
- Basic protocol sender and receiver for a single binary payload.
- Deterministic payload chunking, chunk metadata codec, and validated reconstruction.

### Changed

- Replaced the upstream template identity with the Proximity Transfer project identity.
- Set the development version to `0.1.0-SNAPSHOT`.
- Organized classes, interfaces, and enums into individual files.
- Grouped connection contracts under the dedicated `transport.connection` package.
- Grouped multiplatform implementation under `io.github.ezer_mackenzie.proximitytransfer.core` while retaining a single library module.
- Aligned Kotlin packages, Android namespace, and Gradle group with the `ezer-mackenzie` GitHub identity.

### Removed

- Fibonacci sample implementation and tests.
- Incomplete Maven Central publishing configuration inherited from the template.

No public version of the proximity-transfer protocol has been released yet.

<!-- Add released versions below using this form:
## [0.1.0] - YYYY-MM-DD

Release it with the Git tag v0.1.0.
-->
