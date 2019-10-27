import scala.quoted._

object scalatest {

  inline def assert(condition: => Boolean): Unit = ${ assertImpl('condition, '{""}) }

  def assertImpl(cond: Expr[Boolean], clue: Expr[Any])(given qctx: QuoteContext): Expr[Unit] = {
    import qctx.tasty.{_, given}

    cond.unseal.underlyingArgument match {
      case app @ Apply(sel @ Select(lhs, op), rhs :: Nil) =>
        val IsSelect(select) = sel
        val cond = Apply(Select.copy(select)(lhs, "exists"), rhs :: Nil).seal.cast[Boolean]
        '{ scala.Predef.assert($cond) }
      case _ =>
        '{ scala.Predef.assert($cond) }
    }
  }
}
