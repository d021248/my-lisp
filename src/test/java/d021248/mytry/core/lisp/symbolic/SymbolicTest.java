package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.symbolic.Differentiator.diff;
import static d021248.mytry.core.lisp.symbolic.Expr.*;
import static d021248.mytry.core.lisp.symbolic.ExprEvaluator.eval;
import static d021248.mytry.core.lisp.symbolic.ExprParser.parse;
import static d021248.mytry.core.lisp.symbolic.Simplifier.simplify;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SymbolicTest {

  // =========================================================================
  // Differentiator
  // =========================================================================

  @Nested
  class DiffTest {

    @Test
    void constant_differentiates_toZero() {
      assertEquals(ZERO, diff(num(5), "x"));
    }

    @Test
    void variable_differentiates_toOne() {
      assertEquals(ONE, diff(var("x"), "x"));
    }

    @Test
    void otherVariable_differentiates_toZero() {
      assertEquals(ZERO, diff(var("y"), "x"));
    }

    @Test
    void sum_applies_sumRule() {
      // d/dx (x + x) = 1 + 1
      Expr result = simplify(diff(add(var("x"), var("x")), "x"));
      assertEquals(2.0, eval(result, Map.of("x", 0.0)));
    }

    @Test
    void product_applies_productRule() {
      // d/dx (x * x) = x*1 + 1*x = 2x
      Expr result = simplify(diff(mul(var("x"), var("x")), "x"));
      assertEquals(10.0, eval(result, Map.of("x", 5.0)));
    }

    @Test
    void power_applies_powerChainRule() {
      // d/dx (x^3) = 3*x^2
      Expr result = simplify(diff(pow(var("x"), 3), "x"));
      assertEquals(12.0, eval(result, Map.of("x", 2.0)));
    }

    @Test
    void secondDerivative_ofCubic() {
      // f = x^3, f' = 3x^2, f'' = 6x
      Expr f = pow(var("x"), 3);
      Expr f2 = simplify(diff(simplify(diff(f, "x")), "x"));
      assertEquals(18.0, eval(f2, Map.of("x", 3.0)));
    }
  }

  // =========================================================================
  // Simplifier
  // =========================================================================

  @Nested
  class SimplifyTest {

    @Test
    void addZeroLeft_simplifiesAway() {
      assertEquals(var("x"), simplify(add(num(0), var("x"))));
    }

    @Test
    void addZeroRight_simplifiesAway() {
      assertEquals(var("x"), simplify(add(var("x"), num(0))));
    }

    @Test
    void mulOneLeft_simplifiesAway() {
      assertEquals(var("x"), simplify(mul(num(1), var("x"))));
    }

    @Test
    void mulOneRight_simplifiesAway() {
      assertEquals(var("x"), simplify(mul(var("x"), num(1))));
    }

    @Test
    void mulZero_simplifiesAway() {
      assertEquals(ZERO, simplify(mul(num(0), var("x"))));
    }

    @Test
    void powZero_simplifiesToOne() {
      assertEquals(ONE, simplify(pow(var("x"), 0)));
    }

    @Test
    void powOne_simplifiesToBase() {
      assertEquals(var("x"), simplify(pow(var("x"), 1)));
    }

    @Test
    void constantFolding_add() {
      assertEquals(num(5.0), simplify(add(num(2), num(3))));
    }

    @Test
    void constantFolding_mul() {
      assertEquals(num(12.0), simplify(mul(num(3), num(4))));
    }

    @Test
    void idempotent_simplifyTwice() {
      Expr e = add(mul(num(0), var("x")), add(num(2), num(3)));
      assertEquals(simplify(e), simplify(simplify(e)));
    }
  }

  // =========================================================================
  // Evaluator
  // =========================================================================

  @Nested
  class EvalTest {

    @Test
    void evaluatesConstant() {
      assertEquals(42.0, eval(num(42), Map.of()));
    }

    @Test
    void evaluatesVariable() {
      assertEquals(7.0, eval(var("x"), Map.of("x", 7.0)));
    }

    @Test
    void evaluatesAdd() {
      assertEquals(5.0, eval(add(num(2), num(3)), Map.of()));
    }

    @Test
    void evaluatesMul() {
      assertEquals(12.0, eval(mul(num(3), num(4)), Map.of()));
    }

    @Test
    void evaluatesPow() {
      assertEquals(8.0, eval(pow(num(2), 3), Map.of()));
    }

    @Test
    void evaluatesComplex() {
      // 3^2 + 2*3 + 1 = 9 + 6 + 1 = 16
      Expr e = parse("x^2 + 2*x + 1");
      assertEquals(16.0, eval(e, Map.of("x", 3.0)));
    }

    @Test
    void unboundVariable_throws() {
      assertThrows(IllegalArgumentException.class, () -> eval(var("z"), Map.of()));
    }
  }

  // =========================================================================
  // ExprParser
  // =========================================================================

  @Nested
  class ParserTest {

    @Test
    void parsesNumber() {
      assertEquals(num(42), parse("42"));
    }

    @Test
    void parsesVariable() {
      assertEquals(var("x"), parse("x"));
    }

    @Test
    void parsesAddition() {
      Expr e = parse("x + 1");
      assertEquals(4.0, eval(e, Map.of("x", 3.0)));
    }

    @Test
    void parsesMultiplication() {
      Expr e = parse("2 * x");
      assertEquals(10.0, eval(e, Map.of("x", 5.0)));
    }

    @Test
    void parsesPower() {
      Expr e = parse("x^3");
      assertEquals(8.0, eval(e, Map.of("x", 2.0)));
    }

    @Test
    void respectsPrecedence() {
      // 1 + 2*3 = 7 (not 9)
      assertEquals(7.0, eval(parse("1 + 2 * 3"), Map.of()));
    }

    @Test
    void parsesParentheses() {
      // (1 + 2) * 3 = 9
      assertEquals(9.0, eval(parse("(1 + 2) * 3"), Map.of()));
    }

    @Test
    void parsesSubtraction() {
      assertEquals(-1.0, eval(parse("2 - 3"), Map.of()));
    }

    @Test
    void parsesNegation() {
      assertEquals(-5.0, eval(parse("-x"), Map.of("x", 5.0)));
    }

    @Test
    void parsesComplexExpression() {
      // 3*x^2 - 2*x + 1 at x=4 → 48 - 8 + 1 = 41
      assertEquals(41.0, eval(parse("3*x^2 - 2*x + 1"), Map.of("x", 4.0)));
    }
  }

  // =========================================================================
  // Full pipeline: numerical verification
  // =========================================================================

  @ParameterizedTest(name = "d/dx({0}) at x={1}")
  @CsvSource({
    "x,          1.0",
    "x^2,        3.0",
    "x^3,        2.0",
    "x^2 + 3*x,  4.0",
    "5*x^4,      1.0",
    "x*x*x,      2.0",
  })
  void symbolicDerivative_matchesNumerical(String input, double at) {
    Expr f = parse(input);
    Expr df = simplify(diff(f, "x"));
    double h = 1e-7;

    double symbolic = eval(df, Map.of("x", at));
    double numerical = (eval(f, Map.of("x", at + h)) - eval(f, Map.of("x", at))) / h;

    assertEquals(symbolic, numerical, 1e-4, () -> "f=" + input + " f'=" + df + " at x=" + at);
  }
}
