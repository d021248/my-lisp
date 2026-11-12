package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.symbolic.Expr.*;

/// Algebraic simplification of [Expr] expressions.
///
/// Applies bottom-up rewriting rules:
///
///
///   - Constant folding: `2 + 3 → 5`, `2 * 3 → 6`
///   - Additive identity: `0 + u → u`, `u + 0 → u`
///   - Multiplicative identity: `1 * u → u`, `u * 1 → u`
///   - Multiplicative zero: `0 * u → 0`, `u * 0 → 0`
///   - Power identities: `u^0 → 1`, `u^1 → u`
///
///
/// Simplification is applied recursively (bottom-up) and is idempotent: {@code
/// simplify(simplify(e)) == simplify(e)}.
public final class Simplifier {

  private Simplifier() {}

  /// Returns a simplified form of `expr`. Applies rules bottom-up: subexpressions are
  /// simplified first.
  public static Expr simplify(Expr expr) {
    return switch (expr) {
      case Expr.Num n -> n;
      case Expr.Var v -> v;
      case Expr.Add(Expr left, Expr right) -> simplifyAdd(simplify(left), simplify(right));
      case Expr.Mul(Expr left, Expr right) -> simplifyMul(simplify(left), simplify(right));
      case Expr.Pow(Expr base, double exp) -> simplifyPow(simplify(base), exp);
      // Transcendental: simplify argument, constant-fold if possible
      case Expr.Sin(Expr arg) -> {
        Expr s = simplify(arg);
        yield s instanceof Expr.Num(double v) ? Expr.num(Math.sin(v)) : Expr.sin(s);
      }
      case Expr.Cos(Expr arg) -> {
        Expr s = simplify(arg);
        yield s instanceof Expr.Num(double v) ? Expr.num(Math.cos(v)) : Expr.cos(s);
      }
      case Expr.Exp(Expr arg) -> {
        Expr s = simplify(arg);
        yield s instanceof Expr.Num(double v) ? Expr.num(Math.exp(v)) : Expr.exp(s);
      }
      case Expr.Ln(Expr arg) -> {
        Expr s = simplify(arg);
        yield s instanceof Expr.Num(double v) ? Expr.num(Math.log(v)) : Expr.ln(s);
      }
    };
  }

  // ---- Add rules -----------------------------------------------------------

  private static Expr simplifyAdd(Expr left, Expr right) {
    // constant folding: c1 + c2 → c3
    if (left instanceof Expr.Num(double a) && right instanceof Expr.Num(double b)) {
      return num(a + b);
    }
    // 0 + u → u
    if (left instanceof Expr.Num(double v) && v == 0) return right;
    // u + 0 → u
    if (right instanceof Expr.Num(double v) && v == 0) return left;
    return add(left, right);
  }

  // ---- Mul rules -----------------------------------------------------------

  private static Expr simplifyMul(Expr left, Expr right) {
    // constant folding: c1 * c2 → c3
    if (left instanceof Expr.Num(double a) && right instanceof Expr.Num(double b)) {
      return num(a * b);
    }
    // 0 * u → 0, u * 0 → 0
    if (left instanceof Expr.Num(double v) && v == 0) return ZERO;
    if (right instanceof Expr.Num(double v) && v == 0) return ZERO;
    // 1 * u → u
    if (left instanceof Expr.Num(double v) && v == 1) return right;
    // u * 1 → u
    if (right instanceof Expr.Num(double v) && v == 1) return left;
    return mul(left, right);
  }

  // ---- Pow rules -----------------------------------------------------------

  private static Expr simplifyPow(Expr base, double exp) {
    // c ^ n → constant
    if (base instanceof Expr.Num(double b)) return num(Math.pow(b, exp));
    // u^0 → 1
    if (exp == 0) return ONE;
    // u^1 → u
    if (exp == 1) return base;
    return pow(base, exp);
  }
}
