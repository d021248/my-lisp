package d021248.mytry.core.lang.logic;

import static d021248.mytry.core.lang.logic.Goal.*;
import static d021248.mytry.core.lang.logic.MiniKanren.*;
import static d021248.mytry.core.lang.logic.Term.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MiniKanrenTest {

  // -------------------------------------------------------------------------
  // Relational goals reused across tests
  // -------------------------------------------------------------------------

  private static Goal membero(Term x, Term list) {
    return fresh(
        head ->
            fresh(
                tail ->
                    conde(
                        conj(unify(list, cons(head, tail)), unify(x, head)),
                        conj(unify(list, cons(head, tail)), membero(x, tail)))));
  }

  private static Goal appendo(Term l, Term r, Term out) {
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
  // Primitive goal tests
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("unify two equal values succeeds and produces one state")
  void unifyTwoEqualValuesSucceeds() {
    var results = run(10, q -> conj(unify(q, val(42)), unify(q, val(42))));
    assertThat(results).containsExactly(val(42));
  }

  @Test
  @DisplayName("unify two different ground values fails and produces no states")
  void unifyTwoDifferentValuesFails() {
    var results = run(10, q -> conj(unify(q, val(1)), unify(q, val(2))));
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("unify a variable with a value binds the variable to that value")
  void unifyVariableWithValueBindsIt() {
    var results = run(1, q -> unify(q, val("hello")));
    assertThat(results).containsExactly(val("hello"));
  }

  @Test
  @DisplayName("fresh introduces an unbound variable that can subsequently be unified")
  void freshIntroducesUnboundVariable() {
    var results = run(1, q -> fresh(x -> conj(unify(x, val(99)), unify(q, x))));
    assertThat(results).containsExactly(val(99));
  }

  @Test
  @DisplayName("succeed always passes the current state through unchanged")
  void succeedAlwaysPassesStateThrough() {
    var results = run(5, q -> conj(unify(q, val(7)), succeed()));
    assertThat(results).containsExactly(val(7));
  }

  @Test
  @DisplayName("fail produces no states regardless of the current state")
  void failProducesNoStates() {
    var results = run(5, q -> conj(unify(q, val(7)), fail()));
    assertThat(results).isEmpty();
  }

  // -------------------------------------------------------------------------
  // Composite goal tests
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("conde returns results from all successful branches")
  void condeReturnsResultsFromAllBranches() {
    var results = run(10, q -> conde(unify(q, val(1)), unify(q, val(2)), unify(q, val(3))));
    assertThat(results).containsExactlyInAnyOrder(val(1), val(2), val(3));
  }

  @Test
  @DisplayName("conj requires all goals to succeed — one fail makes the conjunction fail")
  void conjRequiresAllGoalsToSucceed() {
    var results = run(10, q -> conj(unify(q, val("x")), fail()));
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("run(2, ...) returns at most 2 results even when more are available")
  void runLimitsResults() {
    var results = run(2, q -> conde(unify(q, val(1)), unify(q, val(2)), unify(q, val(3))));
    assertThat(results).hasSize(2);
  }

  // -------------------------------------------------------------------------
  // Relational goal tests
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("membero")
  class MemberoTests {

    @Test
    @DisplayName("membero enumerates all elements of a list in order")
    void memberoEnumeratesAllElements() {
      var nums = list(val(1), val(2), val(3));
      assertThat(run(10, q -> membero(q, nums))).containsExactly(val(1), val(2), val(3));
    }

    @Test
    @DisplayName("membero succeeds when the element is present in the list")
    void memberoChecksMembership() {
      var nums = list(val("a"), val("b"), val("c"));
      var results = run(1, q -> conj(membero(val("b"), nums), unify(q, val(true))));
      assertThat(results).containsExactly(val(true));
    }

    @Test
    @DisplayName("membero fails when the element is absent from the list")
    void memberoFailsForAbsentElement() {
      var nums = list(val(1), val(2), val(3));
      assertThat(run(1, q -> conj(membero(val(99), nums), unify(q, val(true))))).isEmpty();
    }
  }

  @Nested
  @DisplayName("appendo")
  class AppendoTests {

    @Test
    @DisplayName("appendo computes the concatenation of two lists")
    void appendoComputesConcatenation() {
      var l12 = list(val(1), val(2));
      var l34 = list(val(3), val(4));
      var l1234 = list(val(1), val(2), val(3), val(4));
      assertThat(runStar(q -> appendo(l12, l34, q))).containsExactly(l1234);
    }

    @Test
    @DisplayName("appendo reconstructs the missing suffix given a prefix and the full list")
    void appendoReconstructsMissingSuffix() {
      var prefix = list(val(1), val(2));
      var full = list(val(1), val(2), val(3), val(4));
      assertThat(run(1, q -> appendo(prefix, q, full))).containsExactly(list(val(3), val(4)));
    }

    @Test
    @DisplayName("appendo produces all splits of a list")
    void appendoEnumeratesAllSplits() {
      var l123 = list(val(1), val(2), val(3));
      var splits =
          run(
              10,
              q ->
                  fresh(
                      l -> fresh(r -> conj(appendo(l, r, l123), unify(q, cons(l, cons(r, NIL)))))));
      assertThat(splits).hasSize(4); // (), (1 2 3) | (1), (2 3) | (1 2), (3) | (1 2 3), ()
    }

    @Test
    @DisplayName("appendo with NIL left-argument returns the right list unchanged")
    void appendoNilLeftIsIdentity() {
      var l123 = list(val(1), val(2), val(3));
      assertThat(runStar(q -> appendo(NIL, l123, q))).containsExactly(l123);
    }
  }
}
