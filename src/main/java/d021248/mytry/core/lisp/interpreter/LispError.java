package d021248.mytry.core.lisp.interpreter;

/// Runtime error in the Lisp interpreter.
public final class LispError extends RuntimeException {
  public LispError(String message) {
    super(message);
  }
}
