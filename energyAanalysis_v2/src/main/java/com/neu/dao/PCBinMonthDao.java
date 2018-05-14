package com.neu.dao;

import java.sql.SQLException;
import java.util.List;

import com.neu.common.AnalysisException;
import com.neu.utils.SqlHelper;

public class PCBinMonthDao {

private SqlHelper sqlHelper = null;
	
	public PCBinMonthDao(){
		sqlHelper = new SqlHelper();
	}
	
	/**
	 * 保存月功率曲线数据
	 * @param pcBinList
	 * @throws AnalysisException
	 */
	public void savePCBinMonth(List<List<String>> pcBinList) throws AnalysisException{
		String sql = "INSERT INTO pcbin_month(province,farm_name,factory,fan_type,fan_no,data_date,wind_speed,ac_power,data_type)"
				+ " values(?,?,?,?,?,?,?,?,?)";
		try {
			sqlHelper.executeBatch(sql, pcBinList);
		} catch (SQLException e) {
			throw new AnalysisException("保存月功率曲线失败。", e);
		}
	}
	
}
