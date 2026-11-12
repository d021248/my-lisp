/// Immutable list ADTs and pure-function operations.
///
/// Contains two complementary list abstractions:
///
///
///   - [d021248.mytry.core.lisp.list.LispList] – eager, immutable singly-linked list as a
///       sealed ADT (`Nil` / `Cons`)
///   - [d021248.mytry.core.lisp.list.Lisp] – pure-function operations on eager lists ({@code
///       map}, `filter`, `foldl`, `foldr`, `zipWith`, `scanl`, {@code
///       sort}, …)
///   - [d021248.mytry.core.lisp.list.LazyList] – lazy (call-by-need) singly-linked list
///       enabling infinite sequences
///   - [d021248.mytry.core.lisp.list.Lazy] – generators (`from`, `iterate`,
///       `fibs`, `primes`) and lazy transformations on `LazyList`
///
/// @since 1.0
package d021248.mytry.core.lisp.list;
