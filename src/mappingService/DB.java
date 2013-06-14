package mappingService;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class DB {
	public DB(Element element){
		dbName = element.element("dbResQname").element("localpart").getText();
		
		Element localDbResType = element.element("localDbResType");
		url = localDbResType.element("jdbcUrl").getText();
		user = localDbResType.element("jdbcUser").getText();
		pwd = localDbResType.element("jdbcPwd").getText();
		driver = localDbResType.element("jdbcDriver").getText();
		schema = user.toUpperCase();
		
		List<?> regexPatterns = element.element("tableNameFilter").elements("regex");
		for(int i=0; i<regexPatterns.size(); i++){
			Element regexPattern = (Element) regexPatterns.get(i);
			regex.add(regexPattern.getText());
		}

		List<?> excludePatterns = element.element("tableNameFilter").elements("exclude");
		for(int i=0; i<excludePatterns.size(); i++){
			Element excludePattern = (Element) excludePatterns.get(i);
			exclude.add(excludePattern.getText());
		}
		con = conn();
	}
	
	public Connection conn(){
		try {
		Class.forName(driver);
		Connection con = DriverManager.getConnection(url, user, pwd);
		return con;
		}catch(ClassNotFoundException cf){
			System.out.println("cannot find class"+cf);
			return null;
		}catch(SQLException sqle){
			System.out.println("cannot connection db:"+sqle);
			return null;
		} catch (Exception e) {
			System.out.println("Failed to load JDBC/ODBC driver.");
			return null;
		}
	}
	
	/*public void getTables(Element dbEle){
		String dbName = dbEle.attributeValue("label");
		System.out.println("<<"+dbName+">>");
		try {
    	DatabaseMetaData dbmd = con.getMetaData();
      String[] types = new String[1];
			types[0] = "TABLE"; 
			ResultSet results = dbmd.getTables(null, null, "%", types);
			while (results.next()) {
				String tableSchema = results.getObject(2).toString();
				String tableName = results.getObject(3).toString();
				String fullname = tableSchema + "." + tableName;
				boolean in = true;
				for(int i=0; i<regex.size() && in; i++){
					if(!fullname.matches(regex.get(i)))
						in = false;
				}
				for(int i=0; i<exclude.size() && in; i++){
					if(fullname.matches(exclude.get(i)))
						in = false;
				}
				if(in){// && !tables.contains(tableName)
					System.out.println(tableSchema+" "+tableName);
					Element tableEle = dbEle.addElement("table").addAttribute("label", tableName).addAttribute("localName", dbName+"."+tableName);
					List<String> columns = getColumn(tableSchema, tableName);
		  		for(int k=0; k<columns.size(); k++){
		    		String column = columns.get(k);
		    		tableEle.addElement("column").addAttribute("label", column);
		    	}
				}
			}
			results.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	}*/
	
	public List<String> getTable(){
		List<String> tables = new ArrayList<String>();
    try {
    	DatabaseMetaData dbmd = con.getMetaData();
      String[] types = new String[1];
			types[0] = "TABLE"; 
			ResultSet results = dbmd.getTables(null, null, "%", types);
			while (results.next()) {
				String tableSchema = results.getObject(2).toString();
				String tableName = results.getObject(3).toString();
				String fullname = tableSchema + "." + tableName;
				boolean in = true;
				for(int i=0; i<regex.size() && in; i++){
					if(!fullname.matches(regex.get(i)))
						in = false;
				}
				for(int i=0; i<exclude.size() && in; i++){
					if(fullname.matches(exclude.get(i)))
						in = false;
				}
				if(in)// && !tables.contains(tableName)
					tables.add(tableName);
			}
			results.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return tables;
	}
	
	public List<String> getColumn(String tableName){
		return getColumn(schema, tableName);
	}
	private List<String> getColumn(String schemaName, String tableName){
		//System.out.println("table: " + tableName);
		List<String> columns = new ArrayList<String>();
    try {
    	DatabaseMetaData dbmd = con.getMetaData();
    	ResultSet rSet = dbmd.getColumns(null, schemaName, tableName, null);
			//.getPrimaryKeys(null, null, tableName);
			while(rSet.next()){
				String columnName = rSet.getObject(4).toString();
				if(!columns.contains(columnName))
					columns.add(columnName);
			}
			rSet.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return columns;
	}
	
	public String getColumnType(String tableUri, String column){
		String tableName = tableUri.substring(tableUri.lastIndexOf('.')+1);
		String columnType = "";
    try {
    	DatabaseMetaData dbmd = con.getMetaData();
    	ResultSet rSet = dbmd.getColumns(null, schema, tableName, null);
			//.getPrimaryKeys(null, null, tableName);
			while(rSet.next()){
				String columnName = rSet.getString(4);
        String dbColumnTypeName = rSet.getString(6);
				if(column.equals(columnName))
					columnType = dbColumnTypeName;
			}
			rSet.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return columnType;
	}
	
	public String getName(){
		return dbName;
	}
	
	private String dbName;
	private String url="";
	private String user="";
	private String pwd="";
  private String driver = "";
  private String schema = null;
  private List<String> regex = new ArrayList<String>();
  private List<String> exclude = new ArrayList<String>();
  private Connection con = null;
}
