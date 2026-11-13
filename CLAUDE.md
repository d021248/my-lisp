# CLAUDE.md — my-lisp

## Project Overview

Lisp interpreter in Java 25 — lexer, parser, evaluator, 40+ built-in functions,
lazy infinite lists, and symbolic differentiation with Taylor series.
Self-contained; no dependencies outside the JDK and test libraries.

Sibling repos: [my-try](../my-try) (core Java learning), [my-demos](../my-demos), [my-downloads](../my-downloads).

---

## Build & Test

```bash
mvn compile                          # compile only
mvn test                             # all tests
mvn test -Dtest=ClassName            # single test class
mvn test -Dtest="A,B,C"             # multiple test classes
```

Java version: **25** (`--enable-preview` active in compiler and surefire).

---

## Key Conventions

### Language
All code, comments, Javadoc, and `package-info.java` files must be in **English**.

### Naming
| Element | Convention |
|---|---|
| Utility class (static methods, no state) | `final` class, `private` constructor |
| Demo/example classes | `*Demo` or `*Example` suffix |

### Demo Classes

Two valid structures:

**A — Behaviour demo** (shows how to *use* an API):
```java
public final class FooDemo {
  public static void main() { }          // no String[] args
  private static void helper() {}       // helpers after main
}
```

**B — Concept demo** (defines the types it demonstrates):
```java
public final class BarDemo {
  record Pair<A, B>(A first, B second) {}  // inner types first
  public static void main() { }
}
```

Rules for both: `final` class, `main()` without `String[] args`.

### Documentation
- Use `///` Markdown Javadoc (Java 23+), not `/** */`
- Every `package` needs a `package-info.java`
- Every non-demo public class needs a class-level Javadoc comment
- Method Javadoc only on public API methods (skip overrides, private, demo code)
- `@since 1.0` on `package-info.java` blocks
- Keep `///` lines under ~100 chars (spotless wraps at ~100)

### Java 25 Style
- `var` where the type is obvious from the right-hand side
- Pattern-matching `instanceof`: `if (x instanceof Foo f)`
- `"...".formatted(...)` over `String.format` or `+` chains
- Never inline FQCNs — always `import`
- Records for immutable data; sealed classes for closed hierarchies
- Text blocks for multi-line strings

### Tests
- JUnit Jupiter 6.x (`@Test`, `@ParameterizedTest`, `@ValueSource`, `@MethodSource`)
- AssertJ for assertions (`assertThat(...)`)
- `@DisplayName` on every `@Test` — full sentence describing what is tested
- Test file mirrors source structure: `src/test/java/d021248/mytry/<same-path>/`
- Use records for test data carriers; text blocks for expected strings

---

## Package Map

```
src/main/java/d021248/mytry/
│
├── core/lisp/
│   ├── interpreter/   Lexer, parser, evaluator, Env, Builtins (40+ functions), SExpr types
│   ├── list/          Lazy infinite lists — fibs, primes, from, take, filter, map
│   ├── symbolic/      Symbolic differentiation, simplification, Taylor series expansion
│   └── demo/          InterpreterDemo, LazyListDemo, ListDemo, SymbolicDemo, TaylorDemo
│
└── core/lang/logic/   miniKanren logic programming — MiniKanren, Goal, State, Subst, Term,
                       MiniKanrenDemo; broken/ — BacktrackingWithExceptionsDemo
```

---

## Dependencies

| Artifact | Purpose |
|---|---|
| `junit-jupiter-api/engine/params` 6.1.3 | Test framework |
| `assertj-core` 3.27.7 | Fluent assertions |
