package d021248.mytry.core.lisp.list;

import static d021248.mytry.core.lisp.list.Lazy.*;
import static d021248.mytry.core.lisp.list.Lisp.show;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LazyTest {

  // -------------------------------------------------------------------------
  // from
  // -------------------------------------------------------------------------

  @Test
  void from_takeFive_returnsConsecutiveIntegers() {
    assertEquals("(1 2 3 4 5)", show(take(from(1), 5)));
  }

  @Test
  void from_takeZero_returnsEmptyList() {
    assertEquals("()", show(take(from(42), 0)));
  }

  @Test
  void from_startingFromNegative_works() {
    assertEquals("(-2 -1 0 1 2)", show(take(from(-2), 5)));
  }

  // -------------------------------------------------------------------------
  // iterate
  // -------------------------------------------------------------------------

  @Test
  void iterate_powersOfTwo_generatesCorrectSequence() {
    assertEquals("(1 2 4 8 16)", show(take(iterate(1, x -> x * 2), 5)));
  }

  @Test
  void iterate_identityFunction_repeatsValue() {
    assertEquals("(7 7 7 7 7)", show(take(iterate(7, x -> x), 5)));
  }

  // -------------------------------------------------------------------------
  // repeat
  // -------------------------------------------------------------------------

  @Test
  void repeat_takeFour_returnsFourCopies() {
    assertEquals("(0 0 0 0)", show(take(repeat(0), 4)));
  }

  // -------------------------------------------------------------------------
  // fibs
  // -------------------------------------------------------------------------

  @Test
  void fibs_firstTen_returnsCorrectFibonacciSequence() {
    assertEquals("(0 1 1 2 3 5 8 13 21 34)", show(take(fibs(), 10)));
  }

  // -------------------------------------------------------------------------
  // car / cdr
  // -------------------------------------------------------------------------

  @Test
  void car_returnsHead() {
    assertEquals(42, (int) car(from(42)));
  }

  @Test
  void cdr_returnsNextElement() {
    assertEquals(2, (int) car(cdr(from(1))));
  }

  @Test
  void cdr_emptyList_returnsNil() {
    assertEquals("()", show(take(cdr(LazyList.nil()), 0)));
  }

  // -------------------------------------------------------------------------
  // map
  // -------------------------------------------------------------------------

  @Test
  void map_squaresOfNaturals_computedLazily() {
    assertEquals("(1 4 9 16 25)", show(take(map(from(1), x -> x * x), 5)));
  }

  @Test
  void map_onEmptyList_returnsEmptyList() {
    assertEquals("()", show(take(map(LazyList.<Integer>nil(), x -> x * 2), 5)));
  }

  // -------------------------------------------------------------------------
  // filter
  // -------------------------------------------------------------------------

  @Test
  void filter_evenNaturals_returnsEvenNumbers() {
    assertEquals("(2 4 6 8 10)", show(take(filter(from(1), x -> x % 2 == 0), 5)));
  }

  @Test
  void filter_noneMatch_returnsEmptyList() {
    LazyList<Integer> finite = LazyList.cons(1, () -> LazyList.cons(3, LazyList::nil));
    assertEquals("()", show(take(filter(finite, x -> x % 2 == 0), 10)));
  }

  // -------------------------------------------------------------------------
  // zipWith
  // -------------------------------------------------------------------------

  @Test
  void zipWith_sumOfTwoStreams_returnsPairwiseSums() {
    assertEquals("(2 4 6 8 10)", show(take(zipWith(from(1), from(1), Integer::sum), 5)));
  }

  @Test
  void zipWith_stopsAtShorterFiniteList() {
    LazyList<Integer> finite =
        LazyList.cons(10, () -> LazyList.cons(20, () -> LazyList.cons(30, LazyList::nil)));
    assertEquals("(11 22 33)", show(take(zipWith(from(1), finite, Integer::sum), 10)));
  }

  // -------------------------------------------------------------------------
  // Fibonacci identity
  // -------------------------------------------------------------------------

  @Test
  void fibs_pairSumsEqualNextFib() {
    // fib(n) + fib(n+1) = fib(n+2), so zipWith(+, fibs, tail(fibs)) = fibs shifted
    // by 2
    LazyList<Long> f = fibs();
    LazyList<Long> pairSums = zipWith(f, cdr(f), Long::sum);
    assertEquals("(1 2 3 5 8 13 21 34)", show(take(pairSums, 8)));
  }

  // -------------------------------------------------------------------------
  // primes (Sieve of Eratosthenes)
  // -------------------------------------------------------------------------

  @Test
  void primes_firstTen_returnsCorrectPrimes() {
    assertEquals("(2 3 5 7 11 13 17 19 23 29)", show(take(primes(), 10)));
  }

  @Test
  void primes_25th_is97() {
    // The 25th prime is 97
    LispList<Integer> first25 = take(primes(), 25);
    assertEquals(97, (int) Lisp.car(Lisp.drop(first25, 24)).orElseThrow());
  }
}
