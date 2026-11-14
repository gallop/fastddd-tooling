# Contributing to FastDDD Tooling

## Before opening a change

- Preserve the Tooling API compatibility contract and the independent Framework version boundary.
- Use Java 21 and run `./mvnw.cmd -B clean verify`.
- Add golden-output or generated-project coverage for template, generator, archetype and Maven plugin changes.
- Use Conventional Commit prefixes with a Chinese descriptive subject.

## Pull requests

- Keep one concern per pull request and explain generated-output or compatibility impact.
- Do not submit credentials, customer data, proprietary Studio code, private templates, or copied third-party code without an auditable license source.
- Generated output must not contain internal URLs, private package coordinates, secrets or Studio-only assets.

## Reporting security issues

Follow [`SECURITY.md`](SECURITY.md); do not disclose unpatched vulnerabilities in public issues.
