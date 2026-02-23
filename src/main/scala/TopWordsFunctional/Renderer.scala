package TopWordsFunctional

trait Renderer:

  def renderTop(n: Int, counts: Map[String, Int]): String =
    counts.toSeq
      .sortBy(-_._2)
      .take(n)
      .map((word, count) => s"$word $count")
      .mkString("\n")
