package d021248.mytry.core.lisp.demo;

import static d021248.mytry.core.lisp.list.Lisp.show;
import static d021248.mytry.core.lisp.symbolic.Expr.*;
import static d021248.mytry.core.lisp.symbolic.ExprParser.parse;
import static d021248.mytry.core.lisp.symbolic.Taylor.*;

import d021248.mytry.core.lisp.list.Lazy;
import d021248.mytry.core.lisp.list.Lisp;
import d021248.mytry.core.lisp.list.LispList;
import d021248.mytry.core.lisp.symbolic.Expr;
import d021248.mytry.core.lisp.symbolic.ExprEvaluator;
import java.util.Locale;
import java.util.Map;

/// Demonstrates Taylor series expansion — the point where all three pillars meet: {@link
/// d021248.mytry.core.lisp.list.LazyList} (infinite sequences), {@link
/// d021248.mytry.core.lisp.symbolic.Differentiator} (symbolic), {@link
/// d021248.mytry.core.lisp.symbolic.ExprEvaluator} (numerical).
public final class TaylorDemo {

  public static void main() {

    // ---- 1. Infinite list of derivatives ------------------------------------

    System.out.println("=== Successive derivatives of x³ ===");
    Expr f1 = parse("x^3");
    // Take first 6 derivatives: x³, 3x², 6x, 6, 0, 0
    LispList<Expr> derivs = Lazy.take(derivatives(f1, "x"), 6);
    int n = 0;
    for (Expr d : derivs) {
      System.out.printf(Locale.ROOT, "  f%s(x) = %s%n", "'".repeat(n++), d);
    }

    // ---- 2. Taylor coefficients at a=0 --------------------------------------

    System.out.println("\n=== Taylor coefficients of x³ + 2x² + x at a=0 ===");
    Expr f2 = parse("x^3 + 2*x^2 + x");
    LispList<Double> coeffs = Lazy.take(coefficients(f2, "x", 0.0), 5);
    System.out.println("  coefficients: " + show(Lisp.map(coeffs, c -> c)));
    // (0, 1, 2, 1, 0) → the polynomial is 0 + 1x + 2x² + 1x³

    // ---- 3. Reconstruct polynomial from Taylor coefficients -----------------

    System.out.println("\n=== Reconstruct polynomial via Taylor ===");
    Expr reconstructed = polynomial(f2, "x", 0.0, 5);
    System.out.println("  original:      " + f2);
    System.out.println("  reconstructed: " + reconstructed);

    // ---- 4. Taylor expansion at a=1 -----------------------------------------

    System.out.println("\n=== Taylor expansion of x³ + 2x² + x at a=1 ===");
    LispList<Double> coeffsAt1 = Lazy.take(coefficients(f2, "x", 1.0), 5);
    System.out.println("  coefficients at a=1: " + show(Lisp.map(coeffsAt1, c -> c)));
    // f(1)=4, f'(1)=8, f''(1)=10, f'''(1)=6 → 4, 8, 5, 1
    Expr polyAt1 = polynomial(f2, "x", 1.0, 4);
    System.out.println("  T₃(x) at a=1: " + polyAt1);

    // ---- 5. Numerical verification ------------------------------------------

    System.out.println("\n=== Numerical verification: f(x) = x³ + 2x² + x at x=2 ===");
    double exact = ExprEvaluator.eval(f2, Map.of("x", 2.0));
    double approx4 = evaluate(f2, "x", 0.0, 2.0, 4);
    double approx3 = evaluate(f2, "x", 0.0, 2.0, 3);
    System.out.printf(Locale.ROOT, "  exact:     %.4f%n", exact);
    System.out.printf(Locale.ROOT, "  4 terms:   %.4f  (exact for degree-3 poly)%n", approx4);
    System.out.printf(Locale.ROOT, "  3 terms:   %.4f  (missing x³ term)%n", approx3);

    // ---- 6. Higher-degree polynomial approximation --------------------------

    System.out.println("\n=== Convergence: (1+x)^5 at x=0.5, increasing terms ===");
    Expr f3 = pow(add(num(1), var("x")), 5);
    double exactVal = ExprEvaluator.eval(f3, Map.of("x", 0.5));
    System.out.printf(Locale.ROOT, "  exact: %.6f%n", exactVal);
    for (int k = 1; k <= 6; k++) {
      double approx = evaluate(f3, "x", 0.0, 0.5, k);
      System.out.printf(
          Locale.ROOT,
          "  %d term%s: %.6f  (error: %.2e)%n",
          k,
          k == 1 ? " " : "s",
          approx,
          Math.abs(exactVal - approx));
    }
  }
}
