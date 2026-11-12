package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.symbolic.Expr.*;

/// Symbolic differentiation of [Expr] expressions.
///
/// Implements the standard rules of calculus:
///
///
///   - d/dx c = 0 (constant rule)
///   - d/dx x = 1 (identity rule)
///   - d/dx (u + v) = du + dv (sum rule)
///   - d/dx (u * v) = u*dv + du*v (product rule)
///   - d/dx (u ^ n) = n * u^(n-1) * du (power / chain rule)
///   - d/dx sin(u) = cos(u) * du (chain rule)
///   - d/dx cos(u) = -sin(u) * du (chain rule)
///   - d/dx exp(u) = exp(u) * du (chain rule)
///   - d/dx ln(u) = (1/u) * du = u^(-1) * du (chain rule)
///
///
/// The result is structurally correct but not simplified — apply [Simplifier#simplify] to
/// reduce it to a canonical form.
public final class Differentiator {

  private Differentiator() {}

  /// Returns the symbolic derivative of `expr` with respect to `var`.
  ///
  /// @param expr the expression to differentiate
  /// @param var the variable name to differentiate with respect to
  /// @return the derivative as an unsimplified [Expr]
  public static Expr diff(Expr expr, String var) {
    return switch (expr) {

      // d/dx c = 0
      case Expr.Num _ -> ZERO;

      // d/dx x = 1, d/dx y = 0
      case Expr.Var(String name) -> name.equals(var) ? ONE : ZERO;

      // d/dx (u + v) = du + dv
      case Expr.Add(Expr u, Expr v) -> add(diff(u, var), diff(v, var));

      // d/dx (u * v) = u*dv + du*v
      case Expr.Mul(Expr u, Expr v) -> add(mul(u, diff(v, var)), mul(diff(u, var), v));

      // d/dx (u^n) = n * u^(n-1) * du (chain rule)
      case Expr.Pow(Expr u, double n) -> mul(mul(num(n), pow(u, n - 1)), diff(u, var));

      // d/dx sin(u) = cos(u) * du
      case Expr.Sin(Expr u) -> mul(cos(u), diff(u, var));

      // d/dx cos(u) = -sin(u) * du = (-1 * sin(u)) * du
      case Expr.Cos(Expr u) -> mul(mul(num(-1), sin(u)), diff(u, var));

      // d/dx exp(u) = exp(u) * du
      case Expr.Exp(Expr u) -> mul(exp(u), diff(u, var));

      // d/dx ln(u) = (1 / u) * du = u^(-1) * du
      case Expr.Ln(Expr u) -> mul(pow(u, -1), diff(u, var));
    };
  }
}
