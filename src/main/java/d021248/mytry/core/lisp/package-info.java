/// Lisp in Modern Java — from data structures to a working interpreter.
///
/// This package demonstrates how classical Lisp/FP concepts map onto Java 22+ language features,
/// organised in four subpackages that build on each other:
///
/// ## Subpackages
///
/// ### `list` — Immutable persistent lists
///
///
///   - [d021248.mytry.core.lisp.list.LispList] — eager list ADT (sealed interface + records)
///   - [d021248.mytry.core.lisp.list.Lisp] — car, cdr, map, filter, foldl, foldr, sort, ...
///   - [d021248.mytry.core.lisp.list.LazyList] — lazy list with `Supplier` tail
///   - [d021248.mytry.core.lisp.list.Lazy] — from, iterate, fibs, primes, ...
///
///
/// ### `symbolic` — Symbolic mathematics
///
///
///   - [d021248.mytry.core.lisp.symbolic.Expr] — expression ADT
///   - [d021248.mytry.core.lisp.symbolic.Differentiator] — symbolic differentiation
///   - [d021248.mytry.core.lisp.symbolic.Simplifier] — algebraic simplification
///   - [d021248.mytry.core.lisp.symbolic.ExprEvaluator] — numerical evaluation
///   - [d021248.mytry.core.lisp.symbolic.Taylor] — Taylor series expansion
///
///
/// ### `interpreter` — Mini Lisp interpreter
///
///
///   - [d021248.mytry.core.lisp.interpreter.SExpr] — S-expression ADT (code = data)
///   - [d021248.mytry.core.lisp.interpreter.LispInterpreter] — eval / apply
///   - [d021248.mytry.core.lisp.interpreter.Builtins] — 40+ built-in functions
///   - [d021248.mytry.core.lisp.interpreter.Env] — lexical environments
///
///
/// ### `demo` — Runnable demonstrations
///
///
///   - [d021248.mytry.core.lisp.demo.ListDemo] — eager list operations
///   - [d021248.mytry.core.lisp.demo.LazyListDemo] — infinite sequences, Sieve of
///       Eratosthenes
///   - [d021248.mytry.core.lisp.demo.SymbolicDemo] — differentiation pipeline
///   - [d021248.mytry.core.lisp.demo.TaylorDemo] — Taylor series convergence
///   - [d021248.mytry.core.lisp.demo.InterpreterDemo] — Lisp programs: factorial, fibonacci,
///       closures
///
/// @since 1.0
package d021248.mytry.core.lisp;
