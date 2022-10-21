package com.rhbgroup.dcp.bo.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;

public class BoConfigGenericRowMapper implements RowMapper<BoConfigGeneric> {
	
	@Override
	public BoConfigGeneric mapRow(ResultSet rs, int rowNum) throws SQLException {
		BoConfigGeneric boConfigGeneric = new BoConfigGeneric();
		boConfigGeneric.setConfigCode(rs.getString("CONFIG_CODE"));
		boConfigGeneric.setConfigDesc(rs.getString("CONFIG_DESC"));

		return boConfigGeneric;
	}

}