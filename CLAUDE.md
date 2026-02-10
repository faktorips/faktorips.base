# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Faktor-IPS base repository** (faktorips.base), containing the core Faktor-IPS platform for developing insurance applications. Faktor-IPS is a model-driven development tool that generates Java code from insurance product models.

## Build System

### Technology Stack
- **Java**: 21+ (required)
- **Maven**: 3.9.11+ with Tycho 5.0.0 for Eclipse plugin builds
- **Build Tool**: Multi-module Maven build with Eclipse Tycho for RCP/plugin components

### Essential Build Commands

Build the entire project:
```bash
mvn clean install
```

Build without tests (faster compilation check):
```bash
mvn clean package -DskipTests
```

Build specific module, only for non Tycho projects:
```bash
mvn clean install -pl :module-artifact-id -am
```

Run tests:
```bash
mvn test
```

Run tests for specific module, only for non Tycho projects:
```bash
mvn test -pl :module-artifact-id
```

Run integration tests (requires X display for Eclipse UI tests):
```bash
mvn clean verify
```

Build with specific Eclipse target platform:
```bash
mvn clean install -Dtarget-platform=eclipse-2024-09
```

Run code quality checks (Checkstyle, SpotBugs):
```bash
mvn checkstyle:checkstyle
mvn spotbugs:spotbugs
```

Generate Javadoc:
```bash
mvn javadoc:javadoc
```

### Build Profiles
- `mavenCentralRelease`: Prepare artifacts for Maven Central (includes GPG signing)
- `nexusRelease`: Deploy to Faktor Zehn's internal Nexus repository
- `DependencyCheck`: Run OWASP dependency security checks

### Maven Configuration
Maven settings are in `.mvn/maven.config`:
- Tycho version: 5.0.0 (critical for Eclipse plugin builds)
- Locale: en_US (required for reproducible builds)
- Local artifacts: ignored (`-Dtycho.localArtifacts=ignore`)

## Architecture

### Module Structure

The repository is organized into **five main modules**:

