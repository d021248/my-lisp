package d021248.mytry.core.lang.logic;

import module java.base;

/// Entry points for running miniKanren queries.
///
/// `run` returns at most `n` answers; `runStar` collects all answers from a finite relation.
/// Both reify the query variable before returning, replacing any residual unbound variables
/// with their representative `Term.Var` nodes.
public final class MiniKanren {

  private MiniKanren() {}

  /// Runs `query` and returns at most `n` reified answers for the query variable.
  public static List<Term> run(int n, Function<Term.Var, Goal> query) {
    var initial = State.empty();
    var result = initial.fresh();
    var queryVar = result.variable();
    var startState = result.state();
    return query
        .apply(queryVar)
        .apply(startState)
        .limit(n)
        .map(s -> s.subst().reify(queryVar))
        .toList();
  }

  /// Runs `query` and collects all answers. Only use with finite relations.
  public static List<Term> runStar(Function<Term.Var, Goal> query) {
    var initial = State.empty();
    var result = initial.fresh();
    var queryVar = result.variable();
    var startState = result.state();
    return query.apply(queryVar).apply(startState).map(s -> s.subst().reify(queryVar)).toList();
  }
}
