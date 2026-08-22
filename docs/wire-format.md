# Wire format

Status: experimental, protocol version 1.

Each frame uses a fixed 10-byte header followed by its payload:

| Offset | Size | Field | Encoding |
|---:|---:|---|---|
| 0 | 4 bytes | Magic | ASCII `PXFR` (`50 58 46 52` hexadecimal) |
| 4 | 1 byte | Protocol version | Unsigned integer; currently `1` |
| 5 | 1 byte | Frame type | Unsigned integer |
| 6 | 4 bytes | Payload length | Unsigned meaning, big-endian byte order; implementation is currently limited to a non-negative Kotlin `Int` |
| 10 | Variable | Payload | Exactly the number of bytes declared by payload length |

## Frame types

| Code | Name | Purpose |
|---:|---|---|
| 1 | `DATA` | Carries opaque payload bytes |
| 2 | `MANIFEST` | Declares payload size, chunk count, and SHA-256 digest |
| 3 | `CAPABILITIES` | Advertises the sender's runtime transport capabilities |
| 4 | `COMPLETE` | Confirms that the receiver reconstructed and verified the payload |
| 5 | `ERROR` | Reports that the receiver rejected the transfer |

## `CAPABILITIES` payload

| Payload offset | Size | Field | Encoding |
|---:|---:|---|---|
| 0 | 1 byte | Capability count | Positive unsigned integer |
| 1 | Variable | Capability codes | One unsigned byte per capability |

Capability codes are encoded once each in ascending numeric order. Decoders reject empty sets, unknown codes, duplicates, and count mismatches. Codes and negotiation behavior are documented in [capabilities.md](capabilities.md).

## `COMPLETE` payload

`COMPLETE` contains exactly the 32-byte SHA-256 digest verified by the receiver. The sender compares it with its original digest before reporting success.

## `ERROR` payload

| Payload offset | Size | Field | Encoding |
|---:|---:|---|---|
| 0 | 1 byte | Error code | Unsigned integer |
| 1 | 2 bytes | Message length | Unsigned integer, big-endian |
| 3 | Variable | Message | Bounded UTF-8 text |

Current error codes are `1` for a malformed transfer and `2` for integrity failure. Messages are limited to 1,024 bytes and must match the declared length.

## `MANIFEST` payload

Every transfer starts with one fixed-width manifest:

| Payload offset | Size | Field | Encoding |
|---:|---:|---|---|
| 0 | 8 bytes | Payload size | Non-negative integer, big-endian |
| 8 | 4 bytes | Chunk count | Positive integer, big-endian |
| 12 | 32 bytes | SHA-256 | Digest of the complete original payload |

The receiver uses the declared chunk count to read one complete transfer. It reconstructs the payload, validates its size, computes SHA-256, and rejects the transfer unless both values match the manifest.

## `DATA` payload

For the current single-transfer protocol, every `DATA` frame contains one chunk:

| Payload offset | Size | Field | Encoding |
|---:|---:|---|---|
| 0 | 4 bytes | Chunk index | Non-negative integer, big-endian |
| 4 | 4 bytes | Total chunks | Positive integer, big-endian |
| 8 | Variable | Chunk data | Opaque bytes |

Indexes are zero-based. The default sender chunk size is 16 KiB. An empty payload is represented by one chunk with index `0`, total `1`, and no chunk data.

The receiver accepts chunks out of order during reconstruction but rejects missing indexes, duplicate indexes, inconsistent totals, and invalid metadata. The current sender and receiver handle one transfer at a time, so this experimental format does not yet carry a transfer or session identifier.

Decoders reject invalid magic, unsupported versions, unknown frame types, negative or mismatched lengths, and incomplete headers. Encoders currently emit only the current protocol version.

SHA-256 detects accidental or malicious content modification relative to the received manifest, but the manifest is not authenticated yet. Peer authentication, encryption, and authenticated metadata remain separate protocol layers planned for later milestones.
