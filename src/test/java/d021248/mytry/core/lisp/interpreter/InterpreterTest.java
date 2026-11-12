package d021248.mytry.core.lisp.interpreter;

import static d021248.mytry.core.lisp.interpreter.LispInterpreter.run;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InterpreterTest {

  @Test
  void arithmetic_addition() {
    assertEquals("6", run("(+ 1 2 3)").toString());
  }

  @Test
  void arithmetic_subtraction() {
    assertEquals("3", run("(- 10 4 3)").toString());
  }

  @Test
  void arithmetic_multiplication() {
    assertEquals("24", run("(* 2 3 4)").toString());
  }

  @Test
  void arithmetic_division() {
    assertEquals("5", run("(/ 10 2)").toString());
  }

  @Test
  void arithmetic_nested() {
    assertEquals("14", run("(* 2 (+ 3 4))").toString());
  }

  @Test
  void comparison_equal() {
    assertEquals("#t", run("(= 3 3)").toString());
  }

  @Test
  void comparison_lessThan() {
    assertEquals("#t", run("(< 1 2)").toString());
  }

  @Test
  void comparison_greaterThan() {
    assertEquals("#f", run("(> 1 2)").toString());
  }

  @Test
  void quote_list() {
    assertEquals("(1 2 3)", run("'(1 2 3)").toString());
  }

  @Test
  void quote_symbol() {
    assertEquals("x", run("'x").toString());
  }

  @Test
  void if_true() {
    assertEquals("1", run("(if #t 1 2)").toString());
  }

  @Test
  void if_false() {
    assertEquals("2", run("(if #f 1 2)").toString());
  }

  @Test
  void lambda_square() {
    assertEquals("25", run("((lambda (x) (* x x)) 5)").toString());
  }

  @Test
  void define_variable() {
    assertEquals("42", run("(define x 42) x").toString());
  }

  @Test
  void define_function() {
    assertEquals(
        "120", run("(define (fact n) (if (= n 0) 1 (* n (fact (- n 1))))) (fact 5)").toString());
  }

  @Test
  void define_shorthand() {
    assertEquals("25", run("(define (square x) (* x x)) (square 5)").toString());
  }

  @Test
  void let_binding() {
    assertEquals("7", run("(let ((a 3) (b 4)) (+ a b))").toString());
  }

  @Test
  void letStar_sequential() {
    assertEquals("10", run("(let* ((a 3) (b (+ a 4))) (+ a b))").toString());
  }

  @Test
  void letrec_recursive() {
    assertEquals(
        "120",
        run("""
				(letrec ((fact (lambda (n) (if (= n 0) 1 (* n (fact (- n 1)))))))
				  (fact 5))
				""")
            .toString());
  }

  @Test
  void car_test() {
    assertEquals("1", run("(car '(1 2 3))").toString());
  }

  @Test
  void cdr_test() {
    assertEquals("(2 3)", run("(cdr '(1 2 3))").toString());
  }

  @Test
  void cons_test() {
    assertEquals("(0 1 2)", run("(cons 0 '(1 2))").toString());
  }

  @Test
  void null_empty() {
    assertEquals("#t", run("(null? '())").toString());
  }

  @Test
  void null_nonEmpty() {
    assertEquals("#f", run("(null? '(1))").toString());
  }

  @Test
  void closure_captures_env() {
    assertEquals(
        "15",
        run("""
				(define (make-adder n) (lambda (x) (+ n x)))
				(define add10 (make-adder 10))
				(add10 5)
				""")
            .toString());
  }

  @Test
  void map_builtin() {
    assertEquals("(1 4 9)", run("(map (lambda (x) (* x x)) '(1 2 3))").toString());
  }

  @Test
  void filter_builtin() {
    assertEquals("(3 4 5)", run("(filter (lambda (x) (> x 2)) '(1 2 3 4 5))").toString());
  }

  @Test
  void reduce_builtin() {
    assertEquals("15", run("(reduce + 0 '(1 2 3 4 5))").toString());
  }

  @Test
  void cond_form() {
    assertEquals(
        "\"medium\"",
        run("""
				(define x 5)
				(cond ((< x 0) "negative") ((< x 10) "medium") (else "large"))
				""")
            .toString());
  }

  @Test
  void and_shortCircuit() {
    assertEquals("#f", run("(and 1 #f 3)").toString());
  }

  @Test
  void or_shortCircuit() {
    assertEquals("1", run("(or #f 1 2)").toString());
  }

  @Test
  void begin_returnsLast() {
    assertEquals("3", run("(begin 1 2 3)").toString());
  }

  @Test
  void recursive_map_in_lisp() {
    assertEquals(
        "(1 4 9)",
        run("""
				(define (my-map f lst)
				  (if (null? lst) '()
				    (cons (f (car lst)) (my-map f (cdr lst)))))
				(my-map (lambda (x) (* x x)) '(1 2 3))
				""")
            .toString());
  }

  @Test
  void fibonacci_in_lisp() {
    assertEquals(
        "55",
        run("""
				(define (fib n) (if (< n 2) n (+ (fib (- n 1)) (fib (- n 2)))))
				(fib 10)
				""")
            .toString());
  }

  @Test
  void setMutatesBinding() {
    assertEquals("99", run("(define x 1) (set! x 99) x").toString());
  }
}
