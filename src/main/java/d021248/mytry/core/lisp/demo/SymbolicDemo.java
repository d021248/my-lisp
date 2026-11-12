package d021248.mytry.core.lisp.demo;

import static d021248.mytry.core.lisp.symbolic.Differentiator.diff;
import static d021248.mytry.core.lisp.symbolic.ExprEvaluator.eval;
import static d021248.mytry.core.lisp.symbolic.ExprParser.parse;
import static d021248.mytry.core.lisp.symbolic.Simplifier.simplify;

import d021248.mytry.core.lisp.symbolic.Expr;
import java.util.Locale;
import java.util.Map;

/// Demonstrates the full symbolic differentiation pipeline: parse → differentiate → simplify → show
/// / eval.
public final class SymbolicDemo {

  public static void main() {
    System.out.println("=== Symbolic Differentiation Demo ===\n");

    // ---- Polynomial: x^3 + 2*x^2 + x ----------------------------------------
    demo("x^3 + 2*x^2 + x", "x", 2.0);

    // ---- Simple product: x * x -----------------------------------------------
    demo("x * x", "x", 5.0);

    // ---- Multivariate: x^2 * y -----------------------------------------------
    System.out.println("--- f(x,y) = x^2 * y ---");
    Expr f = parse("x^2 * y");
    Expr dfdx = simplify(diff(f, "x"));
    Expr dfdy = simplify(diff(f, "y"));
    Map<String, Double> env = Map.of("x", 3.0, "y", 4.0);
    System.out.println("  f       = " + f);
    System.out.println("  df/dx   = " + dfdx);
    System.out.println("  df/dy   = " + dfdy);
    System.out.println("  f(3,4)  = " + eval(f, env));
    System.out.println("  df/dx(3,4) = " + eval(dfdx, env));
    System.out.println("  df/dy(3,4) = " + eval(dfdy, env));
    System.out.println();

    // ---- Higher-order derivative: d²/dx² of x^4 ------------------------------
    System.out.println("--- f(x) = x^4, second derivative ---");
    Expr g = parse("x^4");
    Expr g1 = simplify(diff(g, "x"));
    Expr g2 = simplify(diff(g1, "x"));
    System.out.println("  f       = " + g);
    System.out.println("  f'      = " + g1);
    System.out.println("  f''     = " + g2);
    System.out.println("  f''(2)  = " + eval(g2, Map.of("x", 2.0)));
    System.out.println();

    // ---- Numerical verification -----------------------------------------------
    System.out.println("--- Numerical verification ---");
    verify("x^3", "x", 2.0);
    verify("x^2 + 3*x + 1", "x", 4.0);
    verify("5*x^4", "x", 1.0);
  }

  /// Parses, differentiates, simplifies, evaluates, and prints a single-variable expression.
  private static void demo(String input, String var, double at) {
    Expr f = parse(input);
    Expr df = simplify(diff(f, var));
    Map<String, Double> env = Map.of(var, at);

    System.out.println("--- f(" + var + ") = " + input + " ---");
    System.out.println("  f       = " + f);
    System.out.println("  f'      = " + df);
    System.out.printf(Locale.ROOT, "  f(%.1f)  = %.4f%n", at, eval(f, env));
    System.out.printf(Locale.ROOT, "  f'(%.1f) = %.4f%n", at, eval(df, env));
    System.out.println();
  }

  /// Compares symbolic derivative with numerical approximation (finite difference).
  private static void verify(String input, String var, double at) {
    Expr f = parse(input);
    Expr df = simplify(diff(f, var));
    double h = 1e-7;

    double symbolic = eval(df, Map.of(var, at));
    double numerical = (eval(f, Map.of(var, at + h)) - eval(f, Map.of(var, at))) / h;

    System.out.printf(Locale.ROOT, "  f(x) = %-20s  f'(x) = %-20s  at x=%.1f%n", input, df, at);
    System.out.printf(Locale.ROOT, "  symbolic:  %.6f%n", symbolic);
    System.out.printf(Locale.ROOT, "  numerical: %.6f  (h=%.0e)%n", numerical, h);
    System.out.printf(Locale.ROOT, "  delta:     %.2e%n%n", Math.abs(symbolic - numerical));
  }
}
