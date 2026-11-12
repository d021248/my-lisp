package d021248.mytry.core.lisp.demo;

import static d021248.mytry.core.lisp.list.Lazy.*;
import static d021248.mytry.core.lisp.list.Lisp.show;

import d021248.mytry.core.lisp.list.LazyList;

/// Demonstrates infinite lazy lists via [LazyList] and [Lazy].
///
/// None of these sequences are fully stored in memory — only the elements consumed by {@link
/// Lazy#take} are ever evaluated.
public final class LazyListDemo {

  public static void main() {
    // ---- Generators ---------------------------------------------------------

    // Natural numbers — infinite, only first 5 are forced
    System.out.println("nats:            " + show(take(from(1), 5)));
    // (1 2 3 4 5)

    // Powers of 2 via iterate(seed, f): seed, f(seed), f(f(seed)), ...
    System.out.println("powers of 2:     " + show(take(iterate(1, x -> x * 2), 8)));
    // (1 2 4 8 16 32 64 128)

    // Constant stream
    System.out.println("repeat 7:        " + show(take(repeat(7), 4)));
    // (7 7 7 7)

    // Fibonacci — O(1) memory per step, no memoisation needed
    System.out.println("fibs:            " + show(take(fibs(), 12)));
    // (0 1 1 2 3 5 8 13 21 34 55 89)

    // ---- Lazy transformations -----------------------------------------------

    // map is lazy: squares are only computed when take forces them
    System.out.println("squares:         " + show(take(map(from(1), x -> x * x), 6)));
    // (1 4 9 16 25 36)

    // filter is lazy: scans nats until it accumulates 5 even numbers
    System.out.println("evens:           " + show(take(filter(from(1), x -> x % 2 == 0), 5)));
    // (2 4 6 8 10)

    // zipWith combines two infinite streams element-by-element
    System.out.println(
        "nats + nats:     " + show(take(zipWith(from(1), from(1), Integer::sum), 5)));
    // (2 4 6 8 10)

    // ---- Sieve of Eratosthenes ----------------------------------------------

    // Infinite list of primes — each prime lazily filters its multiples
    System.out.println("primes:          " + show(take(primes(), 15)));
    // (2 3 5 7 11 13 17 19 23 29 31 37 41 43 47)

    // ---- Fibonacci identity -------------------------------------------------
    // fibs(n) + fibs(n+1) = fibs(n+2)
    // zipWith(+, fibs, tail(fibs)) should equal fibs shifted by 2
    LazyList<Long> f = fibs();
    LazyList<Long> pairSums = zipWith(f, cdr(f), Long::sum);
    System.out.println("fib(n)+fib(n+1): " + show(take(pairSums, 8)));
    // (1 2 3 5 8 13 21 34) ← fibs starting from index 2
  }
}
