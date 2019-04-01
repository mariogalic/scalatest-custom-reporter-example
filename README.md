# scalatest-custom-reporter-example

This expample shows how to create ScalaTest custom [`Reporter`](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/Reporter.html)
and pass [custom information](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/Informer.html) 
about a running suite of tests to the reporter for presentation to the user.

Also, it attempts to answer Stack Overflow question [How to add scalatest tags to a report](https://stackoverflow.com/questions/55323460/how-to-add-scalatest-tags-to-a-report)

## Tutorial

### Problem statement

[`JUnitXmlReporter`](https://github.com/scalatest/scalatest/blob/5fc7c4b05e1777ae64d915e0b029e9f1bfc79c3d/scalatest/src/main/scala/org/scalatest/tools/JUnitXmlReporter.scala) s
is responsible for generating report in JUnit's XML format when configured via [`-u` argument](http://www.scalatest.org/user_guide/using_the_runner):

```sbtshell
Test / testOptions += Tests.Argument("-u", "target")
```
 
Given the following test:
 
 ```scala
class HelloSpec extends FlatSpec with Matchers  {

  object Slow extends Tag("Slow")
  object Unreliable extends Tag("Unreliable")

  "The Hello object" should "say hello" taggedAs (Slow) in {
    assert(true)
  }

  it should "sing lullaby" taggedAs (Unreliable, Slow) in {
    assert(true)
  }
}
```
 by default,
[JUnitXmlReporter.xmlify](https://github.com/scalatest/scalatest/blob/5fc7c4b05e1777ae64d915e0b029e9f1bfc79c3d/scalatest/src/main/scala/org/scalatest/tools/JUnitXmlReporter.scala#L343)
outputs the following string:

```$xml
...
  <testcase 
    name="The Hello object should say hello" classname="example.HelloSpec" time="0.011">
  </testcase>
  <testcase 
    name="The Hello object should sing lullaby" classname="example.HelloSpec" time="0.001">
  </testcase>
...
```

while we would like to add test's [tags](http://www.scalatest.org/user_guide/tagging_your_tests) to the report like so:

```$xml
...
  <testcase 
    name="The Hello object should say hello" tag="Set(Slow)" classname="example.HelloSpec" time="0.011">
  </testcase>
  <testcase 
    name="The Hello object should sing lullaby" tag="Set(Unreliable, Slow)" classname="example.HelloSpec" time="0.001">
</testcase>
...
```

### How to create custom reporter?

1. Create custom reporter by extending `JUnitXmlReporter`:
    ```
    package org.scalatest.tools
    class JUnitReporterWithTags extends JUnitXmlReporter("target")
    ```
    
1. Add member map to hold suite's [`tags`](https://github.com/scalatest/scalatest/blob/5fc7c4b05e1777ae64d915e0b029e9f1bfc79c3d/scalatest/src/main/scala/org/scalatest/Suite.scala#L912) by test name:
    ```
    private var tags: Map[String, Set[String]] = Map.empty
    ```
    
1. Override `xmlify` to inject the tags in the output string:
    ```
      override def xmlify(testsuite: Testsuite): String = {
        var xml = super.xmlify(testsuite)
        for (testcase <- testsuite.testcases) yield {
          xml = xml.replace(s""""${testcase.name}"""", s""""${testcase.name}" tag="${tags(testcase.name)}"""" )
        }
        xml
      }

    ```

### How to pass suite's tags as custom information to the reporter?

1. Mixing [`BeforeAndAfterAll`](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/BeforeAndAfterAll.html) trait in the test:
    ```
    class HelloSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
    ```
1. Piggyback [`Suite.tags`](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/Suite.html#tags:Map[String,Set[String]]) 
as [`InfoProvided`](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/events/InfoProvided.html) event's `payload` argument which 
gets passed to reporter via [`Informer`](http://doc.scalatest.org/3.0.1-2.12/org/scalatest/Informer.html):
    ```
    override def beforeAll(): Unit = {
        info("", Some(tags))
      }
    ```
1. Override `JUnitXmlReporter.apply` to extract and store tags payload:
    ```
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
    ```
    
 ### How to configure SBT to produce the report via custom reporter?
 
1. Give fully qualified name of custom reporter `JUnitReporterWithTags` to `-C argument`:
    ```
    Test / testOptions += Tests.Argument("-C", "org.scalatest.tools.JUnitReporterWithTags")
    ```
1. Produce the report with `sbt test`
1. Report should be created at
    ```
    target/TEST-example.HelloSpec.xml
    ```
 
 
 
 


