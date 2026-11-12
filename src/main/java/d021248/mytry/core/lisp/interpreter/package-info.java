/// Mini Lisp interpreter — the metacircular demonstration.
///
/// Evaluates S-expressions with lexical scoping, closures, and a rich set of builtins.
///
/// Special forms: `define`, `lambda`, `let`, `let*`, `letrec`,
/// `if`, `cond`, `and`, `or`, `quote`, `begin`, `set!`
///
/// Core classes:
///
///
///   - [d021248.mytry.core.lisp.interpreter.SExpr] — sealed ADT: code and data
///   - [d021248.mytry.core.lisp.interpreter.SExprParser] — S-expression parser
///   - [d021248.mytry.core.lisp.interpreter.Env] — lexical environment chain
///   - [d021248.mytry.core.lisp.interpreter.LispInterpreter] — eval / apply
///   - [d021248.mytry.core.lisp.interpreter.Builtins] — 40+ built-in functions
///
/// @since 1.0
package d021248.mytry.core.lisp.interpreter;
