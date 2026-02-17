package TopWordsFunctional

import scala.io.Source
import scala.util.Try
import scala.language.unsafeNulls

object TopWordsFunctional {

  /** Process words with sliding window counts */
  def processWords(
      words: Iterator[String | Null],
      ignoredWords: Set[String],
      minLength: Int,
      windowSize: Int,
      updateEvery: Int
  ): Iterator[(Map[String, Int], Int)] = {

    // Step 1: remove nulls
    val cleaned: Iterator[String] = words.collect { case w: String => w.nn }

    // Step 2: lowercase and trim
    val processed: Iterator[String] =
      cleaned.map(w => w.nn.trim.nn.toLowerCase.nn)
        .filter(w => w.length >= minLength && !ignoredWords.contains(w))

    // Sliding window counts
    processed
      .scanLeft[(List[String], Map[String, Int], Int)]((List.empty[String], Map.empty[String, Int], 0)) {
        case ((window, counts, counter), word) =>
          val newWindow = (word :: window).take(windowSize)
          val updatedCounts =
            if (window.size >= windowSize) {
              val removed = window.last
              val dec = counts.getOrElse(removed, 1) - 1
              val decremented =
                if (dec <= 0) counts - removed
                else counts + (removed -> dec)
              decremented + (word -> (decremented.getOrElse(word, 0) + 1))
            } else {
              counts + (word -> (counts.getOrElse(word, 0) + 1))
            }
          (newWindow, updatedCounts, counter + 1)
      }
      .drop(1)
      .filter { case (window, _, counter) => window.size == windowSize && counter % updateEvery == 0 }
      .map { case (_, counts, _) => (counts, counts.size) }
  }

  /** Print a word cloud from counts */
  def printWordCloud(wordCounts: Map[String, Int], cloudSize: Int, minFrequency: Int): String = {
    wordCounts
      .filter { case (_, count) => count >= minFrequency }
      .toSeq
      .sortBy { case (word, count) => (-count, word) }
      .take(cloudSize)
      .map { case (word, count) => s"$word: $count" }
      .mkString(" ")
  }

  /** Main CLI */
  def main(args: Array[String]): Unit = {

    var cloudSize = 10
    var minLength = 1
    var windowSize = 10
    var updateEvery = 1
    var minFrequency = 1

    // simple args parsing
    args.sliding(2, 2).foreach {
      case Array("--cloud-size", v) => cloudSize = Try(v.toInt).getOrElse(cloudSize)
      case Array("-c", v)           => cloudSize = Try(v.toInt).getOrElse(cloudSize)
      case Array("--length-at-least", v) => minLength = Try(v.toInt).getOrElse(minLength)
      case Array("-l", v)                 => minLength = Try(v.toInt).getOrElse(minLength)
      case Array("--window-size", v) => windowSize = Try(v.toInt).getOrElse(windowSize)
      case Array("-w", v)             => windowSize = Try(v.toInt).getOrElse(windowSize)
      case Array("--update-every", v) => updateEvery = Try(v.toInt).getOrElse(updateEvery)
      case Array("--min-frequency", v) => minFrequency = Try(v.toInt).getOrElse(minFrequency)
      case _ => ()
    }

    val lines = Source.stdin.getLines()

    val words = lines.flatMap(line => line.nn.split("(?U)[^\\p{Alpha}0-9']+").iterator.collect { case w: String => w.nn })
    val ignoredWords = Set.empty[String]

    val wordClouds = processWords(words, ignoredWords, minLength, windowSize, updateEvery)

    // Print the last word cloud snapshot
    wordClouds.toSeq.lastOption.foreach { case (counts, _) =>
      println(printWordCloud(counts, cloudSize, minFrequency))
    }
  }
}

