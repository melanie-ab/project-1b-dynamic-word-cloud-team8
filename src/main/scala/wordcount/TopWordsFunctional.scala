
package wordcount

import scala.io.Source
import scala.collection.mutable
import scala.util.Try

// Trait for handling configuration parameters
trait Config:
  def handleArgs(args: Array[String]): Map[String, Int] =
    val defaults = Map(
      "cloud-size" -> 10,
      "length-at-least" -> 6,
      "window-size" -> 1000,
      "min-frequency" -> 3,
      "update-every" -> 1
    )

    def safeParseInt(value: String, default: Int) = Try(value.toInt).getOrElse(default)

    val collectedArgs = args.grouped(2).collect {
      case Array("-c", v) | Array("--cloud-size", v) => "cloud-size" -> safeParseInt(v, defaults("cloud-size"))
      case Array("-l", v) | Array("--length-at-least", v) => "length-at-least" -> safeParseInt(v, defaults("length-at-least"))
      case Array("-w", v) | Array("--window-size", v) => "window-size" -> safeParseInt(v, defaults("window-size"))
      case Array("-f", v) | Array("--min-frequency", v) => "min-frequency" -> safeParseInt(v, defaults("min-frequency"))
      case Array("-u", v) | Array("--update-every", v) => "update-every" -> safeParseInt(v, defaults("update-every"))
    }.toMap

    defaults ++ collectedArgs

// Trait for word processing logic using a mutable queue + map (scalable)
trait WordProcessor:
  def processWords(
      words: Iterator[String],
      ignoredWords: Set[String],
      minLength: Int,
      windowSize: Int,
      updateEvery: Int
  ): Iterator[Map[String, Int]] =
    val window = mutable.Queue[String]()
    val counts = mutable.Map[String, Int]().withDefaultValue(0)
    var counter = 0

    words
      .map(_.toLowerCase.trim)
      .filter(w => w.length >= minLength && !ignoredWords.contains(w))
      .flatMap { word =>
        // Add new word
        window.enqueue(word)
        counts(word) += 1
        if window.size > windowSize then
          val removed = window.dequeue()
          counts(removed) -= 1
          if counts(removed) == 0 then counts -= removed

        counter += 1
        // Only yield when window is full and updateEvery reached
        if window.size == windowSize && counter % updateEvery == 0 then
          Some(counts.toMap)
        else None
      }

// Trait for output handling
trait OutputHandler:
  def doOutput(output: String): Unit =
    try println(output)
    catch
      case _: java.io.IOException => sys.exit(0)

  def printWordCloud(wordCounts: Map[String, Int], cloudSize: Int, minFrequency: Int): String =
    wordCounts
      .filter(_._2 >= minFrequency)
      .toSeq
      .sortBy { case (w, c) => (-c, w) }
      .take(cloudSize)
      .map { case (w, c) => s"$w: $c" }
      .mkString(" ")

// Main object combining all traits
object TopWordsFunctional extends Config with WordProcessor with OutputHandler:

  def main(args: Array[String]): Unit =
    val conf = handleArgs(args)
    val cloudSize = conf("cloud-size")
    val minLength = conf("length-at-least")
    val windowSize = conf("window-size")
    val minFrequency = conf("min-frequency")
    val updateEvery = conf("update-every")

    println(s"[main] DEBUG TopWordsFunctional - cloudSize=$cloudSize, minLength=$minLength, windowSize=$windowSize, minFrequency=$minFrequency, updateEvery=$updateEvery")

    // Read ignore-list file
    val ignoredWords =
      try Source.fromFile("ignore-list").getLines().map(_.trim.toLowerCase).filter(_.nonEmpty).toSet
      catch
        case _: Exception => Set.empty[String]
    println(s"[main] DEBUG Ignored Words: ${ignoredWords.mkString(", ")}")

    // Read stdin and split words
    val words = Source.stdin.getLines().flatMap(_.split("""(?U)[^\p{Alpha}0-9']+"""))

    // Process words and print word clouds
    processWords(words, ignoredWords, minLength, windowSize, updateEvery)
      .foreach { counts =>
        val cloud = printWordCloud(counts, cloudSize, minFrequency)
        doOutput(cloud)
      }
