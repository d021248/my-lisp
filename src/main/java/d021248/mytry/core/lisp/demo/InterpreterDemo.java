package d021248.mytry.core.lisp.demo;

import static d021248.mytry.core.lisp.interpreter.LispInterpreter.run;

/// Demonstrates the mini Lisp interpreter.
public final class InterpreterDemo {

  public static void main() {
    System.out.println("=== Arithmetic ===");
    show("(+ 1 2 3)", run("(+ 1 2 3)"));
    show("(* 2 (+ 3 4))", run("(* 2 (+ 3 4))"));

    System.out.println("\n=== Lambda & Closures ===");
    show("((lambda (x) (* x x)) 5)", run("((lambda (x) (* x x)) 5)"));

    System.out.println("\n=== Define & Recursion ===");
    var factResult =
        run(
            """
				(define (fact n)
				  (if (= n 0) 1 (* n (fact (- n 1)))))
				(fact 10)
				""");
    show("(fact 10)", factResult);

    System.out.println("\n=== Let ===");
    show("(let ((a 3) (b 4)) (+ a b))", run("(let ((a 3) (b 4)) (+ a b))"));

    System.out.println("\n=== List operations ===");
    show("(car '(1 2 3))", run("(car '(1 2 3))"));
    show("(cdr '(1 2 3))", run("(cdr '(1 2 3))"));
    show("(cons 0 '(1 2 3))", run("(cons 0 '(1 2 3))"));

    System.out.println("\n=== Higher-order: map ===");
    show("(map (lambda (x) (* x x)) '(1 2 3 4 5))", run("(map (lambda (x) (* x x)) '(1 2 3 4 5))"));

    System.out.println("\n=== Higher-order: filter ===");
    show(
        "(filter (lambda (x) (> x 2)) '(1 2 3 4 5))",
        run("(filter (lambda (x) (> x 2)) '(1 2 3 4 5))"));

    System.out.println("\n=== Higher-order: reduce ===");
    show("(reduce + 0 '(1 2 3 4 5))", run("(reduce + 0 '(1 2 3 4 5))"));

    System.out.println("\n=== Recursive map in Lisp ===");
    var mapResult =
        run(
            """
				(define (my-map f lst)
				  (if (null? lst) '()
				    (cons (f (car lst)) (my-map f (cdr lst)))))
				(my-map (lambda (x) (* x x)) '(1 2 3 4 5))
				""");
    show("(my-map square '(1 2 3 4 5))", mapResult);

    System.out.println("\n=== FizzBuzz ===");
    var fizzBuzz =
        run(
            """
				(define (fizzbuzz n)
				  (cond
				    ((= (modulo n 15) 0) "FizzBuzz")
				    ((= (modulo n 3) 0)  "Fizz")
				    ((= (modulo n 5) 0)  "Buzz")
				    (else                 n)))
				(map fizzbuzz '(1 2 3 4 5 6 7 8 9 10 11 12 13 14 15))
				""");
    show("(map fizzbuzz '(1..15))", fizzBuzz);
  }

  private static void show(String expr, Object result) {
    System.out.printf("  %-50s => %s%n", expr, result);
  }
}
