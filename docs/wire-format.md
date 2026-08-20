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

Decoders reject invalid magic, unsupported versions, unknown frame types, negative or mismatched lengths, and incomplete headers. Encoders currently emit only the current protocol version.

The frame header does not itself provide authentication, encryption, or corruption detection. End-to-end integrity and session security are separate protocol layers planned for later milestones.
