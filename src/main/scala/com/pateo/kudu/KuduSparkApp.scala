package com.pateo.kudu


import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.kudu.spark.kudu.KuduDataFrameReader
import org.apache.kudu.spark.kudu.KuduDataFrameWriter

/**
 * @author ${user.name}
 */
object App2 {

  def foo(x: Array[String]) = x.foldLeft("")((a, b) => a + b)

  def main(args: Array[String]) {
    println("Hello World!")
    println("concat arguments = " + foo(args))

    import org.apache.kudu.spark.kudu._
    import org.apache.kudu.spark.kudu.KuduContext
    import org.apache.kudu.client._
    import collection.JavaConverters._

    val conf = new SparkConf().setAppName("kudu-example")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // Read a table from Kudu
    val df = sqlContext.read.options(Map("kudu.master" -> "qing-hadoop-master-srv2:7051", "kudu.table" -> "impala::test.tmp_20170830_gps_log_kudu")).kudu

    // Query using the Spark API...
    df.select("id").filter("id>= 5 ").show()

    // ...or register a temporary table and use SQL
    df.registerTempTable("kudu_table")
    val filteredDF = sqlContext.sql("select id from kudu_table where id >= 5")

    // Use KuduContext to create, delete, or write to Kudu tables "kudu.master:7051"
    val kuduContext = new KuduContext("qing-hadoop-master-srv2:7051", sqlContext.sparkContext)

    // Create a new Kudu table from a dataframe schema
    // NB: No rows from the dataframe are inserted into the table
    kuduContext.createTable(
      "test_table", df.schema, Seq("key"),
      new CreateTableOptions()
        .setNumReplicas(1)
        .addHashPartitions(List("key").asJava, 3))

    // Insert data
    kuduContext.insertRows(df, "test_table")

    // Delete data
    kuduContext.deleteRows(filteredDF, "test_table")

    // Upsert data
    kuduContext.upsertRows(df, "test_table")

    // Update data
    val alteredDF = df.select("id", "count + 1")
    //kuduContext.updateRows(filteredRows, "test_table" 

    // Data can also be inserted into the Kudu table using the data source, though the methods on KuduContext are preferred
    // NB: The default is to upsert rows; to perform standard inserts instead, set operation = insert in the options map
    // NB: Only mode Append is supported
    df.write.options(Map("kudu.master" -> "kudu.master:7051", "kudu.table" -> "test_table")).mode("append").kudu

    // Check for the existence of a Kudu table
    kuduContext.tableExists("another_table")

    // Delete a Kudu table
    kuduContext.deleteTable("unwanted_table")
  }

}
