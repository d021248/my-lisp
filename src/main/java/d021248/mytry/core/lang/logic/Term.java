package d021248.mytry.core.lang.logic;

/// A term in the miniKanren logic system.
///
/// Terms are either unbound variables, ground values lifted from Java, or cons-pairs
/// used to build logic lists.
public sealed interface Term permits Term.Var, Term.Val, Term.Cons {

  /// An unbound logic variable, identified by a unique integer id.
  record Var(int id) implements Term {
    @Override
    public String toString() {
      return "_%d".formatted(id);
    }
  }

  /// A ground Java value lifted into a term.
  record Val<T>(T value) implements Term {
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /// A cons-pair — the building block for logic lists.
  record Cons(Term head, Term tail) implements Term {
    @Override
    public String toString() {
      var sb = new StringBuilder("(");
      Term cur = this;
      while (cur instanceof Cons(var h, var t)) {
        if (sb.length() > 1) sb.append(" ");
        sb.append(h);
        cur = t;
      }
      if (!cur.equals(NIL)) sb.append(" . ").append(cur);
      return sb.append(")").toString();
    }
  }

  /// The empty-list sentinel: a `Val` carrying `null`.
  Val<Void> NIL = new Val<>(null);

  // -------------------------------------------------------------------------
  // Smart constructors
  // -------------------------------------------------------------------------

  static Var var(int id) {
    return new Var(id);
  }

  static <T> Val<T> val(T value) {
    return new Val<>(value);
  }

  static Cons cons(Term head, Term tail) {
    return new Cons(head, tail);
  }

  /// Builds a logic list from the given terms, terminated by `NIL`.
  static Term list(Term... terms) {
    Term result = NIL;
    for (var i = terms.length - 1; i >= 0; i--) result = cons(terms[i], result);
    return result;
  }
}
