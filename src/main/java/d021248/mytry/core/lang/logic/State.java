package d021248.mytry.core.lang.logic;

/// The state of a logic computation: a substitution and a fresh-variable counter.
public record State(Subst subst, int counter) {

  /// Returns the initial empty state.
  public static State empty() {
    return new State(Subst.empty(), 0);
  }

  /// Returns a new state with `v` bound to `t` in the substitution.
  public State bind(Term.Var v, Term t) {
    return new State(subst.extend(v, t), counter);
  }

  /// Carries a freshly allocated variable together with the updated state.
  public record Fresh(State state, Term.Var variable) {}

  /// Allocates a fresh logic variable and returns it paired with the advanced state.
  public Fresh fresh() {
    var v = Term.var(counter);
    return new Fresh(new State(subst, counter + 1), v);
  }
}
