package d021248.mytry.core.lisp.demo;

import static d021248.mytry.core.lisp.list.Lisp.*;
import static d021248.mytry.core.lisp.list.LispList.*;

import d021248.mytry.core.lisp.list.LispList;
import java.util.Comparator;
import java.util.List;

/// Runnable demonstration of all [d021248.mytry.core.lisp.list.Lisp] operations.
public final class ListDemo {

  public static void main() {
    LispList<Integer> list = cons(1, cons(2, cons(3, nil())));

    // ---- Inspection ---------------------------------------------------------
    System.out.println("list:            " + show(list)); // (1 2 3)
    System.out.println("length:          " + length(list)); // 3
    System.out.println("car:             " + car(list).orElseThrow()); // 1
    System.out.println("cdr:             " + show(cdr(list))); // (2 3)

    // ---- Structural ---------------------------------------------------------
    System.out.println("cons 0:          " + show(cons(0, list))); // (0 1 2 3)
    System.out.println("append:          " + show(append(list, cons(4, nil())))); // (1 2 3 4)
    System.out.println("reverse:         " + show(reverse(list))); // (3 2 1)
    System.out.println("take 2:          " + show(take(list, 2))); // (1 2)
    System.out.println("drop 1:          " + show(drop(list, 1))); // (2 3)
    System.out.println("takeWhile <3:    " + show(takeWhile(list, x -> x < 3))); // (1 2)
    System.out.println("dropWhile <3:    " + show(dropWhile(list, x -> x < 3))); // (3)

    // ---- Predicates ---------------------------------------------------------
    System.out.println("any >2:          " + any(list, x -> x > 2)); // true
    System.out.println("all >0:          " + all(list, x -> x > 0)); // true
    System.out.println("find >1:         " + find(list, x -> x > 1)); // Optional[2]

    // ---- Higher-order -------------------------------------------------------
    System.out.println("map *2:          " + show(map(list, x -> x * 2))); // (2 4 6)
    System.out.println("filter even:     " + show(filter(list, x -> x % 2 == 0))); // (2)
    System.out.println(
        "flatMap dup:     " + show(flatMap(list, x -> cons(x, cons(x, nil()))))); // (1 1 2 2 3 3)
    System.out.println(
        "zipWith +10:     "
            + show(
                zipWith( // (11 22 33)
                    list, cons(10, cons(20, cons(30, nil()))), Integer::sum)));

    // ---- Folds --------------------------------------------------------------
    System.out.println("foldl sum:       " + foldl(list, 0, Integer::sum)); // 6
    System.out.println("foldr rebuild:   " + show(foldr(list, nil(), LispList::cons))); // (1 2 3)
    System.out.println("scanl sum:       " + show(scanl(list, 0, Integer::sum))); // (0 1 3 6)

    // ---- Sort ---------------------------------------------------------------
    LispList<Integer> unsorted = cons(3, cons(1, cons(4, cons(1, cons(5, nil())))));
    System.out.println(
        "sort asc:        " + show(sort(unsorted, Comparator.naturalOrder()))); // (1 1 3 4 5)
    System.out.println(
        "sort desc:       " + show(sort(unsorted, Comparator.reverseOrder()))); // (5 4 3 1 1)

    // ---- Java interop -------------------------------------------------------
    List<Integer> javaList = toList(list);
    System.out.println("toList:          " + javaList); // [1, 2, 3]
    System.out.println("fromList:        " + show(fromList(javaList))); // (1 2 3)
    System.out.print("for-each:        ");
    for (int x : list) System.out.print(x + " ");
    System.out.println(); // 1 2 3
  }
}
