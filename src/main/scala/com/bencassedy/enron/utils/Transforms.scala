package com.bencassedy.enron.utils

import com.bencassedy.enron.config.Config
import com.bencassedy.enron.utils.EnronUtils._
import org.apache.spark.ml.feature.{IDFModel, HashingTF, StopWordsRemover, RegexTokenizer}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

/**
  * Functions for performing pipeline transformations on DataFrames
  */
object Transforms {

  /**
    * convert bodies into tf-idf vectors by (1) tokenizing the text, (2) removing stopwords, (3) adding in word count
    * mappings (via a UDF) that we will use later on for analysis, (4) hashing the term frequency values, and
    * (5) rescaling the TF data based on IDF
    *
    * @param df dataframe of email body payloads
    * @return
    */
  def transformBodies(df: DataFrame, colName: String)(implicit config: Config): DataFrame = {
    val tokenizer = new RegexTokenizer().setInputCol(colName).setOutputCol("words").setPattern("\\w+").setGaps(false)
    val wordsData = tokenizer.transform(df)
    val remover = new StopWordsRemover().setInputCol("words").setOutputCol("filteredWords")
    val filteredWords = remover.transform(wordsData)
    val filteredWordsWithCounts = filteredWords.withColumn("wordCounts", wordCounts(filteredWords("filteredWords")))
    val hashingTF = new HashingTF().setInputCol("filteredWords").setOutputCol("rawFeatures").setNumFeatures(config.numTextFeatures)

    hashingTF.transform(filteredWordsWithCounts)
  }

  /**
    * this function accepts a dataframe with a column named 'features', which is presumably the output column
    * of one or more pipeline transformations on an original dataframe. It will then calculate inverse doc frequency,
    * and return the idf-rescaled dataframe
    *
    * @param df the dataframe to be rescaled
    * @param idfModel the IDF model that will perform the rescaling
    * @return dataframe identical to the input df, but with idf-rescaled features
    */
  def rescaleData(df: DataFrame)(implicit idfModel: IDFModel) :RDD[(String, Vector)] = {
    idfModel.transform(df)
      .select("$oid", "features")
      .map {
        row => (row.get(0).asInstanceOf[String], row.get(1).asInstanceOf[Vector])
      }
  }
}