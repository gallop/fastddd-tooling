# FastDDD Tooling Community 文档索引

[English](README.md) | **简体中文**

本目录的唯一职责是解释 Tooling API、生成器、模板、FastDoc、Maven 插件、脚手架及其版本兼容性。Framework 运行时文档和 Studio 商业产品文档不属于本仓事实源。

## 现行入口

| 目标 | 入口 |
|---|---|
| Maven 项目初始化与代码生成 | [`../fastddd-tooling/fastddd-maven-plugin/README.zh-CN.md`](../fastddd-tooling/fastddd-maven-plugin/README.zh-CN.md) |
| FastDoc Starter | [`../fastddd-tooling/fastddd-fastdoc-spring-boot-starter/README.zh-CN.md`](../fastddd-tooling/fastddd-fastdoc-spring-boot-starter/README.zh-CN.md) |
| 模板开发 | [`../fastddd-tooling/fastddd-templates/docs/TEMPLATE_DEVELOPMENT_GUIDE.md`](../fastddd-tooling/fastddd-templates/docs/TEMPLATE_DEVELOPMENT_GUIDE.md) |
| 聚合模块与弃用说明 | [`../fastddd-tooling/README.zh-CN.md`](../fastddd-tooling/README.zh-CN.md) |
| 跨仓候选制品构建 | 内部 CI 说明（CROSS_REPOSITORY_CI.md）已于 2026-09-12 移至仓外私有档案，不随开源仓发布 |
| 兼容组合 | [`COMPATIBILITY_MATRIX.zh-CN.md`](COMPATIBILITY_MATRIX.zh-CN.md) 与模块内 YAML 规范源 |

## 已完成的拆仓文档清理

拆仓前复制到本仓的 Framework/Studio 文档副本已在 ECO-004 移除，仍可通过 Git 历史追溯：

- Framework 公共架构/API/升级文档以 `fastddd-framework` 为事实源；
- Studio AI 设计、可视化建表、统一生成器和部署文档以私有 `fastddd-studio-pro` 为事实源；
- Tooling 不再复制这些文档，也不维护它们的验证结论。

## 新文档准入

新文档必须至少关联一个 Tooling 模块，并说明目标读者、兼容版本和验证命令。包含 Studio 私有流程、内部地址、凭据或商业规划的内容不得进入本仓。

> Canonical owner：FastDDD Tooling Community。最后更新：2026-09-12（跨仓 CI 说明移至仓外私有档案）。
