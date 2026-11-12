package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.symbolic.Differentiator.diff;
import static d021248.mytry.core.lisp.symbolic.Expr.*;
import static d021248.mytry.core.lisp.symbolic.ExprEvaluator.eval;
import static d021248.mytry.core.lisp.symbolic.Simplifier.simplify;

import d021248.mytry.core.lisp.list.Lazy;
import d021248.mytry.core.lisp.list.LazyList;
import d021248.mytry.core.lisp.list.LispList;
import java.util.Map;

/// Taylor series expansion — the bridge between all three pillars of this package.
///
/// Uses [Differentiator] (symbolic), [ExprEvaluator] (numerical), and {@link
/// LazyList} (infinite sequences) to produce Taylor coefficients and polynomials.
///
/// The Taylor series of `f(x)` around point `a` is:
///
/// ```
///   f(a) + f'(a)(x-a)/1! + f''(a)(x-a)²/2! + f'''(a)(x-a)³/3! + ...
/// ```
public final class Taylor {

  private Taylor() {}

  // -------------------------------------------------------------------------
  // Symbolic: infinite list of derivatives
  // -------------------------------------------------------------------------

  /// Returns the infinite lazy list of successive derivatives of `f`: {@code f, f', f'', f''',
  /// ...}
  ///
  /// Each derivative is simplified before being stored. The list is *infinite*: for
  /// polynomial expressions the derivatives eventually reach `0` and remain there, but the
  /// list itself never terminates. Use [Lazy#take] to materialise a finite prefix.
  ///
  /// Example: `derivatives(x³, "x") → [x³, 3*x², 6*x, 6, 0, 0, ...]`
  public static LazyList<Expr> derivatives(Expr f, String var) {
    return LazyList.cons(f, () -> derivatives(simplify(diff(f, var)), var));
  }

  // -------------------------------------------------------------------------
  // Numerical: infinite list of Taylor coefficients
  // -------------------------------------------------------------------------

  /// Returns the infinite lazy list of Taylor coefficients of `f` around `a`: {@code
  /// f(a)/0!, f'(a)/1!, f''(a)/2!, ...}
  ///
  /// The n-th element is `f⁽ⁿ⁾(a) / n!`.
  ///
  /// Delegates to [#derivatives] so the differentiation/simplification logic lives in one
  /// place. Previously this method duplicated the derivative computation independently.
  ///
  /// Example: `take(coefficients(x³, "x", 0.0), 5) → (0.0, 0.0, 0.0, 1.0, 0.0)`
  public static LazyList<Double> coefficients(Expr f, String var, double a) {
    return coeffsFromDerivatives(derivatives(f, var), var, a, 0);
  }

  private static LazyList<Double> coeffsFromDerivatives(
      LazyList<Expr> derivs, String var, double a, int n) {
    return switch (derivs) {
      case LazyList.Nil<Expr> _ -> LazyList.nil();
      case LazyList.Cons<Expr>(Expr deriv, var rest) -> {
        // Evaluate the n-th derivative at 'a' and divide by n! to get the Taylor
        // coefficient.
        double coeff = eval(deriv, Map.of(var, a)) / factorial(n);
        yield LazyList.cons(coeff, () -> coeffsFromDerivatives(rest.get(), var, a, n + 1));
      }
    };
  }

  // -------------------------------------------------------------------------
  // Evaluation: approximate f(x) using n terms
  // -------------------------------------------------------------------------

  /// Evaluates the Taylor polynomial of degree `terms - 1` at point `x`:
  ///
  /// ```
  /// Σ (k=0..terms-1) c_k * (x - a)^k
  /// ```
  ///
  /// Example: for `f = x³`, `evaluate(f, "x", 0.0, 2.0, 4)` computes `0 + 0*2 +
  /// 0*4 + 1*8 = 8.0`.
  ///
  /// @param f the expression
  /// @param var the variable name
  /// @param a the expansion point
  /// @param x the evaluation point
  /// @param terms number of Taylor terms to use
  /// @return the approximated value
  public static double evaluate(Expr f, String var, double a, double x, int terms) {
    LispList<Double> coeffs = Lazy.take(coefficients(f, var, a), terms);
    double result = 0.0;
    double power = 1.0; // (x - a)^k, starting at k=0
    double dx = x - a;
    for (double c : coeffs) {
      result += c * power;
      power *= dx;
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Symbolic: build the Taylor polynomial as an Expr
  // -------------------------------------------------------------------------

  /// Returns the Taylor polynomial of degree `terms - 1` as a symbolic [Expr]:
  ///
  /// ```
  /// Σ (k=0..terms-1) c_k * (x - a)^k
  /// ```
  ///
  /// Example: `polynomial(x³ + 2*x² + x, "x", 0.0, 4)` returns the simplified expression
  /// `x + 2*x² + x³` — reconstructing the original polynomial from its Taylor coefficients.
  public static Expr polynomial(Expr f, String var, double a, int terms) {
    LispList<Double> coeffs = Lazy.take(coefficients(f, var, a), terms);
    Expr xVar = var(var);
    Expr xMinusA = a == 0.0 ? xVar : add(xVar, num(-a));
    Expr result = ZERO;
    int k = 0;
    for (double c : coeffs) {
      if (c != 0.0) {
        Expr term;
        if (k == 0) term = num(c);
        else if (k == 1) term = mul(num(c), xMinusA);
        else term = mul(num(c), pow(xMinusA, k));
        result = result.equals(ZERO) ? term : add(result, term);
      }
      k++;
    }
    return simplify(result);
  }

  // -------------------------------------------------------------------------
  // Utility
  // -------------------------------------------------------------------------

  private static double factorial(int n) {
    double result = 1.0;
    for (int i = 2; i <= n; i++) result *= i;
    return result;
  }
}
