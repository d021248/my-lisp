# `d021248.mytry.core.lisp` — Lisp in Modern Java

A didactic exploration of Lisp concepts implemented with Java 22+ features:
sealed interfaces, records, pattern matching, unnamed variables, and Supplier-based laziness.

## Package structure

```
lisp/
├── list/           Immutable data structures (eager + lazy)
├── symbolic/       Symbolic math: differentiation, simplification, Taylor series
├── interpreter/    Mini Lisp interpreter with closures and builtins
└── demo/           Runnable demonstrations for each pillar
```

## `list/` — Persistent linked lists

| Class | Role |
|---|---|
| `LispList<T>` | Eager sealed ADT: `Nil`, `Cons`, `Iterable`, singleton Nil |
| `Lisp` | 20+ pure operations: car, cdr, map, filter, foldl, foldr, sort, ... |
| `LazyList<T>` | Lazy sealed ADT: tail is `Supplier<LazyList<T>>` |
| `Lazy` | Generators (`from`, `iterate`, `fibs`, `primes`) + lazy map/filter/zipWith |

```java
LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
show(map(list, x -> x * 2))              // (2 4 6)
show(take(primes(), 10))                  // (2 3 5 7 11 13 17 19 23 29)
```

## `symbolic/` — Symbolic mathematics

| Class | Role |
|---|---|
| `Expr` | Sealed ADT: Num, Var, Add, Mul, Pow |
| `Differentiator` | `diff(Expr, var)` — 5 calculus rules in one switch |
| `Simplifier` | Constant folding, neutral elements |
| `ExprEvaluator` | `eval(Expr, env)` — numerical evaluation |
| `ExprParser` | Infix parser with operator precedence |
| `Taylor` | Coefficients, polynomial reconstruction, convergence |

```java
Expr f  = parse("x^3 + 2*x^2 + x");
Expr df = simplify(diff(f, "x"));          // 3*x^2 + 4*x + 1
show(take(Taylor.derivatives(f, "x"), 4))  // [x^3+2x^2+x, 3x^2+4x+1, 6x+4, 6]
```

## `interpreter/` — Mini Lisp

| Class | Role |
|---|---|
| `SExpr` | Sealed ADT: Num, Sym, Bool, Str, SList, Fn, BuiltinFn |
| `SExprParser` | S-expression parser (quote, strings, comments) |
| `Env` | Lexical scope chain with mutable bindings |
| `LispInterpreter` | eval/apply + 12 special forms |
| `Builtins` | 35+ built-in functions |

```scheme
(define (fact n) (if (= n 0) 1 (* n (fact (- n 1)))))
(fact 10)  ; => 3628800

(map (lambda (x) (* x x)) '(1 2 3 4 5))  ; => (1 4 9 16 25)
```

## Design decisions

**`car` returns `Optional<T>`, `cdr` returns `LispList<T>`** — car has no meaningful
T for an empty list; cdr returns nil (the ADT encodes absence).

**`Nil` is a singleton** — no data, so all instances are identical after erasure.
Same pattern as `Collections.emptyList()`.

**`Nil` before `Cons` in `permits`** — a javac bootstrap quirk: the first permits
entry triggers member-type registration; subsequent entries rely on it.

**Operations live in utility classes, not on the ADT** — data and behaviour are
separated, mirroring FP conventions (typeclasses / modules).

**`reverse` is implemented via `foldl`** — demonstrates that higher-order operations
compose from primitives without additional recursion.

**`LazyList` tail is a `Supplier`** — exactly one wrapper separates eager from lazy.
Enables infinite sequences (primes, Fibonacci, Taylor coefficients).

**`Lazy.filter` skips iteratively** — avoids stack overflow on sparse infinite lists.

**Merge sort uses `take`/`drop` to split** — clarity over micro-optimisation.

**The interpreter uses `SExpr`, not `LispList` or `Expr`** — three ADTs at three
levels: typed lists, math expressions, untyped Lisp values. They coexist and
demonstrate the same patterns (sealed switch, structural recursion) at different
abstraction levels.
