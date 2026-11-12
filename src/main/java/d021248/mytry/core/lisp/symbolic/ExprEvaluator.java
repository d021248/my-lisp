package d021248.mytry.core.lisp.symbolic;

import java.util.Map;

/// Numerical evaluation of [Expr] expressions.
///
/// Replaces every [Expr.Var] with the corresponding value from an environment {@code
/// Map<String, Double>} and computes the resulting number.
///
/// @throws IllegalArgumentException if a variable is not found in the environment
public final class ExprEvaluator {

  private ExprEvaluator() {}

  /// Evaluates `expr` under the given variable bindings.
  ///
  /// @param expr the expression to evaluate
  /// @param env variable bindings, e.g. `Map.of("x", 3.0)`
  /// @return the numerical result
  public static double eval(Expr expr, Map<String, Double> env) {
    return switch (expr) {
      case Expr.Num(double value) -> value;

      case Expr.Var(String name) -> {
        Double value = env.get(name);
        if (value == null) throw new IllegalArgumentException("Unbound variable: " + name);
        yield value;
      }

      case Expr.Add(Expr left, Expr right) -> eval(left, env) + eval(right, env);

      case Expr.Mul(Expr left, Expr right) -> eval(left, env) * eval(right, env);

      case Expr.Pow(Expr base, double exp) -> Math.pow(eval(base, env), exp);

      case Expr.Sin(Expr arg) -> Math.sin(eval(arg, env));

      case Expr.Cos(Expr arg) -> Math.cos(eval(arg, env));

      case Expr.Exp(Expr arg) -> Math.exp(eval(arg, env));

      case Expr.Ln(Expr arg) -> {
        double v = eval(arg, env);
        if (v <= 0) throw new IllegalArgumentException("ln: argument must be positive, got: " + v);
        yield Math.log(v);
      }
    };
  }
}
