# Contributing

Thank you for helping build Proximity Transfer. The project is experimental and prioritizes correctness, security, and interoperability.

## Before contributing

Read [AGENTS.md](AGENTS.md), the [README](README.md), the [Code of Conduct](CODE_OF_CONDUCT.md), and the [security policy](SECURITY.md). Discuss large protocol, wire-format, security, or public-API changes before implementing them.

## Development principles

- Keep the common protocol independent of Android and iOS APIs.
- Put platform integrations behind transport abstractions.
- Introduce abstractions only when an implemented requirement needs them.
- Use established codecs and cryptographic libraries instead of inventing primitives.
- Accompany behavior changes with automated tests and relevant documentation.
- Do not require hardware to test the protocol core.

## Local checks

The repository includes the Gradle wrapper. Run the relevant checks before submitting a change:

```bash
./gradlew check
```

Platform-specific work may require Android SDK or Xcode tooling in addition to the common test suite. Document any test that cannot run in the contributor's environment.

## Commit convention

Use focused [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat: add binary frame header
fix: reject duplicate terminal frames
test: cover corrupted chunks
docs: explain transport negotiation
chore: configure project metadata
```

Allowed types include `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, and `chore`. Use `!` or a `BREAKING CHANGE:` footer for an incompatible change.

When another person or agent made a concrete contribution included in the commit, add an accurate trailer:

```text
Co-authored-by: Name <email@example.com>
```

Do not add a coauthor only because they reviewed, suggested a tool, or were present in the conversation.

## Changelog and releases

Add user-visible changes to the `Unreleased` section of [CHANGELOG.md](CHANGELOG.md). The project follows Semantic Versioning:

- `MAJOR`: incompatible stable API or wire-protocol changes.
- `MINOR`: backward-compatible capabilities.
- `PATCH`: backward-compatible fixes.

Release tags must be annotated and formatted as `vMAJOR.MINOR.PATCH`, for example:

```bash
git tag -a v0.1.0 -m "v0.1.0"
```

Do not create a release tag until its intended scope is complete and verified.

## Pull requests

Explain what changed, why it changed, how it was tested, and which platforms are affected. Call out wire-format, compatibility, permission, privacy, or security implications explicitly.
