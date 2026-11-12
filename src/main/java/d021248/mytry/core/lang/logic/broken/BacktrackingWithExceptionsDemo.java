package d021248.mytry.core.lang.logic.broken;

import java.util.ArrayList;
import java.util.List;

/// # Backtracking with Exceptions — Anti-Pattern
///
/// The naive approach to constraint search throws a `BacktrackException` whenever
/// a constraint fails, relying on the call stack for the backtracking structure.
/// This couples control flow to exception mechanics, obscures intent, and incurs
/// throw/catch overhead for every dead branch explored.
///
/// The fix is modeling search as `Goal: State → Stream<State>` (see the parent package).
/// Lazy streams decouple exploration from control flow entirely.
///
/// @see d021248.mytry.core.lang.logic.Goal
public final class BacktrackingWithExceptionsDemo {

  static class BacktrackException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    BacktrackException() {
      super(null, null, true, false);
    }
  }

  // Solve N-Queens by throwing BacktrackException on constraint violation
  static List<List<Integer>> solveNQueens(int n) {
    var solutions = new ArrayList<List<Integer>>();
    solve(new ArrayList<>(), n, solutions);
    return solutions;
  }

  private static void solve(List<Integer> queens, int n, List<List<Integer>> solutions) {
    if (queens.size() == n) {
      solutions.add(List.copyOf(queens));
      return;
    }
    for (var col = 0; col < n; col++) {
      queens.add(col);
      try {
        validate(queens);
        solve(queens, n, solutions);
      } catch (BacktrackException _) {
        // swallow and try next column
      }
      queens.remove(queens.size() - 1);
    }
  }

  private static void validate(List<Integer> queens) {
    var row = queens.size() - 1;
    for (var i = 0; i < row; i++) {
      var diff = Math.abs(queens.get(i) - queens.get(row));
      if (diff == 0 || diff == row - i) throw new BacktrackException();
    }
  }

  public static void main() {
    System.out.println("N=4 solutions (exception-based backtracking):");
    solveNQueens(4).forEach(System.out::println);
  }
}
