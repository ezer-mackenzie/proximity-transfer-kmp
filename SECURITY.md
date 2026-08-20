# Security policy

## Project status

Proximity Transfer is experimental and has not reached a stable security release. It must not yet be treated as suitable for sensitive or safety-critical data.

The first milestone verifies transfer integrity with SHA-256. Integrity checking alone does not provide peer authentication, confidentiality, authorization, or replay protection.

## Reporting a vulnerability

Do not disclose suspected vulnerabilities in a public issue.

Once the repository has a public hosting location, use its private security-advisory mechanism to report vulnerabilities. Until a private reporting channel is configured, contact the repository owner privately and include:

- affected revision or version;
- reproduction steps or a proof of concept;
- expected and observed behavior;
- potential impact;
- suggested mitigation, if known.

The project will acknowledge a report, investigate it, coordinate remediation, and credit the reporter when requested and appropriate. Specific response deadlines are not promised until a maintained security channel exists.

## Security design rules

- Never implement cryptographic primitives manually.
- Use reviewed implementations of standard algorithms.
- Authenticate protocol state and metadata when encryption is introduced.
- Treat transport security and protocol security as separate concerns.
- Validate lengths, versions, states, identifiers, and resource limits before allocating or processing payloads.
- Do not report success until the reconstructed payload passes integrity verification.
- Document threat-model and wire-format decisions that affect interoperability.

## Supported versions

No supported public version exists yet. This table will be updated after the first release:

| Version | Supported |
|---|---|
| Unreleased development code | Best effort only |
