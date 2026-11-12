package d021248.mytry.core.lisp.interpreter;

import java.util.ArrayList;
import java.util.List;

/// Recursive-descent parser for S-expressions.
///
/// Error messages include a character position (0-based) so callers can pinpoint problems in
/// longer programs. [#parse] additionally rejects trailing content after the first expression.
public final class SExprParser {
  private final String input;
  private int pos;

  private SExprParser(String input) {
    this.input = input;
    this.pos = 0;
  }

  /// Parses a single S-expression and verifies that no non-whitespace content follows.
  ///
  /// @throws LispError if the input is empty, malformed, or has trailing content
  public static SExpr parse(String input) {
    var p = new SExprParser(input.trim());
    SExpr result = p.readExpr();
    p.skipWs();
    if (p.pos < p.input.length()) {
      throw new LispError(
          "Unexpected content after expression at position "
              + p.pos
              + ": '"
              + p.input.charAt(p.pos)
              + "'");
    }
    return result;
  }

  /// Parses all S-expressions in the input (for multi-expression programs).
  public static List<SExpr> parseAll(String input) {
    var p = new SExprParser(input.trim());
    List<SExpr> exprs = new ArrayList<>();
    while (p.pos < p.input.length()) {
      p.skipWs();
      if (p.pos < p.input.length()) exprs.add(p.readExpr());
    }
    return exprs;
  }

  private SExpr readExpr() {
    skipWs();
    if (pos >= input.length()) throw err("Unexpected end of input");
    char c = input.charAt(pos);
    if (c == '(') return readList();
    // Reader macros: ' ` , ,@
    if (c == '\'') {
      pos++;
      return SExpr.list(SExpr.sym("quote"), readExpr());
    }
    if (c == '`') {
      pos++;
      return SExpr.list(SExpr.sym("quasiquote"), readExpr());
    }
    if (c == ',') {
      pos++;
      if (pos < input.length() && input.charAt(pos) == '@') {
        pos++;
        return SExpr.list(SExpr.sym("unquote-splicing"), readExpr());
      }
      return SExpr.list(SExpr.sym("unquote"), readExpr());
    }
    if (c == '"') return readString();
    return readAtom();
  }

  private SExpr readList() {
    int openPos = pos; // remember '(' position for error reporting
    pos++; // skip '('
    List<SExpr> items = new ArrayList<>();
    while (true) {
      skipWs();
      if (pos >= input.length())
        throw new LispError("Unterminated list opened at position " + openPos);
      if (input.charAt(pos) == ')') {
        pos++;
        return SExpr.list(items);
      }
      items.add(readExpr());
    }
  }

  private SExpr readString() {
    int openPos = pos; // remember '"' position for error reporting
    pos++; // skip opening "
    var sb = new StringBuilder();
    while (pos < input.length() && input.charAt(pos) != '"') {
      if (input.charAt(pos) == '\\' && pos + 1 < input.length()) {
        pos++;
        sb.append(
            switch (input.charAt(pos)) {
              case 'n' -> '\n';
              case 'r' -> '\r';
              case 't' -> '\t';
              case '"' -> '"';
              case '\\' -> '\\';
              default -> input.charAt(pos);
            });
      } else {
        sb.append(input.charAt(pos));
      }
      pos++;
    }
    if (pos >= input.length())
      throw new LispError("Unterminated string opened at position " + openPos);
    pos++; // skip closing "
    return SExpr.str(sb.toString());
  }

  private SExpr readAtom() {
    int start = pos;
    while (pos < input.length() && !isDelim(input.charAt(pos))) pos++;
    String tok = input.substring(start, pos);
    if (tok.equals("#t")) return SExpr.TRUE;
    if (tok.equals("#f")) return SExpr.FALSE;
    try {
      return SExpr.num(Double.parseDouble(tok));
    } catch (NumberFormatException _) {
      return SExpr.sym(tok);
    }
  }

  private void skipWs() {
    while (pos < input.length()) {
      char c = input.charAt(pos);
      if (Character.isWhitespace(c)) {
        pos++;
        continue;
      }
      if (c == ';') { // line comment: skip to end of line
        while (pos < input.length() && input.charAt(pos) != '\n') pos++;
        continue;
      }
      break;
    }
  }

  /// Builds a [LispError] that includes the current parser position.
  private LispError err(String msg) {
    return new LispError(msg + " at position " + pos);
  }

  private static boolean isDelim(char c) {
    return Character.isWhitespace(c) || c == '(' || c == ')' || c == '"' || c == ';';
  }
}
