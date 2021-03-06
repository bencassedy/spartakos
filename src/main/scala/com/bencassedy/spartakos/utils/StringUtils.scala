package com.bencassedy.spartakos.utils

import org.apache.spark.sql.functions._

/**
  * Common utility methods
  */
object StringUtils {

  val wordCounts = udf( (tokens: Seq[String]) => tokens.foldLeft(Map.empty[String, Int]){
    (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
  }.toSeq.sortBy(-_._2))

}
