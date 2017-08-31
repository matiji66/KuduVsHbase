package org.kududb.examples.loadgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.RowResult;
import org.apache.kudu.client.RowResultIterator;

/**
 * java -cp com-pateo-kudu-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.kududb.examples.loadgen.ScanTest java_sample-100000
 * 1000000===========scan cost===========961
 * 
 * key 999800 value value 999800
1000000===========KuduScanTest cost===========49633
1000000===========KuduScanTest rate===========0.049632999950367
 * @author sh04595
 *
 */
public class KuduScanTest {

	  private static final String KUDU_MASTER = System.getProperty("kuduMaster", "qing-hadoop-master-srv2:7051");
	//  private static final String KUDU_MASTER = "qing-hadoop-master-srv2:7051" ; //7051

	  public static void main(String[] args) {
//		  JavaSparkContext jsc = new JavaSparkContext();
//		  List<String> asList = Arrays.asList("1","2");
//		  jsc.parallelize(asList);
		     
	    System.out.println("-----------------------------------------------");
	    System.out.println("Will try to connect to Kudu master at " + KUDU_MASTER);
	    System.out.println("Run with -DkuduMaster=myHost:port to override.");
	    System.out.println("-----------------------------------------------");
	    String tableName = "java_sample-1000000" ;
	    System.out.println("===== tableName ====" +tableName);
	    int count = 0;
	    boolean delete = false;
	    if (args.length >= 3) {
			tableName = args[0];
			count = Integer.valueOf(args[1]);
			delete= Boolean.valueOf(args[2]);
		}
	    KuduClient client = new KuduClient.KuduClientBuilder(KUDU_MASTER).build();

	    try {

	      KuduTable table = client.openTable(tableName);
//	      table.newInsert();
//	      table.newDelete();
//	      table.newUpdate();
//	      table.newUpsert();
	      
	      long start = System.currentTimeMillis();
	      
	      List<String> projectColumns = new ArrayList<>(1);
	      projectColumns.add("key");

	      projectColumns.add("value");
	      
	      KuduScanner scanner = client.newScannerBuilder(table)
	          .setProjectedColumnNames(projectColumns)
	          .build();
	      
	      int count1 = 0 ;
	      
	      while (scanner.hasMoreRows()) {
	        RowResultIterator results = scanner.nextRows();
	        while (results.hasNext()) {
	          RowResult result = results.next();
	          if (count1%100 ==0) {
	        	  System.out.println("key " +result.getInt(0) +" value " +result.getString(1));
	          }
	          count1 ++ ;
	        }
	      }
	      long end = System.currentTimeMillis();
	      System.out.println( count1+ "===========KuduScanTest cost===========" + (end- start));
	      System.out.println( count1+ "===========KuduScanTest rate===========" + ((end- start)/(count1+0.001)));

	    } catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      try {
	    	  if (delete) {
	    		  System.out.println("============delete" +delete);
	    		client.deleteTable(tableName);
			}
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
}
