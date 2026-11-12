package d021248.mytry.core.lisp.symbolic;

import static d021248.mytry.core.lisp.symbolic.Expr.*;

// sin, cos, exp, ln factory methods are included via the Expr.* wildcard import

/// Recursive-descent parser for infix mathematical expressions.
///
/// Grammar (precedence from lowest to highest):
///
/// ```
///   expr   = term (('+' | '-') term)*
///   term   = power (('*' | '/') power)*
///   power  = unary ('^' power)?        ← right-associative: x^2^3 = x^(2^3)
///   unary  = '-' unary | atom
///   atom   = NUMBER | VARIABLE | '(' expr ')'
///   NUMBER = [0-9]* ('.' [0-9]*)? ([eE] [+-]? [0-9]+)?
/// ```
///
/// Subtraction `a - b` is rewritten as `a + (-1 * b)`, and division `a / b` as
/// `a * b^(-1)`, so the resulting [Expr] tree uses only `Add`, `Mul`, and
/// `Pow`.
///
/// Examples:
///
///
///   - `"x^2 + 2*x + 1"` → `Add(Add(Pow(Var(x),2), Mul(Num(2),Var(x))), Num(1))`
///   - `"3*x^3 - x"` → `Add(Mul(Num(3),Pow(Var(x),3)), Mul(Num(-1),Var(x)))`
///
public final class ExprParser {

  private final String input;
  private int pos;

  private ExprParser(String input) {
    this.input = input;
    this.pos = 0;
  }

  /// Parses an infix expression string into an [Expr] tree.
  ///
  /// @param input the expression string
  /// @return the parsed expression
  /// @throws IllegalArgumentException if the input is malformed
  public static Expr parse(String input) {
    var parser = new ExprParser(input.strip());
    Expr result = parser.parseExpr();
    parser.skipWhitespace();
    if (parser.pos < parser.input.length()) {
      throw new IllegalArgumentException(
          "Unexpected character at position "
              + parser.pos
              + ": '"
              + parser.input.charAt(parser.pos)
              + "'");
    }
    return result;
  }

  // ---- Grammar rules -------------------------------------------------------

  // expr = term (('+' | '-') term)*
  private Expr parseExpr() {
    Expr left = parseTerm();
    while (true) {
      skipWhitespace();
      if (match('+')) {
        left = add(left, parseTerm());
      } else if (match('-')) {
        left = add(left, mul(num(-1), parseTerm()));
      } else {
        return left;
      }
    }
  }

  // term = power (('*' | '/') power)*
  private Expr parseTerm() {
    Expr left = parsePower();
    while (true) {
      skipWhitespace();
      if (match('*')) {
        left = mul(left, parsePower());
      } else if (match('/')) {
        left = mul(left, pow(parsePower(), -1));
      } else {
        return left;
      }
    }
  }

  // power = unary ('^' power)? ← right-recursive for right-associativity
  // x^2^3 is parsed as x^(2^3) = x^8, matching standard mathematical convention.
  private Expr parsePower() {
    Expr base = parseUnary();
    skipWhitespace();
    if (match('^')) {
      // Recurse into parsePower() (not parseUnary()) for right-associativity.
      Expr exponent = parsePower();
      if (exponent instanceof Expr.Num(double n)) {
        return pow(base, n);
      }
      throw new IllegalArgumentException("Exponent must be a numeric constant, got: " + exponent);
    }
    return base;
  }

  // unary = '-' unary | atom
  private Expr parseUnary() {
    skipWhitespace();
    if (match('-')) {
      return mul(num(-1), parseUnary());
    }
    return parseAtom();
  }

  // atom = NUMBER | VARIABLE | '(' expr ')'
  private Expr parseAtom() {
    skipWhitespace();
    if (pos >= input.length()) throw new IllegalArgumentException("Unexpected end of expression");

    char c = input.charAt(pos);

    // Parenthesized sub-expression
    if (c == '(') {
      pos++;
      Expr inner = parseExpr();
      skipWhitespace();
      if (!match(')')) throw new IllegalArgumentException("Missing closing parenthesis");
      return inner;
    }

    // Number: integer, decimal, or scientific notation (e.g. 1e3, 2.5e-4, .5E+2)
    if (Character.isDigit(c) || c == '.') {
      int start = pos;
      // Digits and decimal point
      while (pos < input.length()
          && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
        pos++;
      }
      // Optional exponent: e/E followed by optional +/- and digits
      if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
        pos++; // consume 'e'/'E'
        if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
          pos++; // consume sign
        }
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
          throw new IllegalArgumentException(
              "Invalid scientific notation: expected digits after exponent at position " + pos);
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
      }
      return num(Double.parseDouble(input.substring(start, pos)));
    }

    // Variable or function call: sin(u), cos(u), exp(u), ln(u)
    if (Character.isLetter(c)) {
      int start = pos;
      while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
        pos++;
      }
      String name = input.substring(start, pos);
      // Known transcendental functions — consume '(' arg ')'
      if (name.equals("sin") || name.equals("cos") || name.equals("exp") || name.equals("ln")) {
        skipWhitespace();
        if (!match('('))
          throw new IllegalArgumentException(name + ": expected '(' at position " + pos);
        Expr arg = parseExpr();
        skipWhitespace();
        if (!match(')'))
          throw new IllegalArgumentException(name + ": expected ')' at position " + pos);
        return switch (name) {
          case "sin" -> sin(arg);
          case "cos" -> cos(arg);
          case "exp" -> exp(arg);
          case "ln" -> ln(arg);
          default -> throw new AssertionError("unreachable");
        };
      }
      return var(name);
    }

    throw new IllegalArgumentException("Unexpected character at position " + pos + ": '" + c + "'");
  }

  // ---- Helpers -------------------------------------------------------------

  private void skipWhitespace() {
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
  }

  private boolean match(char expected) {
    if (pos < input.length() && input.charAt(pos) == expected) {
      pos++;
      return true;
    }
    return false;
  }
}
