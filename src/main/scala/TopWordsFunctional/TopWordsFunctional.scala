package TopWordsFunctional

import scala.io.Source

object TopWordsFunctional {

  // Configuration trait
  trait Config {
    val cloudSize: Int
    val minFrequency: Int
    val updateEvery: Int
    val windowSize: Int
    val minLength: Int
  }

  // Default config with fallback values
  case class WordCloudConfig(
      cloudSize: Int = 15,
      minFrequency: Int = 5,
      updateEvery: Int = 20,
      windowSize: Int = 2000,
      minLength: Int = 8
  ) extends Config

  // Parse command-line args
  def handleArgs(args: Array[String]): WordCloudConfig = {
    val argMap = args.sliding(2, 2).collect {
      case Array(key, value) => key.stripPrefix("--") -> value.toInt
    }.toMap

    WordCloudConfig(
      cloudSize = argMap.getOrElse("cloud-size", 15),
      minFrequency = argMap.getOrElse("min-frequency", 5),
      updateEvery = argMap.getOrElse("update-every", 20),
      windowSize = argMap.getOrElse("window-size", 2000),
      minLength = argMap.getOrElse("length-at-least", 8)
    )
  }

  // Clean each word: remove punctuation and lowercase
  private def cleanWord(word: String | Null): String =
    Option(word).getOrElse("").nn
      .replaceAll("""[^\p{L}\p{N}]""", "").nn
      .toLowerCase.nn

  // Process input text into word frequencies
  def processWords(
      text: String | Null,
      ignoredWords: Set[String],
      minLength: Int,
      windowSize: Int,
      updateEvery: Int
  ): Map[String, Int] = {
    val safeText: String = Option(text).getOrElse("").nn

    // Split into words safely, then clean
    val words: Array[String] = safeText.split("\\s+").nn.map(cleanWord)

    words
      .filter(_.nn.nonEmpty)
      .filter(_.nn.length >= minLength)
      .filterNot(ignoredWords.contains)
      .groupBy(identity)
      .view
      .mapValues(_.length)
      .toMap
  }

  // Main method: read stdin and print top words
  def main(args: Array[String]): Unit = {
    val config = handleArgs(args)

    val inputText: String = Source.stdin.mkString.nn

    val ignoredWords: Set[String] = Set(
      "the", "and", "a", "of", "to", "in", "that", "it"
    )

    val result = processWords(
      inputText,
      ignoredWords,
      config.minLength,
      config.windowSize,
      config.updateEvery
    )

    val filtered = result.filter(_._2 >= config.minFrequency)

    filtered.toSeq.sortBy(-_._2).take(config.cloudSize).foreach {
      case (word, count) => println(s"$word: $count")
    }
  }
}

