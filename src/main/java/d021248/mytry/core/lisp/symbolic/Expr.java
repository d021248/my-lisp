package d021248.mytry.core.lisp.symbolic;

import d021248.mytry.core.lisp.NumFormat;

/// Sealed expression ADT for symbolic mathematics.
///
/// Every mathematical expression is one of nine variants:
///
///
///   - [Num] — a numeric literal (e.g. 3.0)
///   - [Var] — a variable (e.g. "x")
///   - [Add] — a sum of two expressions
///   - [Mul] — a product of two expressions
///   - [Pow] — an expression raised to a constant exponent
///   - [Sin] — sine of an expression
///   - [Cos] — cosine of an expression
///   - [Exp] — natural exponential `e^u`
///   - [Ln] — natural logarithm `ln(u)`
///
///
/// The sealed modifier guarantees exhaustive pattern matching in every `switch` over {@code
/// Expr} — the compiler enforces that all cases are handled.
///
/// Factory methods provide a concise fluent API for building expressions.
public sealed interface Expr
    permits Expr.Num,
        Expr.Var,
        Expr.Add,
        Expr.Mul,
        Expr.Pow,
        Expr.Sin,
        Expr.Cos,
        Expr.Exp,
        Expr.Ln {

  /// A numeric constant.
  record Num(double value) implements Expr {
    @Override
    public String toString() {
      return NumFormat.format(value);
    }
  }

  /// A named variable.
  record Var(String name) implements Expr {
    @Override
    public String toString() {
      return name;
    }
  }

  /// Sum of two expressions: `left + right`.
  record Add(Expr left, Expr right) implements Expr {
    @Override
    public String toString() {
      return "(" + left + " + " + right + ")";
    }
  }

  /// Product of two expressions: `left * right`.
  record Mul(Expr left, Expr right) implements Expr {
    @Override
    public String toString() {
      return "(" + left + " * " + right + ")";
    }
  }

  /// Power: `base ^ exponent` where exponent is a constant.
  record Pow(Expr base, double exponent) implements Expr {
    @Override
    public String toString() {
      return "(" + base + " ^ " + NumFormat.format(exponent) + ")";
    }
  }

  /// Sine: `sin(arg)`.
  record Sin(Expr arg) implements Expr {
    @Override
    public String toString() {
      return "sin(" + arg + ")";
    }
  }

  /// Cosine: `cos(arg)`.
  record Cos(Expr arg) implements Expr {
    @Override
    public String toString() {
      return "cos(" + arg + ")";
    }
  }

  /// Natural exponential: `e^arg`.
  record Exp(Expr arg) implements Expr {
    @Override
    public String toString() {
      return "exp(" + arg + ")";
    }
  }

  /// Natural logarithm: `ln(arg)`. Domain: arg > 0.
  record Ln(Expr arg) implements Expr {
    @Override
    public String toString() {
      return "ln(" + arg + ")";
    }
  }

  // ---- Factory methods (fluent API) ----------------------------------------

  static Expr num(double value) {
    return new Num(value);
  }

  static Expr var(String name) {
    return new Var(name);
  }

  static Expr add(Expr left, Expr right) {
    return new Add(left, right);
  }

  static Expr mul(Expr left, Expr right) {
    return new Mul(left, right);
  }

  static Expr pow(Expr base, double exp) {
    return new Pow(base, exp);
  }

  static Expr sin(Expr arg) {
    return new Sin(arg);
  }

  static Expr cos(Expr arg) {
    return new Cos(arg);
  }

  static Expr exp(Expr arg) {
    return new Exp(arg);
  }

  static Expr ln(Expr arg) {
    return new Ln(arg);
  }

  // ---- Common constants ----------------------------------------------------

  Expr ZERO = num(0);
  Expr ONE = num(1);
  Expr E = num(Math.E); // Euler's number
}
