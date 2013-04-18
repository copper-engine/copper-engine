/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.audit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.StringUtils;

import de.scoopgmbh.copper.management.AuditTrailQueryMXBean;
import de.scoopgmbh.copper.management.model.AuditTrailInfo;

public class AuditTrailQueryEngine extends JdbcDaoSupport implements AuditTrailQueryMXBean {
	private static final Logger logger = LoggerFactory.getLogger(AuditTrailQueryEngine.class);
	
	@Override
	public List<AuditTrailInfo> getAuditTrails(String transactionId,String conversationId,String correlationId,Integer level, int maxResult) {
		
		
		SqlPagingQueryProviderFactoryBean factory  = new SqlPagingQueryProviderFactoryBean();
		
		
		String sortClause = "SEQ_ID";
		String whereClause = "where 1=1 ";
		List<Object> args = new ArrayList<Object>();
		
		if(level != null){
			whereClause += " and LOGLEVEL <= ? ";
			sortClause = "LOGLEVEL";
			args.add(level);
		}
		if(StringUtils.hasText(correlationId)){
			whereClause += " and CORRELATION_ID = ? ";
			sortClause = "CORRELATION_ID";
			args.add(correlationId);
		}
		
		if(StringUtils.hasText(conversationId)){
			whereClause += " and CONVERSATION_ID = ? ";
			sortClause = "CONVERSATION_ID";
			args.add(conversationId);
		}
		
		if(StringUtils.hasText(transactionId)){
			whereClause += " and TRANSACTION_ID = ? ";
			sortClause = "TRANSACTION_ID";
			args.add(transactionId);
		}
		
		String selectClause = "select "
				+"SEQ_ID,"
				+"TRANSACTION_ID,"
				+"CONVERSATION_ID,"
				+"CORRELATION_ID,"
				+"OCCURRENCE,"
				+"LOGLEVEL,"
				+"CONTEXT,"
				+"INSTANCE_ID,"
				+"MESSAGE_TYPE";
		
		factory.setDataSource(getDataSource());
		factory.setFromClause("from COP_AUDIT_TRAIL_EVENT ");
		
		factory.setSelectClause(selectClause);
		
		factory.setWhereClause(whereClause);
		factory.setSortKey(sortClause);
		
		PagingQueryProvider queryProvider = null;
		try {
			queryProvider = (PagingQueryProvider)factory.getObject();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
		
		String query = queryProvider.generateFirstPageQuery(maxResult);
		
		
		//this.getJdbcTemplate().setQueryTimeout(1000);	
		
		
		long start = System.currentTimeMillis();
		RowMapper<AuditTrailInfo> rowMapper = new RowMapper<AuditTrailInfo>(){

			public AuditTrailInfo mapRow(ResultSet rs, int arg1)
					throws SQLException {
				
				return new AuditTrailInfo(
									  rs.getLong("SEQ_ID"),
									  rs.getString("TRANSACTION_ID"), 
									  rs.getString("CONVERSATION_ID"), 
									  rs.getString("CORRELATION_ID"), 
									  rs.getTimestamp("OCCURRENCE").getTime(), 
									  rs.getInt("LOGLEVEL"), 
									  rs.getString("CONTEXT"), 
									  rs.getString("INSTANCE_ID"), 
									  rs.getString("MESSAGE_TYPE")
									  );
			}
			
		};
		List<AuditTrailInfo> res = this.getJdbcTemplate().query(query, rowMapper,args.toArray());
		
		long end = System.currentTimeMillis();
		
		logger.info("query took: "+(end-start)+" ms : "+query);
		
		return res;
	}
	
	public byte[] getMessage(long id){
		String customSelect = "select LONG_MESSAGE from COP_AUDIT_TRAIL_EVENT where SEQ_ID = ? ";
		
		ResultSetExtractor<byte[]> rse = new ResultSetExtractor<byte[]>(){

			@Override
			public byte[] extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				rs.next();
				return convertToArray(rs.getBinaryStream("LONG_MESSAGE"));
			}
			
		};
		
		return this.getJdbcTemplate().query(customSelect, rse, new Object[]{id});
	}

	private byte[] convertToArray(InputStream messageStream) {
		if(messageStream == null){
			return new byte[0];
		}
		
		
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read = 0;
		int off = 0;
		try {
			while((read=messageStream.read(bytes))>0){
				out.write(bytes,off,read);
				off+= read;
			}
			messageStream.close();
			return out.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}

	
}
