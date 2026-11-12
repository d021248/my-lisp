package d021248.mytry.core.lisp.interpreter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Mini Lisp interpreter with tail-call optimisation (TCO).
///
/// Supports: define, lambda, let, let*, letrec, if, cond, and, or, quote, quasiquote, begin, set!
///
/// **TCO via trampoline:** The tail position of every special form and every user-defined
/// function call updates the local `expr`/`env` variables and loops instead of
/// recursing. This makes tail-recursive programs run in O(1) stack space:
///
/// ```java
/// (define (loop n) (if (= n 0) 'done (loop (- n 1))))
/// (loop 1000000)   ; → done — no StackOverflowError
/// ```
///
/// **Note:** `and`/`or` are not TCO'd for their last expression — they are rarely
/// the bottleneck in tail-recursive programs.
public final class LispInterpreter {

  private LispInterpreter() {}

  /// Private record used to thread the body expression and its lexical environment through
  /// let/letrec preparation without recursing into eval.
  private record BodyEnv(SExpr body, Env env) {}

  // ---- Main eval loop (trampoline) ----

  /// Evaluates `initExpr` in `initEnv`.
  ///
  /// The while-loop is the trampoline: tail-position cases update `expr`/`env` and
  /// `continue` instead of recursing. Non-tail cases `return` immediately.
  public static SExpr eval(SExpr initExpr, Env initEnv) {
    SExpr expr = initExpr;
    Env env = initEnv;
    while (true) {
      switch (expr) {

        // ---- Self-evaluating ----
        case SExpr.Num _, SExpr.Bool _, SExpr.Str _, SExpr.Fn _, SExpr.BuiltinFn _ -> {
          return expr;
        }

        // ---- Variable lookup (never a tail call in practice) ----
        case SExpr.Sym(String name) -> {
          return env.lookup(name);
        }

        // ---- List: special forms and function application ----
        case SExpr.SList(List<SExpr> items) -> {
          if (items.isEmpty()) return SExpr.NIL;
          SExpr head = items.getFirst();
          List<SExpr> tail = items.subList(1, items.size());

          // Special-form dispatch (symbol head only)
          if (head instanceof SExpr.Sym(String name)) {
            switch (name) {

              // ---- Non-tail special forms — return immediately ----

              case "quote" -> {
                requireArity("quote", tail, 1);
                return tail.getFirst();
              }
              case "quasiquote" -> {
                requireArity("quasiquote", tail, 1);
                return expandQuasiquote(tail.getFirst(), env);
              }
              case "define" -> {
                return evalDefine(tail, env);
              }
              case "set!" -> {
                return evalSet(tail, env);
              }
              case "lambda" -> {
                return evalLambda(tail, env);
              }
              case "and" -> {
                return evalAnd(tail, env);
              }
              case "or" -> {
                return evalOr(tail, env);
              }

              // ---- Tail-position special forms — update expr/env and loop (TCO) ----

              case "if" -> {
                if (tail.size() < 2) throw new LispError("if: expected at least 2 subforms");
                boolean cond = eval(tail.get(0), env).isTruthy();
                expr = cond ? tail.get(1) : (tail.size() > 2 ? tail.get(2) : SExpr.NIL);
                continue; // TCO
              }
              case "cond" -> {
                SExpr next = evalCondNext(tail, env);
                if (next == null) return SExpr.NIL;
                expr = next;
                continue; // TCO
              }
              case "begin" -> {
                if (tail.isEmpty()) return SExpr.NIL;
                for (int i = 0; i < tail.size() - 1; i++) eval(tail.get(i), env);
                expr = tail.getLast();
                continue; // TCO: last expression in tail position
              }
              case "let" -> {
                BodyEnv be = letPrep(tail, env);
                expr = be.body();
                env = be.env();
                continue; // TCO
              }
              case "let*" -> {
                BodyEnv be = letStarPrep(tail, env);
                expr = be.body();
                env = be.env();
                continue; // TCO
              }
              case "letrec" -> {
                BodyEnv be = letrecPrep(tail, env);
                expr = be.body();
                env = be.env();
                continue; // TCO
              }

                // Not a special form: fall through to function application below.
            }
            // A symbol name that didn't match any special form reaches here.
          }

          // ---- Function application ----
          // Reached for: non-special-form symbol heads, and non-symbol heads.
          SExpr fn = eval(head, env);
          List<SExpr> evArgs = new ArrayList<>(tail.size());
          for (SExpr a : tail) evArgs.add(eval(a, env));

          switch (fn) {
            case SExpr.Fn(List<String> params, SExpr body, Env closure) -> {
              if (params.size() != evArgs.size())
                throw new LispError("Expected " + params.size() + " args, got " + evArgs.size());
              LinkedHashMap<String, SExpr> bindings = new LinkedHashMap<>();
              for (int i = 0; i < params.size(); i++) bindings.put(params.get(i), evArgs.get(i));
              expr = body;
              env = closure.child(bindings);
              // TCO: do not return — fall through to the next while-loop iteration.
            }
            case SExpr.BuiltinFn(_, var impl) -> {
              return impl.apply(evArgs, env);
            }
            default -> throw new LispError("Not a function: " + fn);
          }
        }
      }
    }
  }

