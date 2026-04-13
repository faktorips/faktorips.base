## Review Checklist

### Bytecode Compatibility
- Runtime/Valuetypes must remain bytecode-compatible as far as possible
- Breaking changes to Runtime/Valuetypes only in a summer release
- Code generated with a newer IPS must run against older code (e.g. Sparte 19.12 against Basis 19.7)
- Code generated with an older IPS must run against newer code (e.g. Sparte 19.7 against Basis 19.12)
- If Runtime/Valuetypes were changed: verify that a newer/older runtime can be deployed with IPM Sample

### Integration Tests
- Integration tests must be free of compile errors (mandatory for Fips reviews)

### Naming Conventions: Plugins & Features
Structure using `org.faktorips.mysomething` as an example:

| Plugin/Feature | Purpose |
|---|---|
| `org.faktorips.mysomething` | Feature for the extension |
| `org.faktorips.mysomething.core` | Model and non-UI code |
| `org.faktorips.mysomething.core.test` | Test plugin for core |
| `org.faktorips.mysomething.core.nl1` | National language plugin for core |
| `org.faktorips.mysomething.nl1` | National language support feature |
| `org.faktorips.mysomething.ui` | UI plugin |
| `org.faktorips.mysomething.ui.nl1` | National language plugin for UI |
| `org.faktorips.mysomething.ui.test` | Test plugin for UI |

- New code must follow this naming structure
- UI code belongs in `.ui` plugins, not in `.core`
- Business logic belongs in `.core` — PMOs and UI layer only for UI concerns; use interfaces/predicates to decouple
- PMOs must be domain-oriented: use domain property names, no UI objects (e.g. `BindingContext`) in PMOs; domain validation belongs in domain classes

### Naming Conventions: Getter / Finder / Searcher
- **Getter** `getSomething()` — returns only the stored name as a String, not the referenced object
- **Finder** `findSomething(IIpsProject)` — returns the referenced instance; always searches transitively referenced projects and IpsArchives; always requires an `IIpsProject` parameter
- **Searcher** `searchSomething()` — searches for instance objects of a type (e.g. all ProductCmpts for a ProductCmptType); searches the current project and all referencing projects (not the referenced ones)
- Search methods are inefficient — **must not be used in performance-critical locations** (e.g. during validation)
- When only checking for existence: use `IpsSrcFiles` instead of `find...()` as no XML parsing is needed
- Boolean getters use the `is` prefix — `isEnabled()` not `getEnabled()`
- Method names must accurately describe behavior — if it deletes files, don't call it `clearCache`
- Helper classes that operate on `Foo` should be named `Foos` (Java 8+ convention)

