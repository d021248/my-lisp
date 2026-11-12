package d021248.mytry.core.lisp.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

/// Immutable singly-linked list modelled as an algebraic data type (ADT).
///
/// The two variants — [Nil] and [Cons] — cover all possible states exhaustively. The
/// `sealed` modifier lets the compiler verify at every `switch` that both cases are
/// handled.
///
/// Implements [Iterable] so that instances can be used in for-each loops and with {@link
/// java.util.stream.StreamSupport#stream(java.util.Spliterator, boolean)}.
///
/// **Note on `permits` ordering:** `Nil` must appear before `Cons` to avoid
/// a javac bootstrap issue. The first entry in the `permits` clause triggers member-type
/// registration for the class being compiled; subsequent entries rely on that registration already
/// being in place.
public sealed interface LispList<T> extends Iterable<T> permits LispList.Nil, LispList.Cons {

  /// The empty list (analogous to Lisp's `nil`).
  ///
  /// The type parameter `T` is required even though `Nil` carries no data, because
  /// Java's type system demands that every permitted subtype is assignment-compatible with {@code
  /// LispList<T>} for all `T`. Holds a singleton instance since all `Nil<T>` are
  /// semantically identical after type erasure — avoids unnecessary allocations. `private` is
  /// valid here because records are classes, not interfaces.
  record Nil<T>() implements LispList<T> {
    private static final Nil<?> INSTANCE = new Nil<>();
  }

  /// A non-empty list node: a head element (`car`) and a tail (`cdr`).
  record Cons<T>(T car, LispList<T> cdr) implements LispList<T> {}

  /// Returns the empty-list singleton. Safe to cast: `Nil<T>` carries no data, so all
  /// instances are identical after type erasure — the same pattern used by {@link
  /// java.util.Collections#emptyList()}.
  @SuppressWarnings("unchecked")
  static <T> LispList<T> nil() {
    return (LispList<T>) Nil.INSTANCE;
  }

  /// Returns a new list with `head` prepended to `tail`.
  static <T> LispList<T> cons(T head, LispList<T> tail) {
    return new Cons<>(head, tail);
  }

  /// Returns a Lisp-style string representation, e.g. `"(1 2 3)"`.
  ///
  /// Delegates to [Lisp#show(LispList)]. Java does not permit interfaces to override {@link
  /// Object#toString()} via default methods (JLS §9.4.3), so this is the next best option — call
  /// `list.show()` instead of `Lisp.show(list)`.
  default String show() {
    return Lisp.show(this);
  }

  /// Returns an iterator over the elements of this list from head to tail. The iterator does not
  /// support [Iterator#remove()].
  @Override
  default Iterator<T> iterator() {
    return new Iterator<>() {
      private LispList<T> current = LispList.this;

      @Override
      public boolean hasNext() {
        return current instanceof LispList.Cons<T>;
      }

      @Override
      public T next() {
        if (current instanceof LispList.Cons<T>(T car, var cdr)) {
          current = cdr;
          return car;
        }
        throw new NoSuchElementException();
      }
    };
  }
}
