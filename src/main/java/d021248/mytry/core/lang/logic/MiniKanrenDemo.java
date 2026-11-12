package d021248.mytry.core.lang.logic;

import static d021248.mytry.core.lang.logic.Goal.*;
import static d021248.mytry.core.lang.logic.MiniKanren.*;
import static d021248.mytry.core.lang.logic.Term.*;

/// Demonstrates miniKanren with classic relational goals: `membero` and `appendo`.
///
/// The key insight: the same goal can be run in multiple "directions".
/// `appendo(prefix, suffix, full)` can compute the suffix given prefix and full,
/// enumerate all splits of a list, or verify a concatenation.
public final class MiniKanrenDemo {

  // -------------------------------------------------------------------------
  // Standard relational goals
  // -------------------------------------------------------------------------

  /// `membero(x, list)`: `x` is an element of `list`.
  static Goal membero(Term x, Term list) {
    return fresh(
        head ->
            fresh(
                tail ->
                    conde(
                        conj(unify(list, cons(head, tail)), unify(x, head)),
                        conj(unify(list, cons(head, tail)), membero(x, tail)))));
  }

  /// `appendo(l, r, out)`: `out` is the concatenation of `l` and `r`.
  static Goal appendo(Term l, Term r, Term out) {
    return conde(
        conj(unify(l, NIL), unify(out, r)),
        fresh(
            head ->
                fresh(
                    tail ->
                        fresh(
                            res ->
                                conj(
                                    unify(l, cons(head, tail)),
                                    unify(out, cons(head, res)),
                                    appendo(tail, r, res))))));
  }

  // -------------------------------------------------------------------------
  // Demo
  // -------------------------------------------------------------------------

  public static void main() {
    // Forward: what are the members of (1 2 3)?
    var list123 = list(val(1), val(2), val(3));
    System.out.println("Members of (1 2 3): " + run(10, q -> membero(q, list123)));

    // Forward: (1 2) ++ (3 4) = ?
    var l12 = list(val(1), val(2));
    var l34 = list(val(3), val(4));
    System.out.println("(1 2) ++ (3 4) = " + runStar(q -> appendo(l12, l34, q)));

    // Backward: (1 2) ++ ? = (1 2 3 4)
    var l1234 = list(val(1), val(2), val(3), val(4));
    System.out.println("(1 2) ++ ? = (1 2 3 4): " + run(1, q -> appendo(l12, q, l1234)));

    // Relational: all ways to split (1 2 3)
    System.out.println("All splits of (1 2 3):");
    run(
            10,
            q ->
                fresh(
                    l -> fresh(r -> conj(appendo(l, r, list123), unify(q, cons(l, cons(r, NIL)))))))
        .forEach(t -> System.out.println("  " + t));
  }
}
