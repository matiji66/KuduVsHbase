package org.kududb.examples.loadgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Type;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduPredicate;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.RowResult;
import org.apache.kudu.client.RowResultIterator;

/**
 * java -cp com-pateo-kudu-0.0.1-SNAPSHOT-jar-with-dependencies.jar
 * org.kududb.examples.loadgen.ScanTest java_sample-100000
 * 1000000===========scan cost===========961
 * 
 * @author sh04595
 *
 */
public class KuduScanPredicateTest {

	private static final String KUDU_MASTER = System.getProperty("kuduMaster",
			"qing-hadoop-master-srv2"); //7051

	// private static final String KUDU_MASTER = "qing-hadoop-master-srv2:7051"
	// ; //7051

	public static void main(String[] args) {
		//JavaSparkContext jsc = new JavaSparkContext();
		//List<String> asList = Arrays.asList("1", "2");
		// jsc.parallelize(asList);

		System.out.println("-----------------------------------------------");
		System.out.println("Will try to connect to Kudu master at "
				+ KUDU_MASTER);
		System.out.println("Run with -DkuduMaster=myHost:port to override.");
		System.out.println("-----------------------------------------------");
		String tableName = "java_sample-1000000";
		System.out.println("===== tableName ====" + tableName);
		int count = 10;
		boolean delete = false;
		if (args.length >= 3) {
			tableName = args[0];
			count = Integer.valueOf(args[1]);
			delete = Boolean.valueOf(args[2]);
		}
		KuduClient client = new KuduClient.KuduClientBuilder(KUDU_MASTER)
				.build();

		try {

			KuduTable table = client.openTable(tableName);
			PartialRow partialRow = table.getSchema().newPartialRow();
			
			// table.newInsert();
			// table.newDelete();
			// table.newUpdate();
			// table.newUpsert();

			long start = System.currentTimeMillis();

			List<String> projectColumns = new ArrayList<>(1);
			projectColumns.add("key");

			projectColumns.add("value");
//			val pkPeriod: KuduPredicate = KuduPredicate.newComparisonPredicate(OccurrenceSchema.Period.name), KuduPredicate.ComparisonOp.EQUAL, 1476)
//			builder.addPredicate(pkPeriod)
//			val pkSetId: KuduPredicate = KuduPredicate.newComparisonPredicate(OccurrenceSchema.SetId.name), KuduPredicate.ComparisonOp.EQUAL, 82)
//			builder.addPredicate(pkSetId)
//			val pkEvent: KuduPredicate = KuduPredicate.newComparisonPredicate(OccurrenceSchema.Event.name), KuduPredicate.ComparisonOp.EQUAL, 3195167)
//			builder.addPredicate(pkEvent)
//			val pkDate: KuduPredicate = KuduPredicate.newComparisonPredicate(OccurrenceSchema.Date.name), KuduPredicate.ComparisonOp.EQUAL, 1367922840000L)
//			builder.addPredicate(pkDate)

			//KuduPredicate.
			for (int i = 0; i < count; i++) {
				KuduPredicate newComparisonPredicate = KuduPredicate
						.newComparisonPredicate( new ColumnSchema.ColumnSchemaBuilder("key",Type.INT32).build(),
						KuduPredicate.ComparisonOp.EQUAL, 
						i);

				KuduScanner scanner = client.newScannerBuilder(table)
						.setProjectedColumnNames(projectColumns)
						.addPredicate(newComparisonPredicate)
						.build();

				while (scanner.hasMoreRows()) {
					RowResultIterator results = scanner.nextRows();
					while (results.hasNext()) {
						RowResult result = results.next();
						if (i % 200 == 0) {
							System.out.println("key " + result.getInt(0)
									+ " value ==" + result.getString(1));
						}
					}
				}

			}

			long end = System.currentTimeMillis();
			System.out
					.println(count + "===========KuduScanPredicateTest cost==========="
							+ (end - start));
			System.out.println(count
					+ "===========KuduScanPredicateTest rate==========="
					+ ((end - start) / (count + 0.001)));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (delete) {
					
					client.deleteTable(tableName);
					System.out.println("===========deleteTable ===========" +delete);
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
