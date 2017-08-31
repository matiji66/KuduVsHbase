package org.kududb.examples.loadgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.Upsert;

/**
 * java -cp com-pateo-kudu-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.kududb.examples.loadgen.Sample java_sample-1000000 1000000
 * 100w insert 1000s 1ms/p
 * @author sh04595
 *
 *java -cp com-pateo-kudu-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.kududb.examples.loadgen.KuduInsertTest java_sample-10000 10000 false
 */
public class KuduInsertTest {

  private static final String KUDU_MASTER = System.getProperty("kuduMaster", "qing-hadoop-master-srv2:7051");
//  private static final String KUDU_MASTER = "qing-hadoop-master-srv2:7051" ; //7051

  public static void main(String[] args) {
	  
    System.out.println("-----------------------------------------------");
    System.out.println("Will try to connect to Kudu master at " + KUDU_MASTER);
    System.out.println("Run with -DkuduMaster=myHost:port to override.");
    System.out.println("-----------------------------------------------");
    String tableName = "java_sample_1000000" ;
    int count = 100000;
    if (args.length >= 2) {
		tableName = args[0];
		count = Integer.valueOf(args[1]);
	}
    System.out.println("===== tableName ====" +tableName);

    KuduClient client = new KuduClient.KuduClientBuilder(KUDU_MASTER).build();

    try {
      
      if (! client.tableExists(tableName)) {
    	  List<ColumnSchema> columns = new ArrayList<ColumnSchema>(2);
          columns.add(new ColumnSchema.ColumnSchemaBuilder("key", Type.INT32)
              .key(true)
              .build());
          columns.add(new ColumnSchema.ColumnSchemaBuilder("value", Type.STRING)
              .build());
          List<String> rangeKeys = new ArrayList<>();
          rangeKeys.add("key");
          Schema schema = new Schema(columns);
    	  client.createTable(tableName, schema, new CreateTableOptions().setRangePartitionColumns(rangeKeys));
      }

      KuduTable table = client.openTable(tableName);
      long start = System.currentTimeMillis();
      KuduSession session = client.newSession();
      
      int step = count / 100 ;
      //Insert insert = 
      //Upsert newUpsert = table.newUpsert();
      
      for (int i = 1; i < count; i++) {
    	Insert insert = table.newInsert();
        PartialRow row = insert.getRow();
        row.addInt(0, i);
        row.addString(1, "value " + i);
        session.apply(insert);
        long end = System.currentTimeMillis();
        if (i %step == 0) {
        	System.out.println( i+ "===========insert cost===========" + (end- start));
		}
      }
      long end = System.currentTimeMillis();

      System.out.println( count+ "===========insert cost===========" + (end- start));
      System.out.println( count+ "===========KuduInsertTest rate===========" + ((end- start)/(count+0.001)));

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
       try {
    	  client.shutdown();
      } catch (Exception e) {
    	  e.printStackTrace();
      }
    }
  }
}

