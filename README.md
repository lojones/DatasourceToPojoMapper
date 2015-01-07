JDBC resultset to Java POJO mapper
==================================

Conveniently convert a row from a JDBC resultset to a java object given that the database follows uppercase-underscore naming convention with names matching java object field names.  

IE
#### database table and column names
> USER_DETAIL.USER_ID  
> USER_DETAIL.NUMERICAL_ID  
> USER_DETAIL.FIRST_NAME  
> USER_DETAIL.LAST_NAME  
> USER_DETAIL.PASSWORD  
> USER_DETAIL.BIRTH_DATE  

#### java pojo object field names matching column names above

	public class User {
	  String userId;  
	  Integer numericalId;  
	  String firstName;  
	  String lastName;  
	  String password;  
	  Date birthDate;  
	  ... + getters and setters for these fields
	}

Usage
=====
  DataSourceToPojoMapping dataSourceToPojoMapping = new DataSourceToPojoMapping();
	
	ResultSet rs = preparedStatement.execute();
	
	List<User> users=new ArrayList<User>();
	
	while (rs.next())
	{
		User user = dataSourceToPojoMapping.getMappedObj(rs, User.class);	
		users.add(user);
	}

