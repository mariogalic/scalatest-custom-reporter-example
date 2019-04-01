package example

import org.scalatest.{Args, Reporter}
import org.scalatest.events.{Event, InfoProvided, SuiteCompleted, TestSucceeded}
import org.scalatest._


/**
  * Skeleton to play around with custom reporters
  * https://stackoverflow.com/a/49210186/5205022
  */


class ExampleSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  object Smoke extends Tag("Smoke")
  object Mirror extends Tag("Mirror")

  override def beforeAll(): Unit = {
    info(s"at suite level - $tags")
    note(s"at suite level - $tags")
  }

  "A Stack" should "pop values in last-in-first-out order" taggedAs (Smoke) in {
    markup("thsi is markup lalalalalalala")
    alert("this is an alert", Some(tags))
    info(s"this is an info $tags", Some("info payload"))
    note("this is an note", Some("note payload"))
    assert(true)
  }

}

class MyReporter extends Reporter {
  override def apply(event: Event): Unit = {
    event match {
      case e: InfoProvided =>
      case e: SuiteCompleted =>
      case e: TestSucceeded =>
      case _ =>
    }
  }
}

object Hello extends App {
  val testSuite = new ExampleSpec
//  testSuite.execute()
  val reporter = new MyReporter
  val testNames = testSuite.testNames
  testNames.foreach(test => {
    testSuite.testDataFor(test).tags.foreach(println)
    val result = testSuite.run(Some(test), Args(reporter))
    val status = if (result.succeeds()) "OK" else "FAILURE!"
    println(s"Test: '$test'\n\tStatus=$status")
  })
}




