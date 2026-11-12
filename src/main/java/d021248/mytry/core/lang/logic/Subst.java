package d021248.mytry.core.lang.logic;

import module java.base;

/// An immutable substitution: a mapping from logic variables to terms.
///
/// Substitutions grow monotonically — `extend` always returns a new `Subst`.
/// `walk` follows variable chains to find a term's representative.
/// `unify` attempts to make two terms equal, returning an updated substitution
/// or `Optional.empty()` on failure.
public record Subst(Map<Term.Var, Term> bindings) {

  public Subst {
    bindings = Map.copyOf(bindings);
  }

  /// Returns the empty substitution.
  public static Subst empty() {
    return new Subst(Map.of());
  }

  /// Returns a new substitution with `v` bound to `t`.
  public Subst extend(Term.Var v, Term t) {
    var updated = new HashMap<>(bindings);
    updated.put(v, t);
    return new Subst(Map.copyOf(updated));
  }

  /// Follows variable chains until a non-variable or unbound variable is found.
  public Term walk(Term t) {
    while (t instanceof Term.Var v && bindings.containsKey(v)) {
      t = bindings.get(v);
    }
    return t;
  }

  /// Attempts to unify `u` and `v` under this substitution.
  ///
  /// Returns an updated substitution on success, or `Optional.empty()` on failure.
  public Optional<Subst> unify(Term u, Term v) {
    u = walk(u);
    v = walk(v);
    if (u.equals(v)) return Optional.of(this);
    if (u instanceof Term.Var uv) return Optional.of(extend(uv, v));
    if (v instanceof Term.Var vv) return Optional.of(extend(vv, u));
    if (u instanceof Term.Cons(var uh, var ut) && v instanceof Term.Cons(var vh, var vt)) {
      return unify(uh, vh).flatMap(s -> s.unify(ut, vt));
    }
    if (u instanceof Term.Val<?> uVal && v instanceof Term.Val<?> vVal) {
      return Objects.equals(uVal.value(), vVal.value()) ? Optional.of(this) : Optional.empty();
    }
    return Optional.empty();
  }

  /// Replaces all variables in `t` with their walked values from this substitution.
  public Term reify(Term t) {
    return switch (walk(t)) {
      case Term.Var v -> v;
      case Term.Val<?> v -> v;
      case Term.Cons(var h, var tail) -> Term.cons(reify(h), reify(tail));
    };
  }
}
