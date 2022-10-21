package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.Biller;

@Component
public class BillerRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BillerRepositoryImpl.class);
	private static final String JOB_ID = "LDCPD5113B";

	private static final String STATUS_ACTIVE ="ACTIVE";
	private static final String STATUS_TERMINATED ="TERMINATED";
	private static final String STATUS_SUSPENDED ="SUSPENDED";

	public void updateBiller()  {
		
		billerStatusUpdate();
		topupBillerStatusUpdate();
	}

	public List<Biller> getBillerList() {
		JdbcTemplate template = getJdbcTemplate();
		List<Biller> billerList;

		String sql =  " SELECT a.ID , a.STATUS, a.SUSPENDED_START_DATE, a.SUSPENDED_END_DATE, a.EFFECTIVE_END_DATE FROM dcp.dbo.TBL_BILLER a";
		logger.info(String.format("get biller  sql=%s", sql));
		billerList = template.query(sql,
				new BeanPropertyRowMapper(Biller.class));
		return billerList;
	}


	public List<Biller> getTopUpList() {

		JdbcTemplate template = getJdbcTemplate();
		List<Biller> billerList;

		String sql = "SELECT a.ID , a.STATUS, a.SUSPENDED_START_DATE, a.SUSPENDED_END_DATE, a.EFFECTIVE_END_DATE FROM dcp.dbo.TBL_TOPUP_BILLER a ";
		logger.info(String.format("get topup biller sql=%s", sql));
		billerList = template.query(sql,
				new BeanPropertyRowMapper(Biller.class));
		return billerList;
	}
	
	public void billerStatusUpdate(){
		//biller
		List<Biller> billerList = getBillerList();
		List<Biller>  activeBillerList = new ArrayList<>();
		List<Biller>  suspendedBillerList = new ArrayList<>();
		List<Biller>  terminatedBillerList = new ArrayList<>();

		//SUSPENDED
		Date currentDate=new Date(Calendar.getInstance().getTime().getTime());

		billerList.forEach(biller->{
			if(biller.getEffectiveEndDate()!=null && biller.getEffectiveEndDate().compareTo(currentDate)<=0){
				terminatedBillerList.add(biller);
			}else if(biller.getSuspendedEndDate() !=null &&biller.getSuspendedEndDate().compareTo(currentDate)<=0){
				if(!biller.getStatus().equalsIgnoreCase(STATUS_ACTIVE)){
				activeBillerList.add(biller);
				}
			}else if(biller.getSuspendedStartDate() !=null && biller.getSuspendedStartDate().compareTo(currentDate)<=0){
				suspendedBillerList.add(biller);
			}
		});

		updateBiller(activeBillerList, STATUS_ACTIVE);
		updateBiller(suspendedBillerList, STATUS_SUSPENDED);
		updateBiller(terminatedBillerList, STATUS_TERMINATED);
	}

	public void topupBillerStatusUpdate(){
		//topup
		List<Biller> topUpBillerList = getTopUpList();
		List<Biller>  activetopUpBillerList = new ArrayList<>();
		List<Biller>  suspendedtopUpBillerList = new ArrayList<>();
		List<Biller>  terminatedtopUpBillerList = new ArrayList<>();
		
		Date currentDate=new Date(Calendar.getInstance().getTime().getTime());

		topUpBillerList.forEach(biller->{
			if(biller.getEffectiveEndDate()!=null && biller.getEffectiveEndDate().compareTo(currentDate)<=0){
				terminatedtopUpBillerList.add(biller);
			}else if(biller.getSuspendedEndDate() !=null && biller.getSuspendedEndDate().compareTo(currentDate)<=0){
				if(!biller.getStatus().equalsIgnoreCase(STATUS_ACTIVE)) {
					activetopUpBillerList.add(biller);
				}
			}else if(biller.getSuspendedStartDate() !=null && biller.getSuspendedStartDate().compareTo(currentDate)<=0){
				suspendedtopUpBillerList.add(biller);
			}
		});

		updateTopUpBiller(activetopUpBillerList, STATUS_ACTIVE);
		updateTopUpBiller(suspendedtopUpBillerList, STATUS_SUSPENDED);
		updateTopUpBiller(terminatedtopUpBillerList, STATUS_TERMINATED);
	}

	public int updateBiller(List<Biller> records,String status) {
		long time = System.currentTimeMillis();
		String sql = "UPDATE dcp.dbo.TBL_BILLER SET STATUS=? ,UPDATED_TIME=?, UPDATED_BY=? WHERE ID=?;";

		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Biller biller = records.get(i);
				ps.setString(1,status);
				ps.setDate(2, new java.sql.Date(time));
				ps.setString(3, JOB_ID);
				ps.setInt(4, biller.getId());
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		return row.length;
	}
	
	public int updateTopUpBiller(List<Biller> records, String status) {

		long time = System.currentTimeMillis();
		String sql = "UPDATE dcp.dbo.TBL_TOPUP_BILLER SET STATUS=?,UPDATED_TIME=?, UPDATED_BY=? WHERE ID=?;";
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Biller biller = records.get(i);
				ps.setString(1,status);
				ps.setDate(2, new java.sql.Date(time));
				ps.setString(3, JOB_ID);
				ps.setInt(4, biller.getId());
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
		return row.length;
	}

}
