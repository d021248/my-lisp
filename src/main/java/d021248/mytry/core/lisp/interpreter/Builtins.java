package d021248.mytry.core.lisp.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/// Built-in functions and the default environment.
public final class Builtins {
  private Builtins() {}

  public static Env defaultEnv() {
    Map<String, SExpr> b = new HashMap<>();

    // ---- Arithmetic ----
    b.put("+", bi("+", (a, _) -> SExpr.num(a.stream().mapToDouble(Builtins::n).sum())));
    b.put(
        "-",
        bi(
            "-",
            (a, _) -> {
              requireAtLeast("-", a, 1);
              if (a.size() == 1) return SExpr.num(-n(a.getFirst()));
              double r = n(a.getFirst());
              for (int i = 1; i < a.size(); i++) r -= n(a.get(i));
              return SExpr.num(r);
            }));
    b.put(
        "*",
        bi(
            "*",
            (a, _) -> {
              double r = 1;
              for (var x : a) r *= n(x);
              return SExpr.num(r);
            }));
    b.put(
        "/",
        bi(
            "/",
            (a, _) -> {
              requireAtLeast("/", a, 1);
              if (a.size() == 1) return SExpr.num(1.0 / n(a.getFirst()));
              double r = n(a.getFirst());
              for (int i = 1; i < a.size(); i++) r /= n(a.get(i));
              return SExpr.num(r);
            }));
    b.put(
        "modulo",
        bi(
            "modulo",
            (a, _) -> {
              requireExact("modulo", a, 2);
              return SExpr.num(n(a.get(0)) % n(a.get(1)));
            }));

    // ---- Comparison ----
    b.put(
        "=",
        bi(
            "=",
            (a, _) -> {
              requireExact("=", a, 2);
              return SExpr.bool(n(a.get(0)) == n(a.get(1)));
            }));
    b.put(
        "<",
        bi(
            "<",
            (a, _) -> {
              requireExact("<", a, 2);
              return SExpr.bool(n(a.get(0)) < n(a.get(1)));
            }));
    b.put(
        ">",
        bi(
            ">",
            (a, _) -> {
              requireExact(">", a, 2);
              return SExpr.bool(n(a.get(0)) > n(a.get(1)));
            }));
    b.put(
        "<=",
        bi(
            "<=",
            (a, _) -> {
              requireExact("<=", a, 2);
              return SExpr.bool(n(a.get(0)) <= n(a.get(1)));
            }));
    b.put(
        ">=",
        bi(
            ">=",
            (a, _) -> {
              requireExact(">=", a, 2);
              return SExpr.bool(n(a.get(0)) >= n(a.get(1)));
            }));

    // ---- List operations ----
    b.put(
        "car",
        bi(
            "car",
            (a, _) -> {
              requireExact("car", a, 1);
              var items = l("car", a.getFirst()).items();
              if (items.isEmpty()) throw new LispError("car: empty list");
              return items.getFirst();
            }));
    b.put(
        "cdr",
        bi(
            "cdr",
            (a, _) -> {
              requireExact("cdr", a, 1);
              var items = l("cdr", a.getFirst()).items();
              if (items.isEmpty()) throw new LispError("cdr: empty list");
              return SExpr.list(items.subList(1, items.size()));
            }));
    b.put(
        "cons",
        bi(
            "cons",
            (a, _) -> {
              requireExact("cons", a, 2);
              var tail = l("cons", a.get(1)).items();
              List<SExpr> r = new ArrayList<>();
              r.add(a.getFirst());
              r.addAll(tail);
              return SExpr.list(r);
            }));
    b.put("list", bi("list", (a, _) -> SExpr.list(a)));
    b.put(
        "append",
        bi(
            "append",
            (a, _) -> {
              List<SExpr> r = new ArrayList<>();
              for (var x : a) r.addAll(l("append", x).items());
              return SExpr.list(r);
            }));
    b.put(
        "reverse",
        bi(
            "reverse",
            (a, _) -> {
              requireExact("reverse", a, 1);
              var items = new ArrayList<>(l("reverse", a.getFirst()).items());
              Collections.reverse(items);
              return SExpr.list(items);
            }));
    b.put(
        "length",
        bi(
            "length",
            (a, _) -> {
              requireExact("length", a, 1);
              return SExpr.num(l("length", a.getFirst()).items().size());
            }));
    b.put(
        "null?",
        bi(
            "null?",
            (a, _) -> {
              requireExact("null?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.SList(var it) && it.isEmpty());
            }));
    b.put(
        "pair?",
        bi(
            "pair?",
            (a, _) -> {
              requireExact("pair?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.SList(var it) && !it.isEmpty());
            }));

    // ---- Type predicates ----
    b.put(
        "number?",
        bi(
            "number?",
            (a, _) -> {
              requireExact("number?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.Num);
            }));
    b.put(
        "symbol?",
        bi(
            "symbol?",
            (a, _) -> {
              requireExact("symbol?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.Sym);
            }));
    b.put(
        "string?",
        bi(
            "string?",
            (a, _) -> {
              requireExact("string?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.Str);
            }));
    b.put(
        "boolean?",
        bi(
            "boolean?",
            (a, _) -> {
              requireExact("boolean?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.Bool);
            }));
    b.put(
        "list?",
        bi(
            "list?",
            (a, _) -> {
              requireExact("list?", a, 1);
              return SExpr.bool(a.getFirst() instanceof SExpr.SList);
            }));
    b.put(
        "procedure?",
        bi(
            "procedure?",
            (a, _) -> {
              requireExact("procedure?", a, 1);
              return SExpr.bool(
                  a.getFirst() instanceof SExpr.Fn || a.getFirst() instanceof SExpr.BuiltinFn);
            }));
    b.put(
        "not",
        bi(
            "not",
            (a, _) -> {
              requireExact("not", a, 1);
              return SExpr.bool(!a.getFirst().isTruthy());
            }));
    b.put(
        "equal?",
        bi(
            "equal?",
            (a, _) -> {
              requireExact("equal?", a, 2);
              return SExpr.bool(a.get(0).equals(a.get(1)));
            }));
    // eqv?: value equality for scalars, identity equality for everything else (R7RS
    // §6.1)
    b.put(
        "eqv?",
        bi(
            "eqv?",
            (a, _) -> {
              requireExact("eqv?", a, 2);
              SExpr x = a.get(0), y = a.get(1);
              return SExpr.bool(
                  switch (x) {
                    case SExpr.Num(double v) -> y instanceof SExpr.Num(double w) && v == w;
                    case SExpr.Bool(boolean v) -> y instanceof SExpr.Bool(boolean w) && v == w;
                    case SExpr.Sym(String v) -> y instanceof SExpr.Sym(String w) && v.equals(w);
                    case SExpr.Str(String v) -> y instanceof SExpr.Str(String w) && v.equals(w);
                    default -> x == y; // Fn, BuiltinFn, SList: reference identity
                  });
            }));
    // eq?: identity equality — same as eqv? for symbols/bools, reference for
    // everything else
    b.put(
        "eq?",
        bi(
            "eq?",
            (a, _) -> {
              requireExact("eq?", a, 2);
              SExpr x = a.get(0), y = a.get(1);
              return SExpr.bool(
                  switch (x) {
                    case SExpr.Bool(boolean v) -> y instanceof SExpr.Bool(boolean w) && v == w;
                    case SExpr.Sym(String v) -> y instanceof SExpr.Sym(String w) && v.equals(w);
                    default -> x == y;
                  });
            }));

    // ---- Math ----
    b.put(
        "abs",
        bi(
            "abs",
            (a, _) -> {
              requireExact("abs", a, 1);
              return SExpr.num(Math.abs(n(a.getFirst())));
            }));
    b.put(
        "sqrt",
        bi(
            "sqrt",
            (a, _) -> {
              requireExact("sqrt", a, 1);
              return SExpr.num(Math.sqrt(n(a.getFirst())));
            }));
    b.put(
        "expt",
        bi(
            "expt",
            (a, _) -> {
              requireExact("expt", a, 2);
              return SExpr.num(Math.pow(n(a.get(0)), n(a.get(1))));
            }));
    b.put(
        "min",
        bi(
            "min",
            (a, _) -> {
              requireAtLeast("min", a, 1);
              return SExpr.num(a.stream().mapToDouble(Builtins::n).min().orElseThrow());
            }));
    b.put(
        "max",
        bi(
            "max",
            (a, _) -> {
              requireAtLeast("max", a, 1);
              return SExpr.num(a.stream().mapToDouble(Builtins::n).max().orElseThrow());
            }));

    // ---- Higher-order ----
    // apply: (apply fn arg1 ... argN last-list)
    // All args before the last are prepended to the final list.
    // (apply + 1 2 '(3 4)) → (+ 1 2 3 4) → 10
    b.put(
        "apply",
        bi(
            "apply",
            (a, e) -> {
              requireAtLeast("apply", a, 2);
              SExpr fn = a.getFirst();
              // Last arg must be a list; everything between fn and last-arg are leading args.
              List<SExpr> leading = a.subList(1, a.size() - 1);
              List<SExpr> tail = l("apply", a.getLast()).items();
              List<SExpr> allArgs = new ArrayList<>(leading.size() + tail.size());
              allArgs.addAll(leading);
              allArgs.addAll(tail);
              return LispInterpreter.apply(fn, allArgs, e);
            }));
    // map supports multiple lists: (map f list1 list2 ...) — stops at shortest
    // list.
    b.put(
        "map",
        bi(
            "map",
            (a, e) -> {
              requireAtLeast("map", a, 2);
              var fn = a.getFirst();
              List<List<SExpr>> lists = new ArrayList<>();
              for (int i = 1; i < a.size(); i++) lists.add(l("map", a.get(i)).items());
              int len = lists.stream().mapToInt(List::size).min().orElse(0);
              List<SExpr> r = new ArrayList<>(len);
              for (int i = 0; i < len; i++) {
                List<SExpr> fnArgs = new ArrayList<>(lists.size());
                for (var lst : lists) fnArgs.add(lst.get(i));
                r.add(LispInterpreter.apply(fn, fnArgs, e));
              }
              return SExpr.list(r);
            }));
    b.put(
        "filter",
        bi(
            "filter",
            (a, e) -> {
              requireExact("filter", a, 2);
              var fn = a.getFirst();
              var items = l("filter", a.get(1)).items();
              List<SExpr> r = new ArrayList<>();
              for (var x : items) if (LispInterpreter.apply(fn, List.of(x), e).isTruthy()) r.add(x);
              return SExpr.list(r);
            }));
    b.put(
        "reduce",
        bi(
            "reduce",
            (a, e) -> {
              requireExact("reduce", a, 3);
              var fn = a.get(0);
              var init = a.get(1);
              var items = l("reduce", a.get(2)).items();
              SExpr acc = init;
              for (var x : items) acc = LispInterpreter.apply(fn, List.of(acc, x), e);
              return acc;
            }));

    b.put(
        "for-each",
        bi(
            "for-each",
            (a, e) -> {
              requireAtLeast("for-each", a, 2);
              var fn = a.getFirst();
              var items = l("for-each", a.get(1)).items();
              for (var x : items) LispInterpreter.apply(fn, List.of(x), e);
              return SExpr.NIL;
            }));

    // ---- Strings ----
    b.put(
        "string-length",
        bi(
            "string-length",
            (a, _) -> {
              requireExact("string-length", a, 1);
              return SExpr.num(s("string-length", a.getFirst()).length());
            }));
    b.put(
        "string-append",
        bi(
            "string-append",
            (a, _) -> {
              var sb = new StringBuilder();
              for (var x : a) sb.append(s("string-append", x));
              return SExpr.str(sb.toString());
            }));
    b.put(
        "substring",
        bi(
            "substring",
            (a, _) -> {
              requireAtLeast("substring", a, 2);
              String str = s("substring", a.get(0));
              int start = (int) n(a.get(1));
              int end = a.size() > 2 ? (int) n(a.get(2)) : str.length();
              if (start < 0 || end > str.length() || start > end)
                throw new LispError("substring: index out of range [" + start + ", " + end + ")");
              return SExpr.str(str.substring(start, end));
            }));
    b.put(
        "string-ref",
        bi(
            "string-ref",
            (a, _) -> {
              requireExact("string-ref", a, 2);
              String str = s("string-ref", a.get(0));
              int idx = (int) n(a.get(1));
              if (idx < 0 || idx >= str.length())
                throw new LispError(
                    "string-ref: index "
                        + idx
                        + " out of range for string of length "
                        + str.length());
              return SExpr.str(String.valueOf(str.charAt(idx)));
            }));
    b.put(
        "string-upcase",
        bi(
            "string-upcase",
            (a, _) -> {
              requireExact("string-upcase", a, 1);
              return SExpr.str(s("string-upcase", a.getFirst()).toUpperCase(Locale.ROOT));
            }));
    b.put(
        "string-downcase",
        bi(
            "string-downcase",
            (a, _) -> {
              requireExact("string-downcase", a, 1);
              return SExpr.str(s("string-downcase", a.getFirst()).toLowerCase(Locale.ROOT));
            }));
    b.put(
        "string->number",
        bi(
            "string->number",
            (a, _) -> {
              requireExact("string->number", a, 1);
              try {
                return SExpr.num(Double.parseDouble(s("string->number", a.getFirst())));
              } catch (NumberFormatException _) {
                return SExpr.FALSE;
              }
            }));
    b.put(
        "number->string",
        bi(
            "number->string",
            (a, _) -> {
              requireExact("number->string", a, 1);
              return SExpr.str(a.getFirst().toString());
            }));
    b.put(
        "string->symbol",
        bi(
            "string->symbol",
            (a, _) -> {
              requireExact("string->symbol", a, 1);
              return SExpr.sym(s("string->symbol", a.getFirst()));
            }));
    b.put(
        "symbol->string",
        bi(
            "symbol->string",
            (a, _) -> {
              requireExact("symbol->string", a, 1);
              if (!(a.getFirst() instanceof SExpr.Sym(String name)))
                throw new LispError("symbol->string: expected symbol, got: " + a.getFirst());
              return SExpr.str(name);
            }));
    b.put(
        "string=?",
        bi(
            "string=?",
            (a, _) -> {
              requireExact("string=?", a, 2);
              return SExpr.bool(s("string=?", a.get(0)).equals(s("string=?", a.get(1))));
            }));
    b.put(
        "string<?",
        bi(
            "string<?",
            (a, _) -> {
              requireExact("string<?", a, 2);
              return SExpr.bool(s("string<?", a.get(0)).compareTo(s("string<?", a.get(1))) < 0);
            }));
    b.put(
        "string>?",
        bi(
            "string>?",
            (a, _) -> {
              requireExact("string>?", a, 2);
              return SExpr.bool(s("string>?", a.get(0)).compareTo(s("string>?", a.get(1))) > 0);
            }));
    b.put(
        "string-contains",
        bi(
            "string-contains",
            (a, _) -> {
              requireExact("string-contains", a, 2);
              int idx = s("string-contains", a.get(0)).indexOf(s("string-contains", a.get(1)));
              return idx >= 0 ? SExpr.num(idx) : SExpr.FALSE;
            }));
    b.put(
        "string-split",
        bi(
            "string-split",
            (a, _) -> {
              requireExact("string-split", a, 2);
              String[] parts =
                  s("string-split", a.get(0)).split(Pattern.quote(s("string-split", a.get(1))), -1);
              List<SExpr> r = new ArrayList<>(parts.length);
              for (var p : parts) r.add(SExpr.str(p));
              return SExpr.list(r);
            }));

    // ---- Error ----
    b.put(
        "error",
        bi(
            "error",
            (a, _) -> {
              String msg =
                  a.isEmpty()
                      ? "error"
                      : a.stream()
                          .map(v -> v instanceof SExpr.Str(String s) ? s : v.toString())
                          .reduce((x, y) -> x + " " + y)
                          .orElse("");
              throw new LispError(msg);
            }));

    // ---- I/O ----
    b.put(
        "display",
        bi(
            "display",
            (a, _) -> {
              requireExact("display", a, 1);
              var v = a.getFirst();
              System.out.print(v instanceof SExpr.Str(String s) ? s : v.toString());
              return SExpr.NIL;
            }));
    b.put(
        "newline",
        bi(
            "newline",
            (a, _) -> {
              System.out.println();
              return SExpr.NIL;
            }));

    return new Env(b, null);
  }

  // ---- Helpers ----

  private static SExpr.BuiltinFn bi(String nm, BiFunction<List<SExpr>, Env, SExpr> impl) {
    return new SExpr.BuiltinFn(nm, impl);
  }

  /// Throws [LispError] unless `args` has exactly `n` elements.
  private static void requireExact(String name, List<SExpr> args, int n) {
    if (args.size() != n)
      throw new LispError(name + ": expected " + n + " arg(s), got " + args.size());
  }

  /// Throws [LispError] unless `args` has at least `min` elements.
  private static void requireAtLeast(String name, List<SExpr> args, int min) {
    if (args.size() < min)
      throw new LispError(name + ": expected at least " + min + " arg(s), got " + args.size());
  }

  static double n(SExpr e) {
    if (e instanceof SExpr.Num(double v)) return v;
    throw new LispError("Expected number, got: " + e);
  }

  /// Extracts a string value, giving a caller-aware error on type mismatch.
  static String s(String caller, SExpr e) {
    if (e instanceof SExpr.Str(String v)) return v;
    throw new LispError(caller + ": expected string, got: " + e);
  }

  /// Extracts a list, giving a name-aware error message on type mismatch.
  static SExpr.SList l(String caller, SExpr e) {
    if (e instanceof SExpr.SList s) return s;
    throw new LispError(caller + ": expected list, got: " + e);
  }

  /// Backwards-compat overload used by other classes (e.g. tests).
  static SExpr.SList l(SExpr e) {
    return l("?", e);
  }
}
