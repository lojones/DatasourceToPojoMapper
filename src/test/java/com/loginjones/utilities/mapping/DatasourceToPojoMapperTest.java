package com.loginjones.utilities.mapping;

import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.TestCase;

@RunWith(MockitoJUnitRunner.class)
public class DatasourceToPojoMapperTest extends TestCase
{

	@Mock
	ResultSet rs;
	
	@Mock
	ResultSetMetaData metaData;
	
	Calendar calendar=Calendar.getInstance();
	
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		when(metaData.getColumnCount()).thenReturn(4);
		when(metaData.getColumnLabel(1)).thenReturn("STRING_FIELD");
		when(metaData.getColumnLabel(2)).thenReturn("DATE_FIELD_AS_STRING");
		when(metaData.getColumnLabel(3)).thenReturn("INT_FIELD");
		when(metaData.getColumnLabel(4)).thenReturn("DATE_FIELD");
		
		when(rs.getMetaData()).thenReturn(metaData);
		
		Timestamp ts= new Timestamp(calendar.getTimeInMillis());
		when(rs.getString("STRING_FIELD")).thenReturn("StringValue");
		when(rs.getString("DATE_FIELD_AS_STRING")).thenReturn("DateFieldAsString");
		when(rs.getInt("INT_FIELD")).thenReturn(1);
		when(rs.getTimestamp("DATE_FIELD")).thenReturn(ts);
		
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	@Test
	public void testSetDateFormat()
	{
		DatasourceToPojoMapper mapper=new DatasourceToPojoMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
	}

	@Test
	public void testGetMappedObj() throws Exception
	{
		DatasourceToPojoMapper mapper=new DatasourceToPojoMapper();
		SomeModel model=mapper.getMappedObj(rs, SomeModel.class);
		assertNotNull(model);
	}

	@Test
	public void testSet() throws Exception
	{
		
		
		SomeModel obj=new SomeModel();
		
		DatasourceToPojoMapper mapper=new DatasourceToPojoMapper();
		mapper.set(rs, obj);
		
		assertEquals(Integer.valueOf(1), obj.getIntField());
	}

}
