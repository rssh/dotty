//> using options -source:3.11-migration

// Under 3.11-migration the pre-3.11 meaning is kept -- these are Int, not Tuple1[Int] -- and every
// affected site is reported. `-rewrite` removes the comma; see tests/rewrites/tuple-trailing-comma.

object Value:
  val x: Int = (1,     // warn
  )

object Type:
  type T = (Int,       // warn
  )
  val y: T = 42

object Pattern:
  // no `case _` here: under the retained pre-3.11 meaning `(a,<nl>)` is just the variable pattern
  // `a`, so a catch-all would be unreachable and warn about that instead.
  def f(x: Any): Any = x match
    case (a,           // warn
    ) => a

// Not affected, so not warned about:

object Unaffected:
  // more than one element: already a tuple before and after
  val pair = (1, 2,
  )
  val checkPair: (Int, Int) = pair

  // named element: a lone NamedArg was already a tuple before and after
  val named = (name = 1,
  )
  val checkNamed: (name: Int) = named

  // ordinary trailing commas outside tuple contexts
  val list = List(
    1,
    2,
  )
  def g(
    a: Int,
    b: String,
  ) = a
