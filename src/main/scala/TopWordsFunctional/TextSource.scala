package TopWordsFunctional

trait TextSource:
  def lines: Iterator[String]
