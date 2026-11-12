package d021248.mytry.core.lisp.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/// Pure-function operations on [LispList].
///
/// All methods are stateless and side-effect-free. Exhaustive pattern matching over the sealed
/// `LispList` variants guarantees at compile time that every case is handled.
///
/// **Recursion note:** most operations use direct recursion without trampolining and will
/// throw [StackOverflowError] on very long lists (typically beyond ~5 000 elements on a
/// default JVM stack). The exceptions are [#toList(LispList)] and [#fromList(List)],
/// which are iterative.
public final class Lisp {

  private Lisp() {}

  // -------------------------------------------------------------------------
  // Inspection
  // -------------------------------------------------------------------------

  /// Returns the head of the list, or [Optional#empty()] if the list is empty. Equivalent to
  /// Lisp's `(car list)`.
  public static <T> Optional<T> car(LispList<T> list) {
    return switch (list) {
      case LispList.Nil<T> _ -> Optional.empty();
      case LispList.Cons<T>(T car, _) -> Optional.of(car);
    };
  }

  /// Returns the tail of the list, or [LispList#nil()] if the list is empty. Equivalent to
  /// Lisp's `(cdr list)` — where `(cdr nil) = nil`.
  public static <T> LispList<T> cdr(LispList<T> list) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(_, var cdr) -> cdr;
    };
  }

  /// Returns the number of elements in the list.
  ///
  /// Implemented iteratively to avoid [StackOverflowError] on large lists. The recursive
  /// variant would overflow at roughly the JVM default stack depth (~5 000 frames); this version
  /// handles arbitrary lengths.
  public static <T> int length(LispList<T> list) {
    int n = 0;
    LispList<T> cur = list;
    while (cur instanceof LispList.Cons<T>(_, var cdr)) {
      n++;
      cur = cdr;
    }
    return n;
  }

  /// Returns a Lisp-style string representation, e.g. `"(1 2 3)"`.
  ///
  /// Uses the list's own [Iterable] implementation so the traversal logic is not duplicated
  /// here.
  public static <T> String show(LispList<T> list) {
    return StreamSupport.stream(list.spliterator(), false)
        .map(Object::toString)
        .collect(Collectors.joining(" ", "(", ")"));
  }

  // -------------------------------------------------------------------------
  // Structural operations
  // -------------------------------------------------------------------------

  /// Concatenates two lists. Equivalent to Lisp's `(append a b)`.
  ///
  /// Cost: O(|a|) — traverses `a`; `b` is shared structurally.
  public static <T> LispList<T> append(LispList<T> a, LispList<T> b) {
    return switch (a) {
      case LispList.Nil<T> _ -> b;
      case LispList.Cons<T>(T car, var cdr) -> LispList.cons(car, append(cdr, b));
    };
  }

  /// Returns the list with its elements in reverse order.
  ///
  /// Implemented via [#foldl] — demonstrates that higher-order operations can be composed
  /// from primitives.
  public static <T> LispList<T> reverse(LispList<T> list) {
    return foldl(list, LispList.nil(), (acc, x) -> LispList.cons(x, acc));
  }

  /// Returns the first `n` elements of the list. If `n` exceeds the list length, the
  /// full list is returned.
  public static <T> LispList<T> take(LispList<T> list, int n) {
    if (n <= 0) return LispList.nil();
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(T car, var cdr) -> LispList.cons(car, take(cdr, n - 1));
    };
  }

  /// Drops the first `n` elements and returns the remainder. If `n` exceeds the list
  /// length, the empty list is returned.
  public static <T> LispList<T> drop(LispList<T> list, int n) {
    if (n <= 0) return list;
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(_, var cdr) -> drop(cdr, n - 1);
    };
  }

  /// Returns the longest prefix whose elements satisfy `pred`.
  public static <T> LispList<T> takeWhile(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(T car, var cdr) ->
          pred.test(car) ? LispList.cons(car, takeWhile(cdr, pred)) : LispList.nil();
    };
  }

  /// Drops the longest prefix whose elements satisfy `pred` and returns the remainder.
  public static <T> LispList<T> dropWhile(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(T car, var cdr) -> pred.test(car) ? dropWhile(cdr, pred) : list;
    };
  }

  // -------------------------------------------------------------------------
  // Predicates
  // -------------------------------------------------------------------------

  /// Returns `true` if at least one element satisfies `pred`. Short-circuits.
  public static <T> boolean any(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> false;
      case LispList.Cons<T>(T car, var cdr) -> pred.test(car) || any(cdr, pred);
    };
  }

  /// Returns `true` if every element satisfies `pred`. Short-circuits. Vacuously {@code
  /// true} for the empty list.
  public static <T> boolean all(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> true;
      case LispList.Cons<T>(T car, var cdr) -> pred.test(car) && all(cdr, pred);
    };
  }

  /// Returns the first element satisfying `pred`, or [Optional#empty()] if none is
  /// found.
  public static <T> Optional<T> find(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> Optional.empty();
      case LispList.Cons<T>(T car, var cdr) -> pred.test(car) ? Optional.of(car) : find(cdr, pred);
    };
  }

  // -------------------------------------------------------------------------
  // Higher-order operations
  // -------------------------------------------------------------------------

  /// Applies `f` to every element and returns the resulting list. Equivalent to Lisp's {@code
  /// (mapcar f list)}.
  public static <T, U> LispList<U> map(LispList<T> list, Function<T, U> f) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(T car, var cdr) -> LispList.cons(f.apply(car), map(cdr, f));
    };
  }

  /// Returns a list of elements that satisfy `pred`. Equivalent to Lisp's {@code
  /// (remove-if-not pred list)}.
  public static <T> LispList<T> filter(LispList<T> list, Predicate<T> pred) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.nil();
      case LispList.Cons<T>(T car, var cdr) ->
          pred.test(car) ? LispList.cons(car, filter(cdr, pred)) : filter(cdr, pred);
    };
  }

  /// Applies `f` to each element and concatenates the resulting lists. Equivalent to {@code
  /// concatMap} in Haskell or `Stream.flatMap`.
  ///
  /// Implemented via [#foldr] so that [#append] always traverses only the (short)
  /// result of `f(x)`, not the growing accumulator.
  public static <T, U> LispList<U> flatMap(LispList<T> list, Function<T, LispList<U>> f) {
    return foldr(list, LispList.nil(), (x, acc) -> append(f.apply(x), acc));
  }

  /// Combines corresponding elements of two lists with `f`, stopping at the shorter one.
  ///
  /// Example: `zipWith((1 2 3), (10 20 30), +) → (11 22 33)`
  public static <A, B, C> LispList<C> zipWith(
      LispList<A> as, LispList<B> bs, BiFunction<A, B, C> f) {
    return switch (as) {
      case LispList.Nil<A> _ -> LispList.nil();
      case LispList.Cons<A>(A a, var tailA) ->
          switch (bs) {
            case LispList.Nil<B> _ -> LispList.nil();
            case LispList.Cons<B>(B b, var tailB) ->
                LispList.cons(f.apply(a, b), zipWith(tailA, tailB, f));
          };
    };
  }

  /// Zips two lists into a list of pairs, stopping at the shorter one.
  ///
  /// Each pair is represented as a [LispList] of two elements `(a b)`.
  ///
  /// Example: `zip((1 2 3), (a b c)) → ((1 a) (2 b) (3 c))`
  ///
  /// Implemented via [#zipWith].
  public static <A, B> LispList<LispList<Object>> zip(LispList<A> as, LispList<B> bs) {
    return zipWith(
        as,
        bs,
        (a, b) -> {
          LispList<Object> pair =
              LispList.cons((Object) a, LispList.cons((Object) b, LispList.nil()));
          return pair;
        });
  }

  /// Partitions a list into two lists: elements satisfying `pred` and elements that don't.
  ///
  /// The result is a [LispList] of two [LispList]s: `(matching non-matching)`.
  /// Relative order within each partition is preserved.
  ///
  /// Example: `partition((1 2 3 4 5), odd?) → ((1 3 5) (2 4))`
  ///
  /// Implemented in a single pass via [#foldl] for efficiency.
  public static <T> LispList<LispList<T>> partition(LispList<T> list, Predicate<T> pred) {
    // Accumulate in reverse order, then reverse both halves at the end
    record Acc<T>(LispList<T> yes, LispList<T> no) {}
    Acc<T> result =
        foldl(
            list,
            new Acc<>(LispList.nil(), LispList.nil()),
            (acc, x) ->
                pred.test(x)
                    ? new Acc<>(LispList.cons(x, acc.yes()), acc.no())
                    : new Acc<>(acc.yes(), LispList.cons(x, acc.no())));
    return LispList.cons(
        reverse(result.yes()), LispList.cons(reverse(result.no()), LispList.nil()));
  }

  // -------------------------------------------------------------------------
  // Folds
  // -------------------------------------------------------------------------

  /// Left fold: reduces the list to a single value by applying `f` left-to-right, starting
  /// from `identity`. Equivalent to Lisp's `(reduce f list :initial-value identity)`.
  ///
  /// Example: `foldl((1 2 3), 0, +) → ((0+1)+2)+3 = 6`
  public static <T, A> A foldl(LispList<T> list, A identity, BiFunction<A, T, A> f) {
    return switch (list) {
      case LispList.Nil<T> _ -> identity;
      case LispList.Cons<T>(T car, var cdr) -> foldl(cdr, f.apply(identity, car), f);
    };
  }

  /// Right fold: reduces the list to a single value by applying `f` right-to-left, starting
  /// from `identity`.
  ///
  /// Example: `foldr((1 2 3), nil, cons) → (1 2 3)` (identity for lists)
  public static <T, A> A foldr(LispList<T> list, A identity, BiFunction<T, A, A> f) {
    return switch (list) {
      case LispList.Nil<T> _ -> identity;
      case LispList.Cons<T>(T car, var cdr) -> f.apply(car, foldr(cdr, identity, f));
    };
  }

  /// Like [#foldl], but returns a list of all intermediate accumulator values including the
  /// seed.
  ///
  /// Example: `scanl((1 2 3), 0, +) → (0 1 3 6)`
  public static <T, A> LispList<A> scanl(LispList<T> list, A seed, BiFunction<A, T, A> f) {
    return switch (list) {
      case LispList.Nil<T> _ -> LispList.cons(seed, LispList.nil());
      case LispList.Cons<T>(T car, var cdr) ->
          LispList.cons(seed, scanl(cdr, f.apply(seed, car), f));
    };
  }

  // -------------------------------------------------------------------------
  // Sort
  // -------------------------------------------------------------------------

  /// Sorts the list using a merge sort. Stable: equal elements preserve their relative order.
  ///
  /// Cost: O(n log n) comparisons. Uses [#take]/[#drop] to split — readable but
  /// traverses the list three times per level. An idiomatic choice for a didactic implementation.
  public static <T> LispList<T> sort(LispList<T> list, Comparator<T> cmp) {
    int len = length(list);
    if (len <= 1) return list;
    int mid = len / 2;
    return merge(sort(take(list, mid), cmp), sort(drop(list, mid), cmp), cmp);
  }

  private static <T> LispList<T> merge(LispList<T> a, LispList<T> b, Comparator<T> cmp) {
    return switch (a) {
      case LispList.Nil<T> _ -> b;
      case LispList.Cons<T>(T headA, var tailA) ->
          switch (b) {
            case LispList.Nil<T> _ -> a;
            case LispList.Cons<T>(T headB, var tailB) ->
                cmp.compare(headA, headB) <= 0
                    ? LispList.cons(headA, merge(tailA, b, cmp))
                    : LispList.cons(headB, merge(a, tailB, cmp));
          };
    };
  }

  // -------------------------------------------------------------------------
  // Java interop
  // -------------------------------------------------------------------------

  /// Converts a [LispList] to a [List].
  ///
  /// Delegates to the list's own [Iterable] implementation (which is already iterative), so
  /// traversal logic is not duplicated here. Stack-safe for arbitrary lengths.
  public static <T> List<T> toList(LispList<T> list) {
    List<T> result = new ArrayList<>();
    list.forEach(result::add);
    return Collections.unmodifiableList(result);
  }

  /// Converts a [List] to a [LispList]. Implemented iteratively to avoid stack overflow
  /// on large lists.
  public static <T> LispList<T> fromList(List<T> list) {
    LispList<T> result = LispList.nil();
    for (int i = list.size() - 1; i >= 0; i--) {
      result = LispList.cons(list.get(i), result);
    }
    return result;
  }
}
