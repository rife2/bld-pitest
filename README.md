# [PIT Mutation Testing](https://pitest.org/) Extension for [b<span style="color:orange">l</span>d](https://rife2.com/bld) 

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.2.1-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/bld-pitest/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-pitest)
[![Snapshot](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/snapshots/com/uwyn/rife2/bld-pitest/maven-metadata.xml?label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-pitest)
[![GitHub CI](https://github.com/rife2/bld-pitest/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-pitest/actions/workflows/bld.yml)

To install, please refer to the [extensions documentation](https://github.com/rife2/bld/wiki/Extensions).

To run mutation tests and coverage, add the following to your build file:

```java
@BuildCommand(summary = "Run PIT mutation tests")
public void pit() throws Exception {
    new PitestOperation()
        .fromProject(this)
        .reportDir(Path.of("reports", "mutations"))
        .targetClasses(pkg + ".*")
        .targetTests(pkg + ".*")
        .verbose(true)
        .execute();
    }
```

```console
./bld compile pit

```

- [View Examples Project](https://github.com/rife2/bld-pitest/blob/master/examples/src/bld/java/com/example/)

Please check the [PitestOperation documentation](https://rife2.github.io/bld-pitest/rife/bld/extension/PitestOperation.html#method-summary) for all available configuration options.

## Pitest (PIT) Dependency

Don't forget to add the Pitest `test` dependencies to your build file, as they are not provided by the extension. For example:

```java
repositories = List.of(MAVEN_CENTRAL);
scope(test)
    .include(dependency("org.pitest", "pitest", version(1, 18, 2)))
    .include(dependency("org.pitest", "pitest-command-line", version(1, 17, 4)))
    .include(dependency("org.pitest", "pitest-junit5-plugin", version(1, 2, 1)))
    .include(dependency("org.pitest", "pitest-testng-plugin", version(1, 0, 0)));
```
