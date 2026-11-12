package d021248.mytry.core.lang.logic;

import module java.base;

/// A miniKanren goal: a function from a `State` to a lazy stream of successor states.
///
/// An empty stream means failure. Multiple states mean multiple answers.
/// Goals compose with `and` (conjunction) and `conde` (disjunction).
///
/// Static factory methods mirror the miniKanren primitives:
///
/// - `unify` — make two terms equal (`≡`)
/// - `fresh` — introduce a new logic variable
/// - `conde` — try all branches, collect successes from each
/// - `conj` — require all goals to succeed in sequence
/// - `succeed` / `fail` — trivial terminal goals
@FunctionalInterface
public interface Goal {

  Stream<State> apply(State s);

  // -------------------------------------------------------------------------
  // Primitives
  // -------------------------------------------------------------------------

  /// Unifies `u` and `v` under the current substitution.
  static Goal unify(Term u, Term v) {
    return s ->
        s.subst()
            .unify(u, v)
            .map(unified -> Stream.of(new State(unified, s.counter())))
            .orElse(Stream.empty());
  }

  /// Introduces a fresh logic variable and passes it to `body`.
  static Goal fresh(Function<Term.Var, Goal> body) {
    return s -> {
      var result = s.fresh();
      return body.apply(result.variable()).apply(result.state());
    };
  }

  /// Tries all `branches` and interleaves their results (disjunction).
  static Goal conde(Goal... branches) {
    return s -> Arrays.stream(branches).flatMap(g -> g.apply(s));
  }

  /// Requires all `goals` to succeed in left-to-right order (conjunction).
  static Goal conj(Goal... goals) {
    if (goals.length == 0) return succeed();
    return Arrays.stream(goals).reduce(succeed(), Goal::and);
  }

  /// Always succeeds, passing the state through unchanged.
  static Goal succeed() {
    return s -> Stream.of(s);
  }

  /// Always fails, producing no successor states.
  static Goal fail() {
    return _ -> Stream.empty();
  }

  // -------------------------------------------------------------------------
  // Combinator
  // -------------------------------------------------------------------------

  /// Sequential conjunction: run `this`, then thread each result through `other`.
  default Goal and(Goal other) {
    return s -> apply(s).flatMap(other::apply);
  }
}
