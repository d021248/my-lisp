package d021248.mytry.core.lisp;

import d021248.mytry.core.lisp.interpreter.Builtins;
import d021248.mytry.core.lisp.interpreter.Env;
import d021248.mytry.core.lisp.interpreter.LispError;
import d021248.mytry.core.lisp.interpreter.LispInterpreter;
import d021248.mytry.core.lisp.interpreter.SExpr;
import d021248.mytry.core.lisp.interpreter.SExprParser;
import d021248.mytry.core.lisp.symbolic.Differentiator;
import d021248.mytry.core.lisp.symbolic.Expr;
import d021248.mytry.core.lisp.symbolic.ExprEvaluator;
import d021248.mytry.core.lisp.symbolic.ExprParser;
import d021248.mytry.core.lisp.symbolic.Simplifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/// Interactive REPL bridging the Lisp interpreter with the symbolic math engine.
///
/// Extra builtins: `(diff "e" "v")`, `(eval-at "e" "v" n)`, `(simplify "e")`.
///
/// Short forms (no parens): `diff expr`, `diff expr wrt var`, {@code eval at
/// var=num}.
public final class LispRepl {

  private LispRepl() {}

  /// Launches the interactive REPL.
  ///
  /// @param args ignored
  /// @throws IOException if stdin cannot be read
  public static void main() throws IOException {
    Env env = buildEnv();
    String[] lastExpr = {"x"};

    System.out.println("LispRepl  --  Lisp + Symbolic Math");
    System.out.println("Builtins: (diff \"e\" \"v\")  (eval-at \"e\" \"v\" n)  (simplify \"e\")");
    System.out.println("Short-form: diff <expr>   eval at x=1.5   (exit) to quit");
    System.out.println();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      while (true) {
        System.out.print("l> ");
        System.out.flush();
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        line = line.strip();
        if (line.isEmpty()) {
          continue;
        }
        if (line.equals("(exit)") || line.equals("exit")) {
          break;
        }

        // Short-form: eval at var=num (uses last parsed expression)
        if (line.startsWith("eval at ") && line.contains("=")) {
          int eqIdx = line.indexOf('=');
          String var = line.substring("eval at ".length(), eqIdx).strip();
          String numStr = line.substring(eqIdx + 1).strip();
          try {
            double val = Double.parseDouble(numStr);
            Expr parsed = ExprParser.parse(lastExpr[0]);
            double result = ExprEvaluator.eval(parsed, Map.of(var, val));
            System.out.printf(
                "  f(%s=%s) = %s%n", var, NumFormat.format(val), NumFormat.format(result));
          } catch (Exception e) {
            System.out.println("  error: " + e.getMessage());
          }
          continue;
        }

        // Short-form: diff <expr> [wrt <var>]
        if (!line.startsWith("(") && line.startsWith("diff ")) {
          String rest = line.substring("diff ".length()).strip();
          String var = "x";
          String exprStr = rest;
          if (rest.contains(" wrt ")) {
            int wrtIdx = rest.lastIndexOf(" wrt ");
            var = rest.substring(wrtIdx + 5).strip();
            exprStr = rest.substring(0, wrtIdx).strip();
          }
          try {
            Expr parsed = ExprParser.parse(exprStr);
            lastExpr[0] = exprStr;
            Expr derivative = Simplifier.simplify(Differentiator.diff(parsed, var));
            System.out.printf("  d/d%s: %s%n", var, derivative);
          } catch (Exception e) {
            System.out.println("  error: " + e.getMessage());
          }
          continue;
        }

        // Standard Lisp
        try {
          List<SExpr> exprs = SExprParser.parseAll(line);
          SExpr last = null;
          for (SExpr expr : exprs) {
            last = LispInterpreter.eval(expr, env);
          }
          if (last != null) {
            System.out.println("  -> " + last);
          }
        } catch (LispError e) {
          System.out.println("  error: " + e.getMessage());
        } catch (Exception e) {
          System.out.println("  error: " + e.getMessage());
        }
      }
    }
    System.out.println("bye.");
  }

  /// Returns the default Lisp environment extended with symbolic math builtins.
  ///
  /// @return the augmented [Env]
  private static Env buildEnv() {
    Env env = Builtins.defaultEnv();

    // (diff "expr" "var") -> simplified symbolic derivative as a string
    env.define(
        "diff",
        new SExpr.BuiltinFn(
            "diff",
            (fnArgs, ignored) -> {
              requireExact("diff", fnArgs, 2);
              String exprStr = asStr(fnArgs.get(0), "diff", 1);
              String var = asStr(fnArgs.get(1), "diff", 2);
              Expr parsed = ExprParser.parse(exprStr);
              Expr derivative = Simplifier.simplify(Differentiator.diff(parsed, var));
              return SExpr.str(derivative.toString());
            }));

    // (eval-at "expr" "var" num) -> numeric result
    env.define(
        "eval-at",
        new SExpr.BuiltinFn(
            "eval-at",
            (fnArgs, ignored) -> {
              requireExact("eval-at", fnArgs, 3);
              String exprStr = asStr(fnArgs.get(0), "eval-at", 1);
              String var = asStr(fnArgs.get(1), "eval-at", 2);
              double val = asNum(fnArgs.get(2), "eval-at", 3);
              Expr parsed = ExprParser.parse(exprStr);
              double result = ExprEvaluator.eval(parsed, Map.of(var, val));
              return SExpr.num(result);
            }));

    // (simplify "expr") -> simplified expr as string
    env.define(
        "simplify",
        new SExpr.BuiltinFn(
            "simplify",
            (fnArgs, ignored) -> {
              requireExact("simplify", fnArgs, 1);
              String exprStr = asStr(fnArgs.get(0), "simplify", 1);
              Expr parsed = ExprParser.parse(exprStr);
              return SExpr.str(Simplifier.simplify(parsed).toString());
            }));

    return env;
  }

  // ---- helpers ----

  private static void requireExact(String fn, List<SExpr> args, int n) {
    if (args.size() != n) {
      throw new LispError(fn + ": expected " + n + " argument(s), got " + args.size());
    }
  }

  private static String asStr(SExpr s, String fn, int argPos) {
    if (!(s instanceof SExpr.Str strVal)) {
      throw new LispError(fn + ": argument " + argPos + " must be a string, got " + s);
    }
    return strVal.value();
  }

  private static double asNum(SExpr s, String fn, int argPos) {
    if (!(s instanceof SExpr.Num numVal)) {
      throw new LispError(fn + ": argument " + argPos + " must be a number, got " + s);
    }
    return numVal.value();
  }
}