  /// Applies `fn` to `args`. Used by built-ins (`map`, `filter`, etc.) that
  /// need to call back into user-defined functions.
  ///
  /// Note: apply() itself recurses into eval() and is therefore not TCO'd. Built-ins are not in
  /// tail position of the outer program, so this is correct.
  public static SExpr apply(SExpr fn, List<SExpr> args, Env env) {
    return switch (fn) {
      case SExpr.Fn(List<String> params, SExpr body, Env closure) -> {
        if (params.size() != args.size())
          throw new LispError("Expected " + params.size() + " args, got " + args.size());
        Map<String, SExpr> bindings = new LinkedHashMap<>();
        for (int i = 0; i < params.size(); i++) bindings.put(params.get(i), args.get(i));
        yield eval(body, closure.child(bindings));
      }
      case SExpr.BuiltinFn(_, var impl) -> impl.apply(args, env);
      default -> throw new LispError("Not a function: " + fn);
    };
  }

  // ---- Special forms (non-tail: return immediately) ----

  private static SExpr evalDefine(List<SExpr> args, Env env) {
    if (args.isEmpty()) throw new LispError("define: missing name");
    SExpr first = args.getFirst();
    if (first instanceof SExpr.Sym(String name)) {
      if (args.size() < 2) throw new LispError("define: missing value for " + name);
      return env.define(name, eval(args.get(1), env));
    }
    if (first instanceof SExpr.SList(List<SExpr> nameAndParams) && !nameAndParams.isEmpty()) {
      if (!(nameAndParams.getFirst() instanceof SExpr.Sym(String name)))
        throw new LispError("define: function name must be a symbol: " + nameAndParams.getFirst());
      List<String> params = extractParamNames(nameAndParams.subList(1, nameAndParams.size()));
      SExpr body = makeBody(args.subList(1, args.size()));
      return env.define(name, new SExpr.Fn(params, body, env));
    }
    throw new LispError("define: expected symbol or (name params...), got: " + first);
  }

  private static SExpr evalSet(List<SExpr> args, Env env) {
    if (args.size() != 2) throw new LispError("set!: expected 2 subforms, got " + args.size());
    if (!(args.getFirst() instanceof SExpr.Sym(String name)))
      throw new LispError("set!: first argument must be a symbol: " + args.getFirst());
    return env.set(name, eval(args.get(1), env));
  }

  private static SExpr evalLambda(List<SExpr> args, Env env) {
    if (args.size() < 2) throw new LispError("lambda: expected params and body");
    if (!(args.getFirst() instanceof SExpr.SList(List<SExpr> paramExprs)))
      throw new LispError("lambda: parameter list must be a list, got: " + args.getFirst());
    List<String> params = extractParamNames(paramExprs);
    SExpr body = makeBody(args.subList(1, args.size()));
    return new SExpr.Fn(params, body, env);
  }

  private static SExpr evalAnd(List<SExpr> args, Env env) {
    SExpr r = SExpr.TRUE;
    for (SExpr a : args) {
      r = eval(a, env);
      if (!r.isTruthy()) return r;
    }
    return r;
  }

  private static SExpr evalOr(List<SExpr> args, Env env) {
    for (SExpr a : args) {
      SExpr r = eval(a, env);
      if (r.isTruthy()) return r;
    }
    return SExpr.FALSE;
  }

  // ---- cond: returns the expression to evaluate in tail position ----

  /// Finds the first matching `cond` clause and returns its body as a single expression for
  /// TCO. Returns `null` if no clause matches (cond returns `#nil`).
  private static SExpr evalCondNext(List<SExpr> clauses, Env env) {
    for (SExpr clause : clauses) {
      if (!(clause instanceof SExpr.SList(List<SExpr> pair)) || pair.isEmpty())
        throw new LispError("cond: invalid clause: " + clause);
      SExpr test = pair.getFirst();
      List<SExpr> body = pair.subList(1, pair.size());
      if (test instanceof SExpr.Sym(String n) && n.equals("else")) return makeBody(body);
      SExpr testVal = eval(test, env);
      if (testVal.isTruthy()) return body.isEmpty() ? testVal : makeBody(body);
    }
    return null;
  }

  // ---- let/let*/letrec: return BodyEnv pairs for TCO ----

  private static BodyEnv letPrep(List<SExpr> args, Env env) {
    if (args.size() < 2) throw new LispError("let: expected bindings and body");
    var bindingsList = requireBindingsList("let", args.getFirst());
    Map<String, SExpr> bindings = new LinkedHashMap<>();
    for (SExpr b : bindingsList) {
      var p = parseBindingPair("let", b);
      bindings.put(p.getKey(), eval(p.getValue(), env));
    }
    return new BodyEnv(makeBody(args.subList(1, args.size())), env.child(bindings));
  }

