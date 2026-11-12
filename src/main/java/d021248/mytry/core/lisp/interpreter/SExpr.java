package d021248.mytry.core.lisp.interpreter;

import d021248.mytry.core.lisp.NumFormat;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/// S-Expression: the universal Lisp data type. Every value in the interpreter is an SExpr — code
// and
/// data share the same representation (homoiconicity).
///
/// **Truthiness:** Only `#f` and `()` are falsy. Everything else — including
/// `0`, empty strings, and all functions — is truthy. This follows R7RS semantics.
public sealed interface SExpr
    permits SExpr.Num, SExpr.Sym, SExpr.Bool, SExpr.Str, SExpr.SList, SExpr.Fn, SExpr.BuiltinFn {

  record Num(double value) implements SExpr {
    @Override
    public String toString() {
      return NumFormat.format(value);
    }
  }

  record Sym(String name) implements SExpr {
    @Override
    public String toString() {
      return name;
    }
  }

  record Bool(boolean value) implements SExpr {
    @Override
    public String toString() {
      return value ? "#t" : "#f";
    }
  }

  record Str(String value) implements SExpr {
    @Override
    public String toString() {
      return "\""
          + value
              .replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "\\r")
              .replace("\t", "\\t")
          + "\"";
    }
  }

  record SList(List<SExpr> items) implements SExpr {
    @Override
    public String toString() {
      return "(" + items.stream().map(Object::toString).collect(Collectors.joining(" ")) + ")";
    }
  }

  /// A user-defined function (lambda). Captures its defining environment (closure).
  record Fn(List<String> params, SExpr body, Env closure) implements SExpr {
    /// Defensively copies the parameter list so external mutation cannot affect the closure.
    public Fn {
      params = List.copyOf(params);
    }

    @Override
    public String toString() {
      return "<lambda:" + params + ">";
    }
  }

  /// A built-in function implemented in Java.
  record BuiltinFn(String name, BiFunction<List<SExpr>, Env, SExpr> impl) implements SExpr {
    @Override
    public String toString() {
      return "<builtin:" + name + ">";
    }
  }

  // ---- Factories ----
  static SExpr num(double v) {
    return new Num(v);
  }

  static SExpr sym(String n) {
    return new Sym(n);
  }

  static SExpr bool(boolean v) {
    return v ? TRUE : FALSE;
  }

  static SExpr str(String v) {
    return new Str(v);
  }

  static SExpr list(SExpr... items) {
    return new SList(List.of(items));
  }

  static SExpr list(List<SExpr> items) {
    return new SList(List.copyOf(items));
  }

  SExpr NIL = new SList(List.of());
  SExpr TRUE = new Bool(true);
  SExpr FALSE = new Bool(false);

  default boolean isTruthy() {
    return switch (this) {
      case Bool(boolean v) -> v;
      case SList(List<SExpr> items) -> !items.isEmpty();
      default -> true; // Num, Sym, Str, Fn, BuiltinFn — all truthy (R7RS)
    };
  }
}
