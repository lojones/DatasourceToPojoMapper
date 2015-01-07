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

	/**
	 * Get instance of class T that has fields populated from matching fields current row of ResultSet rs.  
	 * * Matching in this case means camelcase converted to underscore uppercase, see these
	 * mappings as examples:
	 *    javaField = JAVA_FIELD, id = ID, longJavaFieldName = LONG_JAVA_FIELD_NAME
	 * 
	 * @param rs ResultSet set to the interested row
	 * @param clazz Class to get fields of
	 * @return an instance of class clazz that has fields populated
	 * @throws Exception
	 */
	public <T> T getMappedObj(ResultSet rs, Class<T> clazz) throws Exception
	{
		T t = clazz.newInstance();
		set(rs, t);
		return t;
	}
	
	/**
	 * Get fields of the class and all superclasses except for Object
	 * @param clazz Class to get fields off
	 * @param fields List of Field Objects representing all relevent fields
	 * @return
	 */
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
	
	/**
	 * Get the java.sql Type of the given column
	 * @param rs - ResultSet which contains the column
	 * @param columnName - name of the column in the resultset (ie its label)
	 * @return the java.sql.Type value of the database column type
	 * @throws SQLException
	 */
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
	
	/**
	 * For all fields in class T that has standard setters, search the ResultSet's current row for a matching* column name
	 * and set that value in obj.   * Matching in this case means camelcase converted to underscore uppercase, see these
	 * mappings as examples:
	 *    
	 * <table>
	 * <tr><th>java field</th><th></th><th>database column</th></tr>
	 * <tr><td>javaField</td><td>-></td><td>JAVA_FIELD</td></tr>
	 * <tr><td>id</td><td>-></td><td>ID</td></tr>
	 * <tr><td>longJavaFieldName</td><td>-></td><td>LONG_JAVA_FIELD_NAME</td></tr>
	 * </table>
	 * 
	 * @param rs ResultSet set to a current row
	 * @param obj Instance of T to set values on
	 * @throws Exception
	 */
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
