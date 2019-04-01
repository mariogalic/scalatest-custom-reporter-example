package org.scalatest.tools

import org.scalatest.events.{Event, InfoProvided}


class JUnitReporterWithTags extends JUnitXmlReporter("target"){
  private var tags: Map[String, Set[String]] = Map.empty

  override def apply(event: Event): Unit = {
    super.apply(event)
    event match {
      case e: InfoProvided =>
        e.payload.foreach { providedTags =>
          tags ++= providedTags.asInstanceOf[Map[String, Set[String]]]
        }

      case _ =>
    }
  }


  /**
    * name="The Hello object should say hello" classname="example.HelloSpec" time="0.006">
    *                ⬇
    * name="The Hello object should say hello" tag="Set(Slow)" classname="example.HelloSpec" time="0.006">
    */
  override def xmlify(testsuite: Testsuite): String = {
    var xml = super.xmlify(testsuite)
    for (testcase <- testsuite.testcases) yield {
      xml = xml.replace(s""""${testcase.name}"""", s""""${testcase.name}" tag="${tags(testcase.name)}"""" )
    }
    xml
  }

}
