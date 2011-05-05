package org.onesun.feedminer.dbhelper;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.onesun.feedminer.imdb.DBManager;
import org.onesun.feedminer.pojo.ExtractedContent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;


public class ContentHelper {
	private static final Logger logger = Logger.getLogger(ContentHelper.class.getName());

	// public static final String DESCRIPTION_COLUMN = "DESCRIPTION";
	private static final int MAX_CONNECTIONS = 2;
	
	private DBManager dbMgr = new DBManager(MAX_CONNECTIONS);
	private final String tableName = "content_table";
	private Set<String> columns = null;
	
	private static ContentHelper instance = null;
	
	public static ContentHelper getInstance() {
		if(instance == null){
			instance = new ContentHelper();
		}
		
		return instance;
	}

	private ContentHelper(){
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public Set<String> getColumns(){
		return columns;
	}
	
	public DBManager getDBManager(){
		return dbMgr;
	}
	
	public synchronized void insert(ExtractedContent contentObject){
		HashMap<String, String> content = contentObject.getContent();
		final int NB_COLUMNS = columns.size() - 1;
		
		String sql = "INSERT INTO " + tableName + " (";
		String values = " VALUES(";
		
		int colCount = 0;
//		int binColIdx = -1;
//		byte binBytes[] = {'N', 'O', 'T', ' ', 'G', 'O', 'O', 'D'};
		
		for(String column : columns) {
			sql += column;
			
//			if(column.compareToIgnoreCase(DESCRIPTION_COLUMN) == 0) {
//				values += "?";
//				binColIdx = colCount;
//				binBytes = content.get(column).getBytes();
//			}
//			else 
			{
				values += "'" + StringEscapeUtils.escapeSql(content.get(column)) + "'";
			}
			
			if(colCount == NB_COLUMNS){
				sql += ")";
				values += ")";
			}
			else {
				sql += ", ";
				values += ", ";
			}
			colCount++;
		}
		
		sql += values;
		logger.debug("INSERT SQL: " + sql);
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dbMgr.getConnection();
			ps = conn.prepareStatement(sql);
			
//			if(binBytes != null){
//				InputStream bis = new ByteArrayInputStream(binBytes);
//				ps.setBinaryStream(binColIdx, bis, binBytes.length);
//			}
			
			ps.executeUpdate();
		}
		catch(Exception e){
			logger.debug("Exception while inesrting row into " + tableName);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				dbMgr.freeConnection(conn);
			}
			catch(Exception e){
				logger.debug("Exception while closing connection - during table creation : " + tableName);
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void createTable(Set<String> columns) {
		if(tableName == null || columns == null){
			logger.debug("Cannot create table: the table and column names are not any good to proceed!");
			System.exit(1);
		}
		
		this.columns = columns;
		
		Connection conn = dbMgr.getConnection();
		if(conn != null){
			Statement s = null;
			String sql = null;
			
			sql = "CREATE TABLE " + tableName + " (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1), ";
			
			final int NB_COLUMNS = columns.size() - 1;
			
			int colCount = 0;
			for(String column : columns) {
				String colType = "VARCHAR(" + Integer.MAX_VALUE + ")";
				
//				if(column.compareToIgnoreCase(DESCRIPTION_COLUMN) == 0) {
//					colType = "LONGVARBINARY";
//				}
				
				sql += column + " " + colType;
				
				if(colCount == NB_COLUMNS){
					sql += ")"; 
				}
				else {
					sql += ", ";
				}
				colCount++;
			}
			
			logger.debug("Create Table SQL: " + sql);
			try {
				s = conn.createStatement();
				s.execute(sql);
			}
			catch (Exception e) {
				logger.debug("Exception while Creating table " + tableName);
				e.printStackTrace();
			}
			finally {
				try {
					s.close();
					dbMgr.freeConnection(conn);
				}
				catch(Exception e){
					logger.debug("Exception while closing connection - during table creation : " + tableName);
					e.printStackTrace();
				}
			}
		}
	}
}