1. **runtime/** - Runtime libraries used by generated code and applications
   - `faktorips-runtime`: Core runtime API and base classes
   - `faktorips-runtime/client`: Minimal runtime subset
   - `faktorips-runtime-groovy`: Formula execution with Groovy
   - `faktorips-runtime-javax-xml`: JAXB support (legacy javax.xml)
   - `faktorips-runtime-jakarta-xml`: JAXB support (modern jakarta.xml)
   - `faktorips-testsupport`: Test utilities and matchers
   - `faktorips-valuetypes`: Standard value type implementations
   - `faktorips-valuetypes-joda`: Joda-Time value types
   - `bom`: Bill of Materials for dependency management

2. **devtools/** - Development tools and Eclipse IDE plugins
   - `common/`: Shared utilities and model implementations (plain Java, no Eclipse dependencies)
   - `eclipse/`: Eclipse RCP plugins and UI components
     - `plugins/`: Core plugins (model, UI, builders, exporters)
     - `plugins/tests/`: Test plugins with Eclipse dependencies
     - `features/`: Eclipse feature definitions
     - `sites/`: P2 update sites for distribution
     - `targets/`: Eclipse target platform definitions for different Eclipse versions

3. **maven/** - Maven plugins for building Faktor-IPS projects
   - `faktorips-maven-plugin` (in `build/` directory): Code generation from Faktor-IPS models
   - `faktorips-validation-maven-plugin` (in `validation/` directory): Model validation

4. **faktorips-maven-archetype/** - Maven archetype for creating new Faktor-IPS projects

5. **codequality-config/** - Shared Checkstyle, SpotBugs, and code quality configurations

### Key Architectural Patterns

#### Model-Driven Code Generation
Faktor-IPS uses a model-driven approach where insurance products and business logic are defined in `*.ips*` files within `model` directories. The configuration of Faktor-IPS projects is stored in `.ipsproject` files. The **stdbuilder** (`org.faktorips.devtools.stdbuilder`) generates Java code from these models:
- Policy components (insurance contracts)
- Product components (insurance products)
- Table structures (rating tables)
- Enumerations 
- Test cases (JUnit test generation)

Code generation uses **Xtend** templates in the `xtend/` subdirectories of the stdbuilder plugin.

#### Runtime Architecture
Generated code depends on `faktorips-runtime` APIs:
- `IRuntimeRepository`: Repository for loading product configurations
- `IModelObject`: Base interface for all generated model objects
- `IConfigurableModelObject`: Base interface for all generated model objects that are configurable by products
- `IProductComponent`: Base interface for all generated products
- `IpsModel`: Generic access to model information and product data
- Formula execution support via Groovy runtime
- Table contents stored in CSV format and loaded via `opencsv`

#### Eclipse Plugin Architecture (Tycho)
The devtools use **Eclipse Tycho** to build OSGi bundles and Eclipse plugins:
- **Target platforms** define Eclipse dependencies (see `devtools/eclipse/targets/`)
- Multiple Eclipse versions supported: 2024-09 through 2025-12
- Packaging type: `eclipse-plugin`, `eclipse-feature`, `eclipse-repository`
- Tests use `tycho-surefire-plugin` with UI test support (requires X display)

#### Dual Build System
- **Runtime modules**: Standard Maven with `maven-bundle-plugin` for OSGi manifests
- **Devtools modules**: Eclipse Tycho for full RCP plugin builds with P2 repository generation

### Code Quality and Testing

### Gerrit code reviews
- Gerrit is used for code reviews and as main git repository, https://github.com/faktorips/faktorips.base is our mirror and open source repository

#### Essential Gerrit Commands
Commit new change
```bash
git add .
git commit -m "Jira-Ticket-Nr/Jira-SubTask-Nr :: short comment about work"
git review
```
Commit existing change
```bash
git add .
git commit --amend --no-edit
git review
```
Working on an change
```bash
git review -d changeNr
```

#### Testing Frameworks
- Runtime: JUnit 5 (Jupiter) preferred, JUnit 4 supported for legacy tests
- Devtools: JUnit with Eclipse platform (tycho-surefire)
- Mocking: Mockito 5.x
- Matchers: Hamcrest 2.x

#### Test Module Pattern
Test plugins in `devtools/eclipse/plugins/tests/` follow Eclipse plugin testing conventions:
- Packaging: `eclipse-plugin`
- Dependencies declared in `MANIFEST.MF` and `pom.xml`
- Base test utilities in `org.faktorips.abstracttest` and `org.faktorips.abstracttest.core`

#### Code Quality Tools
- **Checkstyle**: Configuration in `codequality-config/checkstyle/fips_checks.xml`
- **SpotBugs**: Exclusions in `codequality-config/findbugs/fips-exclusion-filter.xml`
- **JaCoCo**: Code coverage reporting (integrated in build)
- **Revapi**: API compatibility checking between versions

### Versioning and Releases

Current version: **26.7.0-SNAPSHOT**
- Version format: `<major>.<minor>.<patch>[-SNAPSHOT]`
- OSGi bundle versions include qualifiers: `26.7.0.ci_20260126-1234`
- `version.kind` property controls qualifier: `ci`, `ayyyyMMdd-XX`, `rcXX`, `mXX`, or `release`

### Dependencies and BOMs

Use Bill of Materials (BOM) for dependency management:
- `faktorips-runtime-bom`: Runtime dependencies
- `faktorips-devtools-bom`: Devtools dependencies

Key dependencies:
- Apache Commons (Lang3, Text, BeanUtils, Collections)
- Groovy 5.0.1 (formula execution)
- OpenCSV (table data)
- JAXB (javax/jakarta variants)
- Joda-Time (legacy datetime support)

## CI/CD

### Jenkins
- **Main pipeline**: `.ci/Jenkinsfile`
- Runs on internal Faktor Zehn infrastructure
- Parallel builds with `-T 8` (8 threads)
- Quality gates: Checkstyle, SpotBugs, JaCoCo coverage
- Dependency-Check for security scanning
- Deploys documentation to https://doc.faktorzehn.org/

### GitHub Actions
- **Workflow**: `.github/workflows/maven.yml`
- Builds against multiple Eclipse versions in parallel (matrix build)
- Integration tests run on Eclipse 2024-09 with Xvfb (virtual display)
- Test results uploaded as artifacts

## Development Conventions

### Package Structure
- Runtime: `org.faktorips.runtime.*`
- Devtools model: `org.faktorips.devtools.model.*`
- Devtools core: `org.faktorips.devtools.core.*`
- Devtools UI: `org.faktorips.devtools.core.ui.*`
- Builders: `org.faktorips.devtools.stdbuilder.*`

### Null Handling
SpotBugs annotations used for null safety:
- `@NonNull`, `@Nullable`, `@CheckForNull`

### License
AGPL 3.0 with additional permissions for Eclipse and JUnit libraries. Alternative commercial licensing available from Faktor Zehn GmbH.

## Documentation

- **Project site**: https://faktorzehn.org
- **Documentation**: https://doc.faktorzehn.org/
- **Maven plugin docs**: https://doc.faktorzehn.org/faktorips-maven-plugin/
- **Issue tracker**: https://jira.convista.com/projects/FIPS and GitHub Issues
- **Source repository**: https://github.com/faktorips/faktorips.base
