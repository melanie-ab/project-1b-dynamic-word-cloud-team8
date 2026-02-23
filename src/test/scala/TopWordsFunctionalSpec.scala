package TopWordsFunctional

import org.scalatest.funsuite.AnyFunSuite

class TopWordsFunctionalSpec extends AnyFunSuite:

  test("word counting accumulates correctly"):
    val lines = Iterator("hello world", "hello scala")
    val counter = new WordCounter {}
    val results = counter.countWords(lines).toSeq

    assert(results.last("hello") == 2)
    assert(results.last("world") == 1)

  test("scanLeft produces intermediate states"):
    val lines = Iterator("a", "b")
    val counter = new WordCounter {}
    val results = counter.countWords(lines).toSeq

    assert(results.size == 3)

  test("render top words"):
    val renderer = new Renderer {}
    val result = renderer.renderTop(1, Map("a" -> 5, "b" -> 2))

    assert(result.contains("a 5"))

