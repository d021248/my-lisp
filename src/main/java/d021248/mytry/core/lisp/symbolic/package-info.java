/// Symbolic mathematics: expressions as data, differentiation and evaluation as tree
/// transformations.
///
/// Contains:
///
///
///   - [d021248.mytry.core.lisp.symbolic.Expr] – sealed ADT for mathematical expressions
///       (`Num`, `Var`, `Add`, `Mul`, `Pow`)
///   - [d021248.mytry.core.lisp.symbolic.Differentiator] – symbolic differentiation via
///       exhaustive pattern matching over the `Expr` ADT
///   - [d021248.mytry.core.lisp.symbolic.Simplifier] – algebraic simplification (constant
///       folding, identity rules)
///   - [d021248.mytry.core.lisp.symbolic.ExprEvaluator] – numerical evaluation of {@code
///       Expr} trees given a variable environment
///   - [d021248.mytry.core.lisp.symbolic.ExprParser] – recursive-descent infix parser
///       producing `Expr` trees
///   - [d021248.mytry.core.lisp.symbolic.Taylor] – Taylor series expansion bridging symbolic
///       differentiation, numerical evaluation, and lazy lists
///
/// @since 1.0
package d021248.mytry.core.lisp.symbolic;
