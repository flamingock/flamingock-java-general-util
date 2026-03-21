<p align="center">
  <img src="https://raw.githubusercontent.com/flamingock/flamingock-java/master/misc/logo-with-text.png" width="420px" alt="Flamingock logo" />
</p>

<h3 align="center">flamingock-general-util</h3>
<p align="center">Internal shared utilities for the Flamingock ecosystem.</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.flamingock/flamingock-general-util">
    <img src="https://img.shields.io/maven-central/v/io.flamingock/flamingock-general-util" alt="Maven Version" />
  </a>
  <a href="https://github.com/flamingock/flamingock-java-general-util/actions/workflows/build.yml">
    <img src="https://github.com/flamingock/flamingock-java-general-util/actions/workflows/build.yml/badge.svg" alt="Build" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" />
  </a>
</p>

---

> **Internal library.**
> This module is a shared dependency used across [Flamingock](https://github.com/flamingock/flamingock-java) modules.
> It is published to Maven Central for build convenience, but is **not intended for direct external consumption**.
> Its API surface may change without notice between releases.

---

## What it provides

General-purpose utilities and helper classes shared across Flamingock modules:

- **HTTP client** — Fluent request builder on top of Apache HttpClient with JSON serialization.
- **Reflection utilities** — Generic type resolution, constructor and method discovery, annotation collection.
- **Typed identifiers** — Immutable, validated ID types (`RunnerId`, `ServiceId`, `EnvironmentId`).
- **Error model** — Standardized `FlamingockError` with HTTP variants and recovery flags.
- **Time and concurrency** — Testable `TimeService`, `StopWatch`, and controlled `ThreadSleeper`.
- **Serialization** — Pre-configured Jackson `ObjectMapper` and YAML file loading via SnakeYAML.
- **Audit constants** — Field names and store names used by the audit subsystem.
- **Common types** — `Result<Ok, Error>`, `Pair`, `Trio`, `Wrapper`, functional interfaces.
- **Logging** — Centralized logger factory with consistent Flamingock log formatting.

---

## Coordinates

```xml
<dependency>
    <groupId>io.flamingock</groupId>
    <artifactId>flamingock-general-util</artifactId>
    <version>${flamingock-general-util.version}</version>
</dependency>
```

```kotlin
implementation("io.flamingock:flamingock-general-util:$flamingockGeneralUtilVersion")
```

---

## Requirements

- **Java 8+**

---

## Building

```bash
./gradlew build
```

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).

<p align="center">
  <a href="https://flamingock.io">flamingock.io</a>
</p>
