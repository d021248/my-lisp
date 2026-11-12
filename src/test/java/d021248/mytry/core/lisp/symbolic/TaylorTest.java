package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.list.Lisp.show;
import static d021248.mytry.core.lisp.symbolic.Expr.*;
import static d021248.mytry.core.lisp.symbolic.ExprEvaluator.eval;
import static d021248.mytry.core.lisp.symbolic.ExprParser.parse;
import static d021248.mytry.core.lisp.symbolic.Taylor.*;
import static org.junit.jupiter.api.Assertions.*;

import d021248.mytry.core.lisp.list.Lazy;
import d021248.mytry.core.lisp.list.LispList;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaylorTest {

  // -------------------------------------------------------------------------
  // derivatives
  // -------------------------------------------------------------------------

  @Test
  void derivatives_xCubed_producesCorrectSequence() {
    Expr f = parse("x^3");
    LispList<Expr> derivs = Lazy.take(derivatives(f, "x"), 5);
    // x³ → 3x² → 6x → 6 → 0
    int n = 0;
    double[] expectedAtX2 = {8.0, 12.0, 12.0, 6.0, 0.0};
    for (Expr d : derivs) {
      assertEquals(
          expectedAtX2[n],
          eval(d, Map.of("x", 2.0)),
          1e-10,
          "derivative #" + n + " evaluated at x=2");
      n++;
    }
  }

  @Test
  void derivatives_constant_allZeroAfterFirst() {
    Expr f = num(42);
    LispList<Expr> derivs = Lazy.take(derivatives(f, "x"), 4);
    double[] expected = {42.0, 0.0, 0.0, 0.0};
    int n = 0;
    for (Expr d : derivs) {
      assertEquals(expected[n], eval(d, Map.of("x", 0.0)), 1e-10);
      n++;
    }
  }

  // -------------------------------------------------------------------------
  // coefficients
  // -------------------------------------------------------------------------

  @Test
  void coefficients_xCubed_atZero_isZeroZeroZeroOne() {
    Expr f = parse("x^3");
    LispList<Double> coeffs = Lazy.take(coefficients(f, "x", 0.0), 5);
    assertEquals("(0.0 0.0 0.0 1.0 0.0)", show(coeffs));
  }

  @Test
  void coefficients_polynomial_atZero_matchesCoefficients() {
    // f(x) = x³ + 2x² + x → coefficients at 0: 0, 1, 2, 1, 0
    Expr f = parse("x^3 + 2*x^2 + x");
    LispList<Double> coeffs = Lazy.take(coefficients(f, "x", 0.0), 5);
    assertEquals("(0.0 1.0 2.0 1.0 0.0)", show(coeffs));
  }

  @Test
  void coefficients_atNonZero_areCorrect() {
    // f(x) = x³ + 2x² + x at a=1:
    // f(1)=4, f'(1)=8, f''(1)/2!=5, f'''(1)/3!=1
    Expr f = parse("x^3 + 2*x^2 + x");
    LispList<Double> coeffs = Lazy.take(coefficients(f, "x", 1.0), 5);
    assertEquals("(4.0 8.0 5.0 1.0 0.0)", show(coeffs));
  }

  // -------------------------------------------------------------------------
  // evaluate (numerical Taylor approximation)
  // -------------------------------------------------------------------------

  @Test
  void evaluate_polynomial_exactWithEnoughTerms() {
    // Degree-3 polynomial needs exactly 4 terms
    Expr f = parse("x^3 + 2*x^2 + x");
    double exact = eval(f, Map.of("x", 2.0));
    double taylor = evaluate(f, "x", 0.0, 2.0, 4);
    assertEquals(exact, taylor, 1e-10);
  }

  @Test
  void evaluate_polynomial_atExpansionPoint_isExact() {
    Expr f = parse("x^3 + 2*x^2 + x");
    // At the expansion point itself, even 1 term suffices: f(a)
    assertEquals(4.0, evaluate(f, "x", 1.0, 1.0, 1), 1e-10);
  }

  @Test
  void evaluate_convergence_moretermsMoreAccurate() {
    // (1+x)^5 at x=0.5, expanded at a=0
    Expr f = pow(add(num(1), var("x")), 5);
    double exact = eval(f, Map.of("x", 0.5));
    double prev = Double.MAX_VALUE;
    for (int terms = 1; terms <= 6; terms++) {
      double error = Math.abs(exact - evaluate(f, "x", 0.0, 0.5, terms));
      assertTrue(error <= prev, "error should decrease: terms=" + terms);
      prev = error;
    }
    // With 6 terms, exact for degree-5 poly
    assertEquals(exact, evaluate(f, "x", 0.0, 0.5, 6), 1e-10);
  }

  // -------------------------------------------------------------------------
  // polynomial (symbolic reconstruction)
  // -------------------------------------------------------------------------

  @Test
  void polynomial_atZero_reconstructsOriginal() {
    Expr f = parse("x^3 + 2*x^2 + x");
    Expr poly = polynomial(f, "x", 0.0, 4);
    // The reconstructed polynomial should evaluate identically
    for (double x = -2; x <= 2; x += 0.5) {
      assertEquals(
          eval(f, Map.of("x", x)), eval(poly, Map.of("x", x)), 1e-10, "mismatch at x=" + x);
    }
  }

  @Test
  void polynomial_atNonZero_evaluatesCorrectly() {
    Expr f = parse("x^3 + 2*x^2 + x");
    Expr poly = polynomial(f, "x", 1.0, 4);
    // Should agree with f at several points
    for (double x = -1; x <= 3; x += 0.5) {
      assertEquals(
          eval(f, Map.of("x", x)), eval(poly, Map.of("x", x)), 1e-10, "mismatch at x=" + x);
    }
  }
}
