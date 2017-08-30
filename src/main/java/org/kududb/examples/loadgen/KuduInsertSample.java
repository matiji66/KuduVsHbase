package org.kududb.examples.loadgen;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.ColumnSchema.ColumnSchemaBuilder;
import org.apache.kudu.client.*;

import java.util.ArrayList;
import java.util.List;

/**
 * java -cp com-pateo-kudu-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.kududb.examples.loadgen.Sample java_sample-1000000 1000000
 * 100w insert 1000s 1ms/p
 * @author sh04595
 *
 */
public class KuduInsertSample {

  private static final String KUDU_MASTER = System.getProperty("kuduMaster", "qing-hadoop-master-srv2:7051");
//  private static final String KUDU_MASTER = "qing-hadoop-master-srv2:7051" ; //7051

  public static void main(String[] args) {
	  
    System.out.println("-----------------------------------------------");
    System.out.println("Will try to connect to Kudu master at " + KUDU_MASTER);
    System.out.println("Run with -DkuduMaster=myHost:port to override.");
    System.out.println("-----------------------------------------------");
    String tableName = "java_sample-1000000" ;
    System.out.println("===== tableName ====" +tableName);
    int count = 1000000;
    if (args.length >= 2) {
		tableName = args[0];
		count = Integer.valueOf(args[1]);
	}
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
      for (int i = 0; i < count; i++) {
        Insert insert = table.newInsert();
        PartialRow row = insert.getRow();
        row.addInt(0, i);
        row.addString(1, "value " + i);
        session.apply(insert);
        if (i %step == 0) {
        	long end = System.currentTimeMillis();
        	System.out.println( count+ "===========insert cost===========" + (end- start));
		}
      }
      long end = System.currentTimeMillis();

      System.out.println( count+ "===========insert cost===========" + (end- start));
 
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

