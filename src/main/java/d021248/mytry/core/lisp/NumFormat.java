package d021248.mytry.core.lisp;

/// Shared formatting utility for numeric values used across the `lisp` package.
///
/// Both `SExpr.Num` (interpreter) and `Expr.Num` (symbolic) use the same display
/// rule: if the double value is integer-valued (e.g. `3.0`), print it without a decimal point
/// (`"3"`); otherwise use [Double#toString] (`"3.14"`).
public final class NumFormat {

  private NumFormat() {}

  /// Formats a `double` as an integer string if it is integer-valued, otherwise as a decimal
  /// string.
  ///
  /// Examples: `3.0 → "3"`, `3.14 → "3.14"`, `-2.0 → "-2"`.
  public static String format(double value) {
    return value == (long) value ? Long.toString((long) value) : Double.toString(value);
  }
}
