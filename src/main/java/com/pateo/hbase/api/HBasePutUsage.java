package com.pateo.hbase.api;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.DependentColumnFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
 
public class HBasePutUsage {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// 测试 列值过滤器
		// testScanAndSimpleComFilter();
		
		String tableName = "test:user";
		int count = 1000;
		if (args.length >= 2) {
			tableName = args[0];
			count = Integer.valueOf(args[1]);
		}
		put(tableName, count);
	}

	private static void put(String tableName, int count) throws IOException, InterruptedException {
		Configuration configuration = getConfiguration();
		Logger logger = LoggerFactory.getLogger(HBasePutUsage.class);
		logger.error("-----start time");
		ArrayList<Put> arrayList = new ArrayList<Put>();

		//		Connection connection = getConnection();
		long start = System.currentTimeMillis();
		
		Connection connection = null;
		try {
			connection = ConnectionFactory.createConnection(configuration);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Table table = connection.getTable(TableName.valueOf(tableName));
		for (int i = 0; i < count ; i++) {
			
 			Put put = new Put(Bytes.toBytes("" +i));
			put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("age"),
					Bytes.toBytes("value1111" +i ));
			//arrayList.add(put);
			
			table.put(put);
			//arrayList.clear();
			if (i %200 == 0) {
				long end = System.currentTimeMillis();
			    logger.error( i  +" ======== " + (end - start) + "ms");
				//System.out.println(i  +" ======== " + (end - start) + "ms");
			}
		}
		table.close();
		connection.close();

		logger.error(  " ======== end time  " );

	}

	public static Configuration getConfiguration() {
		Configuration conf = HBaseConfiguration.create();
		// conf.set("hbase.rootdir", "hdfs://nameservice1/hbase");
		// conf.set("hbase.zookeeper.quorum", "10.172.10.168,10.172.10.169");
//		conf.set("hbase.zookeeper.quorum", "qing-zookeeper-srv1,qing-zookeeper-srv3,qing-zookeeper-srv2");
		conf.set(HConstants.ZOOKEEPER_QUORUM, "qing-hbzk-srv1,qing-hbzk-srv2,qing-hbzk-srv3");
		conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");

		return conf;
	}

	public static Connection getConnection() {
		Configuration configuration = getConfiguration();
		Connection connection = null;
		try {
			connection = ConnectionFactory.createConnection(configuration);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	public static void get(String tableName, String row) throws IOException {
		Table table = getConnection().getTable(TableName.valueOf(tableName));
		Get get = new Get(Bytes.toBytes(row));
		Result result = table.get(get);
		byte[] value = result.getValue("f1".getBytes(), "age".getBytes());
		System.err.println("SUCCESS");
		System.err.println("Get:" + result + "\t" + new String(value));
	}

	public static void scan(String tableName) throws IOException {
		Table table = getConnection().getTable(TableName.valueOf(tableName));

		Scan scan = new Scan();
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.err.println("Scan:" + result);
			Cell[] rawCells = result.rawCells();
			byte[] rowKey = result.getRow();
			System.err.println("key is   rowkey:"
					+ Bytes.toStringBinary(rowKey));
			for (int i = 0; i < rawCells.length; i++) {
				Cell cell = rawCells[i];

				// rowKey
				byte[] cloneRow = CellUtil.cloneRow(cell);
				// cell
				byte[] cloneValue = CellUtil.cloneValue(cell);
				byte[] cloneFamily = CellUtil.cloneFamily(cell);
				byte[] cloneQualifier = CellUtil.cloneQualifier(cell);
				System.out.println(Bytes.toString(cloneRow) + "----"
						+ Bytes.toString(cloneFamily) + "----"
						+ Bytes.toString(cloneQualifier) + "----"
						+ Bytes.toString(cloneValue));

			}

		}
		System.err.println("SUCCESS");
	}

	public static void delete(String tableName) throws IOException {
		Admin admin = getConnection().getAdmin();

		if (admin.tableExists(TableName.valueOf(tableName))) {
			try {
				admin.disableTable(TableName.valueOf(tableName));
				admin.deleteTable(TableName.valueOf(tableName));
				System.err.println("Delete table Success");
			} catch (IOException e) {
				System.err.println("Delete table Failed ");
			}

		} else {
			System.err.println("table not exists");
		}
	}

	public static void put(String tableName, String row, String columnFamily,
			String column, String data) throws IOException {
		Table table = getConnection().getTable(TableName.valueOf(tableName));
		Put put = new Put(Bytes.toBytes(row));
		put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column),
				Bytes.toBytes(data));
		table.put(put);
		System.err.println("SUCCESS");
	}

	private static void testScanAndSimpleComFilter() throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set(HConstants.ZOOKEEPER_QUORUM , "10.172.10.168,10.172.10.169");
		// conf.set(Constant.hbase_zookeeper_quorum,
		// "wluat-hbase-srv4,wluat-hbase-srv3,wluat-hbase-srv2");
		conf.set(HConstants.ZOOKEEPER_QUORUM, "qing-hbzk-srv1,qing-hbzk-srv2,qing-hbzk-srv3");
		conf.set(HConstants.ZOOKEEPER_CLIENT_PORT , "2181");
		// HTable table2 = new HTable(conf, "myTable");

		// Create a connection to the cluster.
		String tablename = "wuling:gps_locus_point_detail";
		Connection connection = ConnectionFactory.createConnection(conf);
		Table table = connection.getTable(TableName.valueOf(tablename));

		FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);

		// 列过滤器
		DependentColumnFilter columnFilter = new DependentColumnFilter(
				Bytes.toBytes("f1"), Bytes.toBytes("gpstime"));
		Filter rf = new RowFilter(CompareFilter.CompareOp.NO_OP,
				new BinaryComparator(Bytes.toBytes("gpstime"))); // OK
																	// 筛选出匹配的所有的行

		SingleColumnValueFilter filter2 = new SingleColumnValueFilter(
				Bytes.toBytes("f1"), Bytes.toBytes("province"),
				CompareFilter.CompareOp.EQUAL, Bytes.toBytes("山东省"));
		list.addFilter(columnFilter);
		list.addFilter(filter2);

		PrefixFilter prefixFilter = new PrefixFilter(
				Bytes.toBytes("P01100010015902"));
		// new ByteArrayComparable
		// RowFilter rowFilter = new RowFilter(
		// CompareOp.EQUAL,Bytes.toBytes("P01100010015902"));
		Scan scan = new Scan();
		scan.setFilter(prefixFilter);
		// scan.addColumn( Bytes.toBytes("f1"), Bytes.toBytes("obd_id"));

		ResultScanner ss = table.getScanner(scan);
		for (Result r : ss) {
			Cell[] rawCells = r.rawCells();

			for (int i = 0; i < rawCells.length; i++) {
				Cell cell = rawCells[i];

				// rowKey
				byte[] cloneRow = CellUtil.cloneRow(cell);
				// cell
				byte[] cloneValue = CellUtil.cloneValue(cell);
				byte[] cloneFamily = CellUtil.cloneFamily(cell);
				byte[] cloneQualifier = CellUtil.cloneQualifier(cell);
				System.out.println(Bytes.toString(cloneRow) + "----"
						+ Bytes.toString(cloneFamily) + "----"
						+ Bytes.toString(cloneQualifier) + "----"
						+ Bytes.toString(cloneValue));

				// int rowOffset = cell.getRowOffset();
				// short rowLength = cell.getRowLength();
				// String key =
				// Bytes.toStringBinary(cell.getRowArray(),rowOffset,rowLength);
			}
		}
	}
}