  private static BodyEnv letStarPrep(List<SExpr> args, Env env) {
    if (args.size() < 2) throw new LispError("let*: expected bindings and body");
    var bindingsList = requireBindingsList("let*", args.getFirst());
    Env cur = env;
    for (SExpr b : bindingsList) {
      var p = parseBindingPair("let*", b);
      cur = cur.child(Map.of(p.getKey(), eval(p.getValue(), cur)));
    }
    return new BodyEnv(makeBody(args.subList(1, args.size())), cur);
  }

  private static BodyEnv letrecPrep(List<SExpr> args, Env env) {
    if (args.size() < 2) throw new LispError("letrec: expected bindings and body");
    var bindingsList = requireBindingsList("letrec", args.getFirst());
    Map<String, SExpr> placeholders = new LinkedHashMap<>();
    for (SExpr b : bindingsList)
      placeholders.put(parseBindingPair("letrec", b).getKey(), SExpr.NIL);
    Env recEnv = env.child(placeholders);
    for (SExpr b : bindingsList) {
      var p = parseBindingPair("letrec", b);
      recEnv.define(p.getKey(), eval(p.getValue(), recEnv));
    }
    return new BodyEnv(makeBody(args.subList(1, args.size())), recEnv);
  }

  // ---- Quasiquote expansion ----

  /// Recursively expands a quasiquote template:
  ///
  ///
  ///   - ``,expr` → `eval(expr)`
  ///   - ``(,@list ...)` → splice `list` into the surrounding list
  ///   - Atoms and non-unquoted sublists → returned as-is
  ///
  private static SExpr expandQuasiquote(SExpr template, Env env) {
    // `,expr → eval(expr)
    if (template instanceof SExpr.SList(List<SExpr> items)
        && items.size() == 2
        && items.getFirst() instanceof SExpr.Sym(String tag)
        && tag.equals("unquote")) {
      return eval(items.get(1), env);
    }
    // Non-list atom: return as-is
    if (!(template instanceof SExpr.SList(List<SExpr> items))) return template;
    // List: expand each element, handling ,@ splicing
    List<SExpr> result = new ArrayList<>();
    for (SExpr elem : items) {
      if (elem instanceof SExpr.SList(List<SExpr> inner)
          && inner.size() == 2
          && inner.getFirst() instanceof SExpr.Sym(String tag)
          && tag.equals("unquote-splicing")) {
        SExpr spliced = eval(inner.get(1), env);
        if (!(spliced instanceof SExpr.SList(List<SExpr> splicedItems)))
          throw new LispError("unquote-splicing: expected list, got: " + spliced);
        result.addAll(splicedItems);
      } else {
        result.add(expandQuasiquote(elem, env));
      }
    }
    return SExpr.list(result);
  }

  // ---- Shared helpers ----

  /// Evaluates all expressions in `body` and returns the value of the last one. Used by {@link
  /// #run} to evaluate top-level programs.
  static SExpr evalBody(List<SExpr> body, Env env) {
    SExpr r = SExpr.NIL;
    for (SExpr e : body) r = eval(e, env);
    return r;
  }

  /// Wraps a list of expressions as a single expression. A single expression is returned directly;
  /// multiple expressions are wrapped in `(begin ...)`. An empty list returns `NIL`.
  private static SExpr makeBody(List<SExpr> exprs) {
    if (exprs.isEmpty()) return SExpr.NIL;
    if (exprs.size() == 1) return exprs.getFirst();
    List<SExpr> items = new ArrayList<>();
    items.add(SExpr.sym("begin"));
    items.addAll(exprs);
    return SExpr.list(items);
  }

  private static List<String> extractParamNames(List<SExpr> paramExprs) {
    List<String> params = new ArrayList<>();
    for (SExpr p : paramExprs) {
      if (!(p instanceof SExpr.Sym(String name)))
        throw new LispError("Parameter must be a symbol, got: " + p);
      params.add(name);
    }
    return params;
  }

  private static List<SExpr> requireBindingsList(String form, SExpr expr) {
    if (!(expr instanceof SExpr.SList(List<SExpr> items)))
      throw new LispError(form + ": bindings must be a list, got: " + expr);
    return items;
  }

  private static Map.Entry<String, SExpr> parseBindingPair(String form, SExpr binding) {
    if (!(binding instanceof SExpr.SList(List<SExpr> pair)) || pair.size() != 2)
      throw new LispError(form + ": binding must be (name expr), got: " + binding);
    if (!(pair.getFirst() instanceof SExpr.Sym(String name)))
      throw new LispError(form + ": binding name must be a symbol, got: " + pair.getFirst());
    return Map.entry(name, pair.get(1));
  }

  private static void requireArity(String form, List<SExpr> args, int n) {
    if (args.size() != n)
      throw new LispError(form + ": expected " + n + " args, got " + args.size());
  }

  // ---- Convenience entry points ----

  public static SExpr run(String program) {
    return run(program, Builtins.defaultEnv());
  }

  public static SExpr run(String program, Env env) {
    return evalBody(SExprParser.parseAll(program), env);
  }
}
