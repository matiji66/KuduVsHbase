package com.pateo.hbase.api;

import java.io.IOException;

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
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

 
public class HbaseGetUsage {

	public static void main(String[] args) throws IOException {
		// 测试 列值过滤器
		// testScanAndSimpleComFilter();
		String tableName = "test:user";
		int count = 1000000;
		if (args.length >= 2) {
			tableName = args[0];
			count = Integer.valueOf(args[1]);
		}
		getTest(tableName,count);
	}
	public static void getTest(String tableName, int count) {
		
		long start = System.currentTimeMillis();
 		for (int i = 0; i < count; i++) {
			
 			try {
				get(tableName, count+"");
			} catch (IOException e) {
				e.printStackTrace();
			}
 			if (i %200 ==0) {
 				long end = System.currentTimeMillis();
 				System.out.println(i+" ======== " + (end - start) + "ms");
			}
		}

		long end = System.currentTimeMillis();
		System.out.println(count +" ======== " + (end - start) + "ms");

	}

	public static Configuration getConfiguration() {
		Configuration conf = HBaseConfiguration.create();
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

		FilterList filterList = new FilterList();
		Scan scan = new Scan();
		Filter prefilter = new PrefixFilter(Bytes.toBytes("P011002100007551_149160"));
		filterList.addFilter(prefilter);
		
//		DependentColumnFilter columnFilter = new DependentColumnFilter(
//				Bytes.toBytes("f1"), Bytes.toBytes("gpstime"));
//		filterList.addFilter(columnFilter);
		
//		Filter rf = new RowFilter(CompareFilter.CompareOp.NO_OP, new BinaryComparator(Bytes.toBytes("gpstime"))); // OK 筛选出匹配的所有的行  
//		filterList.addFilter(rf);
		//new QualifierFilter(CompareOp.NO_OP, new BinaryComparator(Bytes.toBytes("gpstime")));
		
		scan.setFilter(prefilter);
		scan.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("gpstime")) ;
		
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
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

}
