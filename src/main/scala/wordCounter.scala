import org.apache.spark.sql.SparkSession

object wordCounter {
  def main(args: Array[String]) {
//    инициализируем сессию
    val spark = SparkSession.builder()
      .appName("Word Counter App")
      .master("local").getOrCreate()
//    загружаем стопслова
    val stopWords = scala.io.Source.fromResource("stopwords-ru.txt").mkString
//    загружаем книгу
    val bookRDD = spark.sparkContext.textFile("src/main/resources/Путь Абая.txt")
//    преобразуем коллекцию строк в коллекцию слов
    val wordCount = bookRDD.flatMap(line => line.split(" "))
//      приводим сова в книжнему регистру, офильтровываем все символы за исключением русских слов
      .map(word => word.toLowerCase
            .replaceAll("[^а-я]",  "")
            .trim)
//      отфильтровываем все пропуски и стопслова
      .filter(word => !word.isEmpty && !stopWords.contains(word))
//      преобразовываем коллекцию слов в коллекцию кортежей (<слово>, 1)
      .map(word => (word, 1))
//      группируем кортежи по первому элементу, вторые элементы складываем
      .reduceByKey((a, b) => a + b)
//      сортируем слова в порядке убывания их вхождений в текст
      .map(pair => pair.swap)
      .sortByKey(false)
      .map(pair => pair.swap)
//        преобразовываем RDD в DataFrame и сохраняем в CSV-фаил
        val df = spark.createDataFrame(wordCount)
        df.write.format("csv").save("src/main/resources/wordCountResult")
  }
}
