package d021248.mytry.core.lisp.list;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/// Pure-function operations on [LazyList].
///
/// Operations that return a [LazyList] are themselves lazy — the tail is not evaluated
/// until consumed. Operations that must traverse the list ([#take], [#car], {@link
/// #cdr}) force evaluation up to the point they reach.
///
/// The bridge to the eager world is [#take]: it forces exactly `n` elements and
/// returns them as a [LispList] that can be passed to [Lisp#show] etc.
public final class Lazy {

  private Lazy() {}

  // -------------------------------------------------------------------------
  // Generators — produce infinite lazy lists
  // -------------------------------------------------------------------------

  /// Returns the infinite ascending integer sequence `n, n+1, n+2, ...`
  ///
  /// Example: `take(from(1), 5) → (1 2 3 4 5)`
  public static LazyList<Integer> from(int n) {
    return LazyList.cons(n, () -> from(n + 1));
  }

  /// Returns an infinite list by repeatedly applying `f` to the previous value, starting from
  /// `seed`.
  ///
  /// Example: `take(iterate(1, x -> x * 2), 8) → (1 2 4 8 16 32 64 128)`
  public static <T> LazyList<T> iterate(T seed, UnaryOperator<T> f) {
    return LazyList.cons(seed, () -> iterate(f.apply(seed), f));
  }

  /// Returns an infinite list of `value` repeated indefinitely.
  ///
  /// Example: `take(repeat(0), 4) → (0 0 0 0)`
  public static <T> LazyList<T> repeat(T value) {
    return LazyList.cons(value, () -> repeat(value));
  }

  /// Returns the infinite Fibonacci sequence: `0, 1, 1, 2, 3, 5, 8, 13, ...`
  ///
  /// Each node captures only two `long` values, so memory use is O(1) regardless of how
  /// many elements are consumed.
  public static LazyList<Long> fibs() {
    return fibsFrom(0L, 1L);
  }

  private static LazyList<Long> fibsFrom(long a, long b) {
    return LazyList.cons(a, () -> fibsFrom(b, a + b));
  }

  /// Returns the infinite list of prime numbers via the Sieve of Eratosthenes.
  ///
  /// Starting from the infinite list `2, 3, 4, ...`, each prime `p` lazily filters
  /// out all multiples of `p` from the remaining stream. Because both `filter` and the
  /// sieve itself are lazy, only the primes that are actually consumed are ever computed.
  ///
  /// Example: `take(primes(), 10) → (2 3 5 7 11 13 17 19 23 29)`
  ///
  /// ```
  /// sieve(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, ...)
  /// = 2 : sieve(filter(not %2) (3, 4, 5, 6, ...))
  /// = 2 : sieve(3, 5, 7, 9, 11, ...)
  /// = 2 : 3 : sieve(filter(not %3) (5, 7, 9, 11, ...))
  /// = 2 : 3 : sieve(5, 7, 11, ...)
  /// = 2 : 3 : 5 : ...
  /// ```
  ///
  /// **Note:** each new prime adds one filter closure to the chain. For the first few hundred
  /// primes this is fine; for very large `n` the nesting depth may cause a {@link
  /// StackOverflowError}.
  public static LazyList<Integer> primes() {
    return sieve(from(2));
  }

  private static LazyList<Integer> sieve(LazyList<Integer> list) {
    int p = car(list);
    return LazyList.cons(p, () -> sieve(filter(cdr(list), n -> n % p != 0)));
  }

  // -------------------------------------------------------------------------
  // Inspection
  // -------------------------------------------------------------------------

  /// Returns the head of the list.
  ///
  /// @throws NoSuchElementException if the list is empty
  public static <T> T car(LazyList<T> list) {
    return switch (list) {
      case LazyList.Nil<T> _ -> throw new NoSuchElementException("car of empty lazy list");
      case LazyList.Cons<T>(T car, _) -> car;
    };
  }

  /// Forces and returns the tail. Returns [LazyList#nil()] for an empty list. Equivalent to
  /// Lisp's `(cdr list)`.
  public static <T> LazyList<T> cdr(LazyList<T> list) {
    return switch (list) {
      case LazyList.Nil<T> _ -> LazyList.nil();
      case LazyList.Cons<T>(_, var cdr) -> cdr.get();
    };
  }

  // -------------------------------------------------------------------------
  // Materialising — force a prefix into an eager LispList
  // -------------------------------------------------------------------------

  /// Forces the first `n` elements and returns them as an eager [LispList]. Safe to call
  /// on infinite lists — only `n` elements are evaluated.
  public static <T> LispList<T> take(LazyList<T> list, int n) {
    if (n <= 0) return LispList.nil();
    return switch (list) {
      case LazyList.Nil<T> _ -> LispList.nil();
      case LazyList.Cons<T>(T car, var cdr) -> LispList.cons(car, take(cdr.get(), n - 1));
    };
  }

  // -------------------------------------------------------------------------
  // Lazy transformations — evaluate nothing upfront
  // -------------------------------------------------------------------------

  /// Returns a lazy list with `f` applied to each element. No elements are evaluated until
  /// consumed (e.g. by [#take]).
  public static <T, U> LazyList<U> map(LazyList<T> list, Function<T, U> f) {
    return switch (list) {
      case LazyList.Nil<T> _ -> LazyList.nil();
      case LazyList.Cons<T>(T car, var cdr) -> LazyList.cons(f.apply(car), () -> map(cdr.get(), f));
    };
  }

  /// Returns a lazy list of elements satisfying `pred`.
  ///
  /// Skipping non-matching elements is done iteratively (not recursively) to avoid stack overflow
  /// on lists with long runs of non-matching elements.
  public static <T> LazyList<T> filter(LazyList<T> list, Predicate<T> pred) {
    LazyList<T> current = list;
    while (current instanceof LazyList.Cons<T>(T car, var cdr)) {
      if (pred.test(car)) {
        final LazyList<T> rest = cdr.get();
        return LazyList.cons(car, () -> filter(rest, pred));
      }
      current = cdr.get();
    }
    return LazyList.nil();
  }

  /// Combines corresponding elements of two lazy lists with `f`, stopping at the shorter one.
  /// The result is itself lazy.
  ///
  /// Example: `take(zipWith(from(1), from(1), Integer::sum), 5) → (2 4 6 8 10)`
  public static <A, B, C> LazyList<C> zipWith(
      LazyList<A> as, LazyList<B> bs, BiFunction<A, B, C> f) {
    return switch (as) {
      case LazyList.Nil<A> _ -> LazyList.nil();
      case LazyList.Cons<A>(A a, var tailA) ->
          switch (bs) {
            case LazyList.Nil<B> _ -> LazyList.nil();
            case LazyList.Cons<B>(B b, var tailB) ->
                LazyList.cons(f.apply(a, b), () -> zipWith(tailA.get(), tailB.get(), f));
          };
    };
  }
}
