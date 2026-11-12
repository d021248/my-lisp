package d021248.mytry.core.lisp.list;

import static d021248.mytry.core.lisp.list.Lisp.*;
import static d021248.mytry.core.lisp.list.LispList.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LispTest {

  // -------------------------------------------------------------------------
  // car
  // -------------------------------------------------------------------------

  @Test
  void car_emptyList_returnsEmpty() {
    assertTrue(car(nil()).isEmpty());
  }

  @Test
  void car_nonEmptyList_returnsHead() {
    assertEquals(1, car(cons(1, nil())).orElseThrow());
  }

  // -------------------------------------------------------------------------
  // cdr
  // -------------------------------------------------------------------------

  @Test
  void cdr_emptyList_returnsNil() {
    assertEquals("()", show(cdr(nil())));
  }

  @Test
  void cdr_singleElement_returnsNil() {
    assertEquals("()", show(cdr(cons(1, nil()))));
  }

  @Test
  void cdr_multipleElements_returnsTail() {
    assertEquals("(2 3)", show(cdr(cons(1, cons(2, cons(3, nil()))))));
  }

  // -------------------------------------------------------------------------
  // length
  // -------------------------------------------------------------------------

  @Test
  void length_emptyList_returnsZero() {
    assertEquals(0, length(nil()));
  }

  @Test
  void length_list_returnsElementCount() {
    assertEquals(3, length(cons(1, cons(2, cons(3, nil())))));
  }

  // -------------------------------------------------------------------------
  // show
  // -------------------------------------------------------------------------

  @Test
  void show_emptyList_returnsEmptyParens() {
    assertEquals("()", show(nil()));
  }

  @Test
  void show_list_returnsLispNotation() {
    assertEquals("(1 2 3)", show(cons(1, cons(2, cons(3, nil())))));
  }

  // -------------------------------------------------------------------------
  // append
  // -------------------------------------------------------------------------

  @Test
  void append_twoEmptyLists_returnsEmptyList() {
    assertEquals("()", show(append(nil(), nil())));
  }

  @Test
  void append_emptyLeft_returnsRight() {
    LispList<Integer> b = cons(1, cons(2, nil()));
    assertEquals(show(b), show(append(nil(), b)));
  }

  @Test
  void append_emptyRight_returnsLeft() {
    LispList<Integer> a = cons(1, cons(2, nil()));
    assertEquals(show(a), show(append(a, nil())));
  }

  @Test
  void append_twoLists_concatenatesInOrder() {
    LispList<Integer> a = cons(1, cons(2, nil()));
    LispList<Integer> b = cons(3, cons(4, nil()));
    assertEquals("(1 2 3 4)", show(append(a, b)));
  }

  // -------------------------------------------------------------------------
  // reverse
  // -------------------------------------------------------------------------

  @Test
  void reverse_emptyList_returnsEmptyList() {
    assertEquals("()", show(reverse(nil())));
  }

  @Test
  void reverse_singleElement_returnsSameList() {
    assertEquals("(1)", show(reverse(cons(1, nil()))));
  }

  @Test
  void reverse_list_reversesOrder() {
    assertEquals("(3 2 1)", show(reverse(cons(1, cons(2, cons(3, nil()))))));
  }

  @Test
  void reverse_twice_isIdentity() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
    assertEquals(show(list), show(reverse(reverse(list))));
  }

  // -------------------------------------------------------------------------
  // take
  // -------------------------------------------------------------------------

  @Test
  void take_zero_returnsEmptyList() {
    assertEquals("()", show(take(cons(1, cons(2, nil())), 0)));
  }

  @Test
  void take_negative_returnsEmptyList() {
    assertEquals("()", show(take(cons(1, nil()), -1)));
  }

  @Test
  void take_fewerThanLength_returnsPrefix() {
    assertEquals("(1 2)", show(take(cons(1, cons(2, cons(3, nil()))), 2)));
  }

  @Test
  void take_moreThanLength_returnsFullList() {
    LispList<Integer> list = cons(1, cons(2, nil()));
    assertEquals(show(list), show(take(list, 99)));
  }

  // -------------------------------------------------------------------------
  // drop
  // -------------------------------------------------------------------------

  @Test
  void drop_zero_returnsFullList() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
    assertEquals(show(list), show(drop(list, 0)));
  }

  @Test
  void drop_someElements_returnsSuffix() {
    assertEquals("(3)", show(drop(cons(1, cons(2, cons(3, nil()))), 2)));
  }

  @Test
  void drop_moreThanLength_returnsEmptyList() {
    assertEquals("()", show(drop(cons(1, cons(2, nil())), 99)));
  }

  // -------------------------------------------------------------------------
  // map
  // -------------------------------------------------------------------------

  @Test
  void map_emptyList_returnsEmptyList() {
    assertEquals("()", show(map(nil(), x -> x)));
  }

  @Test
  void map_doublesEachElement() {
    assertEquals("(2 4 6)", show(map(cons(1, cons(2, cons(3, nil()))), x -> x * 2)));
  }

  // -------------------------------------------------------------------------
  // filter
  // -------------------------------------------------------------------------

  @Test
  void filter_noneMatch_returnsEmptyList() {
    assertEquals("()", show(filter(cons(1, cons(3, nil())), x -> x % 2 == 0)));
  }

  @Test
  void filter_allMatch_returnsFullList() {
    LispList<Integer> list = cons(2, cons(4, nil()));
    assertEquals(show(list), show(filter(list, x -> x % 2 == 0)));
  }

  @Test
  void filter_someMatch_returnsMatchingElements() {
    assertEquals("(2 4)", show(filter(cons(1, cons(2, cons(3, cons(4, nil())))), x -> x % 2 == 0)));
  }

  // -------------------------------------------------------------------------
  // flatMap
  // -------------------------------------------------------------------------

  @Test
  void flatMap_emptyList_returnsEmptyList() {
    assertEquals("()", show(flatMap(nil(), x -> cons(x, nil()))));
  }

  @Test
  void flatMap_duplicatesEachElement() {
    LispList<Integer> result =
        flatMap(cons(1, cons(2, cons(3, nil()))), x -> cons(x, cons(x, nil())));
    assertEquals("(1 1 2 2 3 3)", show(result));
  }

  // -------------------------------------------------------------------------
  // foldl
  // -------------------------------------------------------------------------

  @Test
  void foldl_sum_returnsCorrectResult() {
    assertEquals(6, (int) foldl(cons(1, cons(2, cons(3, nil()))), 0, Integer::sum));
  }

  @Test
  void foldl_isLeftAssociative() {
    // Left fold on strings: ((("" + "a") + "b") + "c") = "abc"
    String result = foldl(cons("a", cons("b", cons("c", nil()))), "", (acc, s) -> acc + s);
    assertEquals("abc", result);
  }

  // -------------------------------------------------------------------------
  // foldr
  // -------------------------------------------------------------------------

  @Test
  void foldr_sum_returnsCorrectResult() {
    assertEquals(6, (int) foldr(cons(1, cons(2, cons(3, nil()))), 0, Integer::sum));
  }

  @Test
  void foldr_withCons_rebuildsListIdentically() {
    // foldr with cons is the identity transformation for linked lists
    LispList<Integer> original = cons(1, cons(2, cons(3, nil())));
    LispList<Integer> rebuilt = foldr(original, nil(), LispList::cons);
    assertEquals(show(original), show(rebuilt));
  }

  // -------------------------------------------------------------------------
  // toList / fromList
  // -------------------------------------------------------------------------

  @Test
  void toList_emptyList_returnsEmptyJavaList() {
    assertTrue(toList(nil()).isEmpty());
  }

  @Test
  void toList_list_returnsEquivalentJavaList() {
    assertEquals(List.of(1, 2, 3), toList(cons(1, cons(2, cons(3, nil())))));
  }

  @Test
  void fromList_emptyJavaList_returnsEmptyLispList() {
    assertEquals("()", show(fromList(List.of())));
  }

  @Test
  void fromList_javaList_preservesOrder() {
    assertEquals("(1 2 3)", show(fromList(List.of(1, 2, 3))));
  }

  @Test
  void roundTrip_fromListToList_isIdentity() {
    List<Integer> original = List.of(1, 2, 3, 4, 5);
    assertEquals(original, toList(fromList(original)));
  }

  // -------------------------------------------------------------------------
  // Iterable
  // -------------------------------------------------------------------------

  @Test
  void iterable_forEachLoop_iteratesHeadToTail() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
    List<Integer> collected = new ArrayList<>();
    for (int x : list) {
      collected.add(x);
    }
    assertEquals(List.of(1, 2, 3), collected);
  }

  @Test
  void iterable_emptyList_iteratesZeroTimes() {
    int count = 0;
    for (Integer ignored : LispList.<Integer>nil()) {
      count++;
    }
    assertEquals(0, count);
  }

  // -------------------------------------------------------------------------
  // takeWhile
  // -------------------------------------------------------------------------

  @Test
  void takeWhile_emptyList_returnsEmptyList() {
    assertEquals("()", show(takeWhile(nil(), x -> true)));
  }

  @Test
  void takeWhile_predicateAlwaysTrue_returnsFullList() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
    assertEquals(show(list), show(takeWhile(list, x -> true)));
  }

  @Test
  void takeWhile_takesPrefix() {
    assertEquals("(1 2)", show(takeWhile(cons(1, cons(2, cons(3, nil()))), x -> x < 3)));
  }

  @Test
  void takeWhile_firstElementFails_returnsEmptyList() {
    assertEquals("()", show(takeWhile(cons(5, cons(1, nil())), x -> x < 3)));
  }

  // -------------------------------------------------------------------------
  // dropWhile
  // -------------------------------------------------------------------------

  @Test
  void dropWhile_emptyList_returnsEmptyList() {
    assertEquals("()", show(dropWhile(nil(), x -> true)));
  }

  @Test
  void dropWhile_dropsSatisfyingPrefix() {
    assertEquals("(3)", show(dropWhile(cons(1, cons(2, cons(3, nil()))), x -> x < 3)));
  }

  @Test
  void dropWhile_noneMatch_returnsFullList() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));
    assertEquals(show(list), show(dropWhile(list, x -> x > 99)));
  }

  // -------------------------------------------------------------------------
  // any / all / find
  // -------------------------------------------------------------------------

  @Test
  void any_emptyList_returnsFalse() {
    assertFalse(any(nil(), x -> true));
  }

  @Test
  void any_matchExists_returnsTrue() {
    assertTrue(any(cons(1, cons(2, cons(3, nil()))), x -> x > 2));
  }

  @Test
  void any_noMatch_returnsFalse() {
    assertFalse(any(cons(1, cons(2, cons(3, nil()))), x -> x > 99));
  }

  @Test
  void all_emptyList_returnsTrue() {
    assertTrue(all(nil(), x -> false));
  }

  @Test
  void all_allMatch_returnsTrue() {
    assertTrue(all(cons(1, cons(2, cons(3, nil()))), x -> x > 0));
  }

  @Test
  void all_oneFailure_returnsFalse() {
    assertFalse(all(cons(1, cons(2, cons(3, nil()))), x -> x < 3));
  }

  @Test
  void find_emptyList_returnsEmpty() {
    assertTrue(find(nil(), x -> true).isEmpty());
  }

  @Test
  void find_matchExists_returnsFirstMatch() {
    assertEquals(2, (int) find(cons(1, cons(2, cons(3, nil()))), x -> x > 1).orElseThrow());
  }

  @Test
  void find_noMatch_returnsEmpty() {
    assertTrue(find(cons(1, cons(2, nil())), x -> x > 99).isEmpty());
  }

  // -------------------------------------------------------------------------
  // scanl
  // -------------------------------------------------------------------------

  @Test
  void scanl_emptyList_returnsSingletonSeed() {
    assertEquals("(0)", show(scanl(nil(), 0, Integer::sum)));
  }

  @Test
  void scanl_sum_returnsRunningTotals() {
    assertEquals("(0 1 3 6)", show(scanl(cons(1, cons(2, cons(3, nil()))), 0, Integer::sum)));
  }

  // -------------------------------------------------------------------------
  // zipWith
  // -------------------------------------------------------------------------

  @Test
  void zipWith_emptyLeft_returnsEmptyList() {
    assertEquals("()", show(zipWith(nil(), cons(1, nil()), Integer::sum)));
  }

  @Test
  void zipWith_emptyRight_returnsEmptyList() {
    assertEquals("()", show(zipWith(cons(1, nil()), nil(), Integer::sum)));
  }

  @Test
  void zipWith_stopsAtShorterList() {
    assertEquals(
        "(11 22)",
        show(zipWith(cons(1, cons(2, cons(3, nil()))), cons(10, cons(20, nil())), Integer::sum)));
  }

  @Test
  void zipWith_sameLength_combinesAllPairs() {
    assertEquals(
        "(11 22 33)",
        show(
            zipWith(
                cons(1, cons(2, cons(3, nil()))),
                cons(10, cons(20, cons(30, nil()))),
                Integer::sum)));
  }

  // -------------------------------------------------------------------------
  // sort
  // -------------------------------------------------------------------------

  @Test
  void sort_emptyList_returnsEmptyList() {
    assertEquals("()", show(sort(nil(), Integer::compare)));
  }

  @Test
  void sort_singleElement_returnsUnchanged() {
    assertEquals("(42)", show(sort(cons(42, nil()), Integer::compare)));
  }

  @Test
  void sort_alreadySorted_returnsUnchanged() {
    assertEquals("(1 2 3)", show(sort(cons(1, cons(2, cons(3, nil()))), Integer::compare)));
  }

  @Test
  void sort_reversedList_sortsAscending() {
    assertEquals("(1 2 3)", show(sort(cons(3, cons(2, cons(1, nil()))), Integer::compare)));
  }

  @Test
  void sort_withDuplicates_preservesAll() {
    assertEquals(
        "(1 1 3 4 5)",
        show(sort(cons(3, cons(1, cons(4, cons(1, cons(5, nil()))))), Integer::compare)));
  }

  @Test
  void sort_reverseComparator_sortDescending() {
    assertEquals("(3 2 1)", show(sort(cons(1, cons(2, cons(3, nil()))), (a, b) -> b - a)));
  }
}
