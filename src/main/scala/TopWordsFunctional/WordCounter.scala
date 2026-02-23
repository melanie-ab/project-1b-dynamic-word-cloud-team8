package TopWordsFunctional

trait WordCounter:

  def countWords(lines: Iterator[String]): Iterator[Map[String, Int]] =
    lines
      .flatMap(_.toLowerCase.split("\\W+"))
      .filter(_.nonEmpty)
      .scanLeft(Map.empty[String, Int]) { (acc, word) =>
        acc.updated(word, acc.getOrElse(word, 0) + 1)
      }
