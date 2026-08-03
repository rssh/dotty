//> using options -source:3.11

// Trailing comma syntax for tuple types and values.
// `(a,)` is a single-element tuple, `(,)` is the empty tuple, and a trailing comma is accepted at
// any arity. Below 3.11 a trailing comma before a closing paren is discarded (SIP-27); see
// tests/pos/trailingCommas/trailingCommas.scala for that behaviour at the default source version.

object TupleTrailingComma:
  // Type tuples with trailing comma
  type T1 = (Int,)                    // single-element type tuple
  type T2 = (Int, String,)            // trailing comma with multiple elements
  type T3 = (Int, String)             // regular tuple (should still work)

  // Value tuples with trailing comma
  val v1: (Int,) = (1,)               // single-element value tuple
  val v2 = (1, 2,)                    // trailing comma with multiple elements
  val v3 = (1, 2)                     // regular tuple (should still work)

  // A single-element tuple really is Tuple1
  val checkV1: Int *: EmptyTuple = v1
  val checkV1Elem: Int = v1(0)

  // Pattern matching with trailing comma
  def test(x: Any): Unit = x match
    case (a,) => println(s"single: $a")
    case (a, b,) => println(s"pair: $a, $b")
    case _ => println("other")

  // With newlines - trailing comma should still be recognized
  val v4: (Int,
  ) = (1,
  )

  val v5: (Int, String,
  ) = (1, "hello",
  )

  type T4 = (Int,
  )

  type T5 = (Int, String,
  )

  def test2(x: Any): Unit = x match
    case (a,
    ) => println(s"single with newline: $a")
    case (a, b,
    ) => println(s"pair with newline: $a, $b")
    case _ => println("other")

  // Empty tuple syntax
  val empty1: (,) = (,)
  val empty2: (,
  ) = (,
  )

  type EmptyT = (,)

  val checkEmpty: EmptyTuple = empty1

  def testEmpty(x: Any): Unit = x match
    case (,) => println("empty tuple")
    case _ => println("other")

  // Named elements accept the comma too, though it is redundant there: a lone named element was
  // already a one-element named tuple, so nothing about these changed at 3.11.
  val n1 = (name = 1,)
  val checkN1: (name: Int) = n1

  type NT1 = (name: Int,)
  val n2: NT1 = n1

  val n3 = (id = 1, label = "x",)
  val checkN3: (id: Int, label: String) = n3

  val n4 = (name = 1,
  )
  val checkN4: (name: Int) = n4

  def testNamed(x: (name: Int)): Int = x match
    case (name = v,) => v
