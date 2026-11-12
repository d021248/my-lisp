package d021248.mytry.core.lisp.interpreter;

import java.util.HashMap;
import java.util.Map;

/// Lexical environment — a chain of scopes with mutable bindings.
///
/// Each scope holds a **mutable** [HashMap]. This is an intentional deviation from the
/// typical `record` contract (records are normally value-based and immutable): `define`
/// and `set!` need to mutate the bindings map of the current scope in place. The
/// *parent-chain structure* itself is immutable — [#child] creates a new scope without
/// modifying any existing one.
///
/// **Implication:** two `Env` instances with the same parent and an identical (but
/// separately created) bindings map are NOT `equals()` — record equality compares the map
/// reference, not its contents. This is acceptable because environments are identity-objects, not
/// value-objects.
public record Env(Map<String, SExpr> bindings, Env parent) {

  public Env() {
    this(new HashMap<>(), null);
  }

  /// No defensive copy — bindings must remain mutable for define/set!.
  public Env {}

  public SExpr lookup(String name) {
    SExpr val = bindings.get(name);
    if (val != null) return val;
    if (parent != null) return parent.lookup(name);
    throw new LispError("Unbound symbol: " + name);
  }

  /// Binds `name` to `val` in *this* scope. Equivalent to Scheme's {@code
  /// define}: always writes to the innermost (current) scope, regardless of whether an outer scope
  /// already has a binding for `name`.
  public SExpr define(String name, SExpr val) {
    bindings.put(name, val);
    return val;
  }

  /// Updates the nearest enclosing binding of `name` to `val`. Walks the scope chain and
  /// mutates the first scope that owns the binding. Equivalent to Scheme's `set!`.
  ///
  /// @throws LispError if `name` is unbound in all enclosing scopes
  public SExpr set(String name, SExpr val) {
    for (Env cur = this; cur != null; cur = cur.parent()) {
      if (cur.bindings.containsKey(name)) {
        cur.bindings.put(name, val);
        return val;
      }
    }
    throw new LispError("set!: unbound variable: " + name);
  }

  /// Creates a child scope whose bindings do not affect this scope.
  public Env child(Map<String, SExpr> childBindings) {
    return new Env(new HashMap<>(childBindings), this);
  }

  /// Creates an empty child scope.
  public Env child() {
    return new Env(new HashMap<>(), this);
  }
}
