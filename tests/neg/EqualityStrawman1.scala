package strawman.equality
import annotation.implicitNotFound

object EqualityStrawman1 {

  trait CanEqual[-T]

  @implicitNotFound("cannot compare value of type ${T} with a value outside its equality class")
  trait Impossible[T]

  object CanEqual {
    object derived extends CanEqual[Any]
  }

  trait Base {
    def === (other: Any): Boolean = this.equals(other)
    def === [T <: CondEquals](other: T)(implicit ce: Impossible[T]): Boolean = ???
  }

  trait CondEquals extends Base {
    def === [T >: this.type <: CondEquals](other: T)(implicit ce: CanEqual[T]): Boolean = this.equals(other)
    def === [T](other: T)(implicit ce: Impossible[T]): Boolean = ???
  }

  trait Equals[-T] extends CondEquals

  case class Str(str: String) extends CondEquals

  case class Num(x: Int) extends Equals[Num]

  case class Other(x: Int) extends Base

  trait Option[+T] extends CondEquals
  case class Some[+T](x: T) extends Option[T]
  case object None extends Option[Nothing]

  implicit def eqStr: CanEqual[Str] = CanEqual.derived
  //implicit def eqNum: CanEqual[Num] = CanEqual.derived
  implicit def eqOption[T: CanEqual]: CanEqual[Option[T]] = CanEqual.derived

  implicit def eqEq[T <: Equals[T]]: CanEqual[T] = CanEqual.derived

  def main(args: Array[String]): Unit = {
    val x = Str("abc")
    x === x

    val n = Num(2)
    val m = Num(3)
    n === m

    Other(1) === Other(2)

    Some(x) === None
    Some(x) === Some(Str(""))
    val z: Option[Str] = Some(Str("abc"))
    z === Some(x)
    z === None
    Some(x) === z
    None === z


    def ddistinct[T <: Base: CanEqual](xs: List[T]): List[T] = xs match {
      case Nil => Nil
      case x :: xs => x :: xs.filterNot(x === _)
    }

    ddistinct(List(z, z, z))

    x === n  // error
    n === x  // error
    x === Other(1)  // error
    Other(2) === x // error
    z === Some(n) // error
    z === n // error
    Some(n) === z // error
    n === z // error
    Other(1) === z // error
    z === Other(1) // error
    ddistinct(List(z, n)) // error
  }
}
