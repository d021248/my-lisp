# my-lisp

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/projects/jdk/25/)
[![Build](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)](https://maven.apache.org/)
[![CI](https://github.com/d021248/my-lisp/actions/workflows/ci.yml/badge.svg)](https://github.com/d021248/my-lisp/actions/workflows/ci.yml)
[![Coverage](https://codecov.io/gh/d021248/my-lisp/branch/main/graph/badge.svg)](https://codecov.io/gh/d021248/my-lisp)

A Lisp interpreter written in Java 25 — lexer, parser, evaluator, 40+ built-in functions,
lazy infinite lists, and symbolic differentiation with Taylor series expansion.

Part of the [`d021248`](https://github.com/d021248) learning suite alongside
[my-try](https://github.com/d021248/my-try) · [my-demos](https://github.com/d021248/my-demos) · [my-downloads](https://github.com/d021248/my-downloads).

---

## Features

- **Interpreter** — full Lisp evaluator: `lambda`, `define`, `let`, `cond`, `quasiquote`, tail calls, closures
- **40+ built-ins** — arithmetic, list operations (`car`, `cdr`, `cons`, `map`, `filter`, `reduce`), I/O, predicates
- **Lazy infinite lists** — `fibs`, `primes`, `from`, `take`, `filter`, `map` backed by virtual threads
- **Symbolic math** — differentiation, simplification, Taylor series (`sin x`, `cos x`, `e^x`, ...)
- **No external dependencies** — pure JDK + test libraries

---

## Build & Test

```bash
mvn compile              # compile
mvn test                 # run 174 tests
mvn test -Dtest=LispTest # single test class
```

Java 25 with `--enable-preview`. Spotless (Google Java Format) runs on every compile.

---

## Running the Demos

```bash
# Interactive Lisp REPL
mvn exec:java -Dexec.mainClass="d021248.mytry.core.lisp.LispRepl"

# Interpreter demo (eval expressions, show results)
mvn exec:java -Dexec.mainClass="d021248.mytry.core.lisp.demo.InterpreterDemo"

# Lazy infinite lists (fibs, primes)
mvn exec:java -Dexec.mainClass="d021248.mytry.core.lisp.demo.LazyListDemo"

# Symbolic differentiation + simplification
mvn exec:java -Dexec.mainClass="d021248.mytry.core.lisp.demo.SymbolicDemo"

# Taylor series expansion
mvn exec:java -Dexec.mainClass="d021248.mytry.core.lisp.demo.TaylorDemo"
```

---

## Structure

```
src/
├── main/java/d021248/mytry/core/lisp/
│   ├── interpreter/   Lexer → SExprParser → LispInterpreter, Env, Builtins
│   ├── list/          Lazy, LazyList, Lisp, LispList — lazy infinite sequences
│   ├── symbolic/      Expr, Differentiator, Simplifier, Taylor, ExprEvaluator
│   └── demo/          Five runnable demos
└── test/java/d021248/mytry/core/lisp/
    ├── interpreter/   InterpreterTest (core eval, 40+ builtins)
    ├── list/          LazyTest, LispTest
    └── symbolic/      SymbolicTest, TaylorTest
```

---

## Example

```lisp
;; Fibonacci via lazy list
(define fibs (lazy-fibs 0 1))
(take 10 fibs)   ; => (0 1 1 2 3 5 8 13 21 34)

;; Symbolic differentiation
(diff '(* x x) 'x)   ; => (* 2 x)
(diff '(sin x) 'x)   ; => (cos x)
```

---

## Dependencies

| Artifact | Version | Purpose |
|---|---|---|
| `junit-jupiter-api/engine/params` | 6.1.3 | Test framework |
| `assertj-core` | 3.27.7 | Fluent assertions |
