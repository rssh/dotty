
enum W[T] {
  case Success(value: T)
  case Failure(msg: String)
}

implicit def wToUnit[T](x:W[T]):Unit = {
  x match {
    case W.Success(_) =>
           System.out.println(s"discarding ${x}")
           ()
    case W.Failure(msg) => throw RuntimeException(msg)
  } 
}

object Test {



  def doA():W[String] = {
    W.Failure("A failure")
  }

  def doB():W[String] = {
    W.Success("B success")
  }

  def main(args:Array[String]): Unit = {
   try {
     doB()
     doA()
     System.out.println("finish")
   } catch {
     case e: RuntimeException =>
       System.out.println(e.getMessage)
   }
  }

}
