package example

import org.scalatest._

object Slow extends Tag("Slow")
object Unreliable extends Tag("Unreliable")

class HelloSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    info("", Some(tags))
  }

  "The Hello object" should "say hello" taggedAs (Slow) in {
    assert(true)
  }

  it should "sing lullaby" taggedAs (Unreliable, Slow) in {
    assert(true)
  }
}
