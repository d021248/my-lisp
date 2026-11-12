/// # miniKanren — Relational Logic Programming
///
/// A miniature implementation of the miniKanren logic programming system.
/// Programs are written as *relations* that can be run in any direction:
/// the same goal that checks membership can also enumerate all members.
///
/// ## Core concepts
///
/// - `Term` — a logic term: a variable (`Var`), a ground value (`Val`), or a cons-pair (`Cons`)
/// - `Subst` — an immutable substitution mapping `Var`s to `Term`s
/// - `State` — a `(Subst, counter)` pair; the counter allocates fresh variables
/// - `Goal` — a function `State → Stream<State>`; zero states = failure, many = multiple answers
/// - `MiniKanren` — `run` and `runStar` entry points
///
/// ## Usage
///
/// ```java
/// import static d021248.mytry.core.lang.logic.Goal.*;
/// import static d021248.mytry.core.lang.logic.MiniKanren.*;
/// import static d021248.mytry.core.lang.logic.Term.*;
///
/// // "What number x satisfies x == 42?"
/// var answers = run(1, q -> unify(q, val(42)));
/// // answers == [Val(42)]
///
/// // Relational append: "What list appended to (1,2) gives (1,2,3)?"
/// ```
///
/// @since 1.0
/// @see d021248.mytry.core.lang.fp.cps
/// @see d021248.mytry.core.lang.parser
package d021248.mytry.core.lang.logic;
