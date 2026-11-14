# FastDDD Tooling Community Documentation Index

**English** | [简体中文](README.zh-CN.md)

The sole responsibility of this directory is to explain the Tooling API, generators, templates, FastDoc, the Maven plugin, scaffolding, and their version compatibility. Framework runtime documentation and Studio commercial-product documentation do not take this repository as their source of truth.

## Current Entries

| Purpose | Entry |
|---|---|
| Maven project initialization & code generation | [`../fastddd-tooling/fastddd-maven-plugin/README.md`](../fastddd-tooling/fastddd-maven-plugin/README.md) |
| FastDoc Starter | [`../fastddd-tooling/fastddd-fastdoc-spring-boot-starter/README.md`](../fastddd-tooling/fastddd-fastdoc-spring-boot-starter/README.md) |
| Template development | [`../fastddd-tooling/fastddd-templates/docs/TEMPLATE_DEVELOPMENT_GUIDE.md`](../fastddd-tooling/fastddd-templates/docs/TEMPLATE_DEVELOPMENT_GUIDE.md) |
| Aggregate module & deprecation notes | [`../fastddd-tooling/README.md`](../fastddd-tooling/README.md) |
| Cross-repository candidate-artifact builds | The internal CI notes (CROSS_REPOSITORY_CI.md) were moved to a private out-of-repository archive on 2026-09-12 and are not published with the open-source repository |
| Compatible combinations | [`COMPATIBILITY_MATRIX.md`](COMPATIBILITY_MATRIX.md) and the module-level YAML specification source |

## Post-split Documentation Cleanup

Copies of Framework/Studio documentation replicated into this repository before the split were removed in ECO-004 and remain traceable through Git history:

- Framework's common architecture/API/upgrade documentation takes `fastddd-framework` as its source of truth;
- Studio's AI design, visual table design, unified generator, and deployment documentation take the private `fastddd-studio-pro` as their source of truth;
- Tooling no longer copies this documentation, nor does it maintain verification conclusions about it.

## New Document Admissions

New documents must be associated with at least one Tooling module and must state the target audience, compatible versions, and verification command. Content containing Studio-private processes, internal addresses, credentials, or commercial planning must not enter this repository.

> Canonical owner: FastDDD Tooling Community. Last updated: 2026-09-12 (cross-repository CI notes moved to a private out-of-repository archive).
