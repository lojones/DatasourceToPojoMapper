package com.loginjones.utilities.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

public class DatasourceToPojoMapper
{

	private final Logger logger = LoggerFactory.getLogger(DatasourceToPojoMapper.class);
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
	
	
	public void setDateFormat(SimpleDateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public <T> T getMappedObj(ResultSet rs, Class<T> clazz) throws Exception
	{
		T t = clazz.newInstance();
		set(rs, t);
		return t;
	}
	
	private <T> List<Field> getAllReleventFields(Class<T> clazz,List<Field> fields)
	{
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (clazz.getSuperclass().equals(Object.class))
		{
			return fields;
		}
		else
		{
			return getAllReleventFields(clazz.getSuperclass(), fields);
		}
		
	}
	
	private int getType(ResultSet rs, String columnName) throws SQLException
	{
		int count=rs.getMetaData().getColumnCount();
		
		for (int i=1;i<=count;i++)
		{
			if (rs.getMetaData().getColumnLabel(i).equals(columnName))
			{
				return rs.getMetaData().getColumnType(i);
			}
		}
		return -1;
	}
	
	public <T> void set(ResultSet rs, T obj) throws Exception
	{
		List<Field> fields = getAllReleventFields(obj.getClass(),new ArrayList<Field>());   
		
		for (Field field : fields)
		{
			String dbColumnName=CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getName());

			Object value=null;
			
			logger.trace("looking for {} column {} for field {}",field.getGenericType(),dbColumnName,field.getName() );
			
			if (field.getGenericType().equals(String.class))
			{
				if (java.sql.Types.TIMESTAMP == getType(rs, dbColumnName))
				{
					value=dateFormat.format(rs.getTimestamp(dbColumnName));
				}
				else
				{
					value=rs.getString(dbColumnName);	
				}
				
			}
			else if (field.getGenericType().equals(Integer.class))
			{
				value=rs.getInt(dbColumnName);
			}
			else if (field.getGenericType().equals(Date.class))
			{
				value=rs.getTimestamp(dbColumnName);
			}
			else
			{
				throw new Exception("Unexpected field type - "+field.getGenericType().toString());
			}

			String setter = "set"+CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName());
			
			logger.trace("Getting setter method '{}' with input arg type {}",setter,value.getClass());
			
			Class<?> inputArgType;
			
			if (value.getClass().equals(Timestamp.class))
			{
				inputArgType=Date.class;
			}
			else 
			{
				inputArgType=value.getClass();				
			}
			
			Method setterMethod = obj.getClass().getDeclaredMethod(setter, inputArgType);
			
			logger.trace("Executing method {} on {} with value {}",setterMethod,obj,value);
			
			setterMethod.invoke(obj, value);
			
		}
	}
	
}