### UI Design (for UI changes)
- Dialogs follow the unified Faktor-IPS design concept
- Eclipse UI Guidelines are followed (see [Eclipse UI Best Practices](https://eclipse-platform.github.io/ui-best-practices/))
- UI is clear, simple and understandable (usability/operability)

**Views & Editors**
- Only the most common commands in the view toolbar — every toolbar command must also be available in a menu (window menu, context menu, or view menu)
- View pulldown menu for commands without selection context; context menu for selection-related actions
- Cut/Copy/Paste and global commands must be accessible via the window menu bar
- Editor changes: open-save-close lifecycle; view changes: save immediately
- Persist the state of every view between sessions

**Wizards & Dialogs**
- Wizard starts with a prompt, not an error message
- Pre-fill wizard fields with the current workbench state
- Initial focus on the first input control (or the default button if no input controls)
- Use a Browse button when referencing an existing object

**Workbench & Preferences**
- Headline style (title case) for menus, tooltips, titles, tabs, column headers, buttons
- Sentence style for all control labels (checkboxes, radio buttons, text fields)
- Reuse the Eclipse color palette and visual concepts — no unnecessary custom colors/fonts
- Do not leave the root preference page empty; put frequently used settings there, details in sub-pages

**Common Violations — must be avoided**
- Poor or non-Eclipse-compliant graphics/icons
- Poorly organized or incorrectly sized dialogs/wizards
- Useless dialogs
- Cryptic error messages or messages with concatenated strings; always include context (actual type/value) in error messages
- Property pages that do not follow platform conventions
- Too many custom colors and fonts
- UI updates on the main thread (flooding/slow updates)
- Eclipse Perspectives: only include functionality relevant to the perspective

### JavaDoc & `{@inheritDoc}`
- **Do not use `{@inheritDoc}` alone** on `@Override` methods — Java/Eclipse inherits the documentation automatically since Java 6
- Remove existing `{@inheritDoc}` when touching those methods
- `{@inheritDoc}` is only allowed when additional text from the superclass is explicitly repeated in a new JavaDoc comment
- Document public API: return value semantics (especially null), URI formats, non-obvious contracts
- Internal methods only need JavaDoc if the contract is non-obvious

### NLS / Internationalization
- Add `//$NON-NLS-1$` to all non-externalized string literals in Eclipse plugin code
- Correct singular/plural forms; UTF-8 properties files
- Use domain-appropriate UI labels
- Derive message bundle paths from class references, not hardcoded strings — keep keys aligned with class names
- Use framework APIs for dates, locales, and formatting — never `Locale.getDefault()`

### Xtend
- Use property access syntax without `get()` — Xtend convention
- Apply constant folding where applicable
- Refactor adjacent Xtend code when touching a file — don't leave inconsistent style

### Testing
- Every new method/behavior **must** have a test, including edge cases (null, empty input, concurrent state, referenced repos) — don't wait to be asked
- Always use `assertThat` with Hamcrest matchers — never `assertEquals`/`assertTrue`/`assertFalse`; use `is()`, `hasItem()`, `containsString()`, etc.
- Test method names follow `testMethodName()` (lowercase); the name must clearly indicate what is being tested
- Any test that modifies global/shared state must restore the original value in `@AfterEach` or `try/finally`
- Tests must actually verify the right thing — assert on the correct field/instance, and verify progress across the whole operation
- Tests that create files, tables, or DB entries must clean up after themselves; verify cleanup works on all platforms
- Do not remove or significantly modify test code without explicit justification
- Do not mock where integration tests are required; understand the difference

### Code Quality & Best Practices

**Structure & Complexity**
- Code follows the existing patterns and conventions of the project
- No unnecessary complexity or over-engineering
- The change is focused — no unrelated modifications mixed in; create a separate ticket for unrelated fixes
- Every change in a diff must be intentional and explainable
- Reduce unnecessary method overrides — don't override if the override does nothing different from the superclass
- No dead/unnecessary code — no empty files, no unnecessary `throws`, no unused methods, no pass-through classes, no commented-out code

**Visibility & API**
- Default to `private`; only use `protected`/`public` when there is a concrete need
- API methods must go in interfaces, not only in implementation classes
- Return unmodifiable collections from getters — never expose internal mutable collections directly; wrap in `Collections.unmodifiableX()`
- If a method only needs to be visible for testing, use package-private (default) visibility

**Thread Safety**
- Never use static mutable state
- Consider concurrent access in all shared state
- Prefer instance-based state over static utility classes

**Error Handling**
- Never silently swallow unexpected states — at minimum log an error; don't just return `null` or skip
- Error severity must match reality: use `WARNING` if the system can continue; don't show errors when an operation succeeds
- SpotBugs and SonarQube warnings must be fixed before submitting

**Performance & Caching**
- Cache expensive lookups at construction/initialization time, not per call
- Hoist invariant computations out of loops and streams
- Cache invalidation must be precise — don't clear the whole cache; separate ticket if broader refactoring is needed
- No obvious performance pitfalls (e.g. N+1 queries, unbounded loops)
- Search methods are inefficient — must not be used in performance-critical locations (see also Getter/Finder/Searcher)

**Switches & Conditionals**
- Use explicit cases for enums so the compiler catches missing values — prefer exhaustive `switch` over long `if/else if` chains
- Use `case null` in switch statements (Java 21+) instead of a separate null check before the switch
- Simple conditions on one line: `if (condition) return value;`

**Constants & Types**
- Extract magic strings and values to constants; reuse existing constants where available
- Use `EnumSet` for enum collections; use `Comparator.comparing()` instead of manual ordering logic
- Use the right `Range` subclass (`IntegerRange`, `BigDecimalRange`, etc.) — don't use a generic `Range` when a specific subclass is correct
- Use exact equality, not `startsWith`/`contains` — consider prefix collisions when filtering by name

**Collections & Optionals**
- Don't wrap single values in lists unnecessarily
- Use `Optional` idiomatically: `ifPresent`, `map`, `orElse` — not `isPresent` + `get`
- Use `Optional.map()` instead of `stream()` on `Optional`
- Use `groupingBy` / `partitioningBy` for single-pass classification instead of iterating a collection multiple times

**Streams & Lambdas**
- Don't convert stream→list→stream; chain filters in a single pipeline
- One stream operation per line — each step should be independently readable
- Pass streams into internal methods instead of materializing to lists when possible
- Use lambdas and method references: `Predicate.not()`, `.toList()`, pass `Optional` directly
- Single-letter names are OK for one-liner lambdas; use meaningful parameter names in multi-line lambda blocks

**Duplication**
- If code appears 2+ times, extract into a helper method — always provide a concrete refactoring example
- If the same method is called 2+ times with the same result, cache in a local variable
- If a constant/property is used across modules, extract to a shared interface

**Naming**
- No generic variable names like `text1`/`text2`, `data`, `temp` — the name should answer "what does this contain?"
- Naming must be precise in domain context: "existing" means in the repository (not in the code), "empty" and "mandatory" are different concepts
- Sort new method overloads next to their related variants, not at random positions

**File Paths**
- Always `.normalize()` paths before comparing
- Test path handling on both Windows and Linux; use forward slashes

**Validation**
- Validation messages must only appear for real errors — avoid false positives; consider edge cases
- Don't over-validate: don't reject entire operations because of partial overlap — skip what's already handled, process the rest
- Use correct identifiers for comparison — qualified names, runtime IDs, and display names are different things

**Documentation**
- Update related documentation in the same change
- Use proper markup (`WARNING:`, `TIP:`) for important callouts
- Describe what IS, not what WILL BE; fix grammar; keep EN/DE consistent; version references must be accurate
- Use gendering with colon: „Benutzer:innen" — applies to all German UI text, documentation, and messages
- Product names remain in English

### Jenkins / CI
- Use `JENKINS_URL` — no hardcoded URLs
- No secret interpolation in pipeline scripts
- Replace all `/tmp` paths with proper workspace-relative paths
- Use `withMaven` for Maven builds; use `uploadDocumentation` and `f10-jenkins-library` patterns
- Use `H` for cron schedule hashing to distribute load
- Know what is in the build image — don't assume tools exist; product data must be in the container
