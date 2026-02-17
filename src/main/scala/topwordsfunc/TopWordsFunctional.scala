package topwordsfunc

object TopWordsFunctional {

  /** Process words with sliding window counts */
  def processWords(
      words: Iterator[String | Null],
      ignoredWords: Set[String],
      minLength: Int,
      windowSize: Int,
      updateEvery: Int
  ): Iterator[(Map[String, Int], Int)] = {

    val cleaned: Iterator[String] =
      words.collect { case w: String => w.nn }

    val processed: Iterator[String] =
      cleaned
        .map(w => w.toLowerCase.nn.trim.nn)
        .filter(w => w.length >= minLength && !ignoredWords.contains(w))

    processed
      .scanLeft[(List[String], Map[String, Int], Int)](
        (List.empty[String], Map.empty[String, Int], 0)
      ) { case ((window, counts, counter), word) =>

        val newWindow: List[String] =
          (word :: window).take(windowSize)

        val updatedCounts: Map[String, Int] =
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
      .filter { case (window, _, counter) =>
        window.size == windowSize && counter % updateEvery == 0
      }
      .map { case (_, counts, _) =>
        (counts, counts.size)
      }
  }

  /** Print a word cloud from counts */
  def printWordCloud(
      wordCounts: Map[String, Int],
      cloudSize: Int,
      minFrequency: Int
  ): String = {

    wordCounts
      .filter { case (_, count) => count >= minFrequency }
      .toSeq
      .sortBy { case (word, count) => (-count, word) }
      .take(cloudSize)
      .map { case (word, count) => s"$word: $count" }
      .mkString(" ")
  }

  /** Parse CLI args into a Map with defaults */
  def handleArgs(args: Array[String]): Map[String, Int] = {

    // Default values
    val defaults = Map(
      "cloud-size" -> 10,
      "min-frequency" -> 3,
      "update-every" -> 10,
      "window-size" -> 1000,
      "length-at-least" -> 6
    )

    // Parse args: expected as --key value
    args.sliding(2, 2).foldLeft(defaults) { (map, pair) =>
      pair match {
        case Array(key, value) if key.startsWith("--") =>
          val cleanKey = key.drop(2) // remove --
          try map + (cleanKey -> value.toInt)
          catch {
            case _: NumberFormatException => map // ignore invalid numbers
          }
        case _ => map
      }
    }
  }
}

