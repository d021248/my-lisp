package d021248.mytry.core.lisp.list;

import java.util.function.Supplier;

/// Lazy (call-by-need) singly-linked list.
///
/// Unlike [LispList], the tail of each [Cons] node is wrapped in a [Supplier] —
/// it is only evaluated when [Supplier#get()] is called. This enables the construction of
/// **infinite sequences** without exhausting memory.
///
/// **Memoization:** The [#cons] factory method wraps every tail supplier in a {@link
/// Memo}, so each node's tail is evaluated *at most once* (call-by-need semantics). Without
/// memoization, traversing the same prefix twice would re-evaluate every closure, which could be
/// expensive for complex generators.
///
/// Operations on `LazyList` are provided by [Lazy].
///
/// **Note on `permits` ordering:** same javac bootstrap constraint as [LispList] —
/// `Nil` must appear before `Cons`.
public sealed interface LazyList<T> permits LazyList.Nil, LazyList.Cons {

  /// The empty lazy list.
  ///
  /// The type parameter `T` is required for type-system compatibility; the singleton cast
  /// is safe because no data is stored.
  record Nil<T>() implements LazyList<T> {

    private static final Nil<?> INSTANCE = new Nil<>();
  }

  /// A non-empty lazy list node.
  ///
  /// The tail `cdr` is a [Supplier]: it is not evaluated until [Supplier#get()]
  /// is called. When created via [LazyList#cons], the supplier is always a [Memo], so
  /// the tail is computed at most once.
  record Cons<T>(T car, Supplier<LazyList<T>> cdr) implements LazyList<T> {}

  /// Thread-safe memoizing supplier — evaluates its delegate exactly once and caches the result.
  ///
  /// Setting `fn = null` after the first evaluation allows the captured closure (and any
  /// objects it references) to be garbage-collected.
  final class Memo<T> implements Supplier<T> {
    private volatile Supplier<T> fn;
    private T value;

    Memo(Supplier<T> fn) {
      this.fn = fn;
    }

    @Override
    public T get() {
      if (fn != null) {
        synchronized (this) {
          if (fn != null) {
            value = fn.get();
            fn = null; // release closure for GC
          }
        }
      }
      return value;
    }
  }

  /// Returns the empty lazy list singleton.
  @SuppressWarnings("unchecked")
  static <T> LazyList<T> nil() {
    return (LazyList<T>) Nil.INSTANCE;
  }

  /// Returns a lazy list node with `head` and a memoized lazily-evaluated `tail`.
  ///
  /// The tail supplier is wrapped in a [Memo] so it is evaluated at most once, regardless
  /// of how many times [Cons#cdr() cdr().get()] is called.
  static <T> LazyList<T> cons(T head, Supplier<LazyList<T>> tail) {
    return new Cons<>(head, new Memo<>(tail));
  }
}
