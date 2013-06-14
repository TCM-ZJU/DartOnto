package mappingService;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import dartgrid.QueryEngine;

public class DBController {
	public String getDBTree(){
		return tree.asXML();
	}
	private DBController(){
		SAXReader saxReader = new SAXReader();
		try {
			document = saxReader.read(DBController.class.getClassLoader().getResourceAsStream(filename));
			tree = DocumentHelper.createDocument();
	    resreg = document.getRootElement();
	    treeRoot = tree.addElement("dbs");
	    List<?> dbResInfos = resreg.elements("dbResInfo");
	    for(int i=0; i<dbResInfos.size(); i++){
	    	Element dbResInfo = (Element) dbResInfos.get(i);
	    	buildDBTreeNode(dbResInfo);
	    }
    } catch (DocumentException e) {
	    System.out.println("cannot find file");
	    e.printStackTrace();
    }
	}
	public static DBController getController(){
		if(controller == null)
			controller = new DBController();
		return controller;
	}
	
	public String buildDBTreeNode(Element dbResInfo){
  	DB db = new DB(dbResInfo);
  	String dbName = db.getName();
  	dbDic.put(dbName, db);
  	Element dbEle = treeRoot.addElement("db").addAttribute("label", dbName);
  	List<String> tables = db.getTable();
  	for(int j=0; j<tables.size(); j++){
  		String table = tables.get(j);
  		Element tableEle = dbEle.addElement("table").addAttribute("label", table).addAttribute("localName", dbName+"."+table);
  		List<String> columns = db.getColumn(table);
  		for(int k=0; k<columns.size(); k++){
    		String column = columns.get(k);
    		tableEle.addElement("column").addAttribute("label", column);
    	}
  	}
  	return dbEle.asXML();
	}
	private static DBController controller = null;
	private Document document;
	private Element resreg = null;
	private Document tree;
	private Element treeRoot = null;
	private final static String filename = "etc/tcm.resreg";
	public static void main(String args[]){
		System.out.println(DBController.getController().getDBTree());
	}
	
	/**
	<dbResInfo>
    <dbResQname>
      <namespace>http://ccnt.cn/mt</namespace>
      <localpart>tcmls</localpart>
    </dbResQname>
    <localDbResType dbType="Oracle" canBeTempDb="true" >
      <jdbcUrl>jdbc:oracle:thin:@10.214.33.111:1521:MT</jdbcUrl>
      <jdbcDriver>oracle.jdbc.driver.OracleDriver</jdbcDriver>
      <jdbcUser>tcmls</jdbcUser>
      <jdbcPwd>tcmls</jdbcPwd>
    </localDbResType>
    <tableNameFilter>
      <regex>TCMLS(.*)</regex>
      <exclude>(.*)BIN[$](.*)</exclude>
      <exclude>(.*)JENA[_](.*)</exclude>
    </tableNameFilter>
  </dbResInfo>
	 * @param localDbResType
	 * @param url
	 * @param user
	 * @param pwd
	 * @param driver
	 * @return
	 */
	public String addDB(String localName, String url, String user, String pwd, String type) {
		Element dbResInfo = resreg.addElement("dbResInfo");
		
		Element dbResQname = dbResInfo.addElement("dbResQname");
		dbResQname.addElement("namespace").addText("http://ccnt.cn/tcm");
		dbResQname.addElement("localpart").addText(localName);
		
		Element localDbResType = dbResInfo.addElement("localDbResType");
		localDbResType.addAttribute("dbType", type);
		localDbResType.addAttribute("canBeTempDb", "true");
		String driver = "oracle.jdbc.driver.OracleDriver";
		if(type.equals("Oracle")){
			url = "jdbc:oracle:thin:@" + url;
			driver = "oracle.jdbc.driver.OracleDriver";
		} else if(type.equals("Mysql")) {
			url = "jdbc:oracle:thin:@" + url;
			driver = "oracle.jdbc.driver.OracleDriver";
		}
		localDbResType.addElement("jdbcUrl").addText(url);
		localDbResType.addElement("jdbcDriver").addText(driver);
		localDbResType.addElement("jdbcUser").addText(user);
		localDbResType.addElement("jdbcPwd").addText(pwd);
		
		Element tableNameFilter = dbResInfo.addElement("tableNameFilter");
		tableNameFilter.addElement("regex").addText(user.toUpperCase()+"(.*)");
		tableNameFilter.addElement("exclude").addText("(.*)[$](.*)");
		
		System.out.println(dbResInfo.asXML());
		
		return buildDBTreeNode(dbResInfo);
  }
	public boolean deleteDB(String localName) {
		List<?> dbResInfos = resreg.elements("dbResInfo");
    for(int i=0; i<dbResInfos.size(); i++){
    	Element dbResInfo = (Element) dbResInfos.get(i);
    	if(dbResInfo.element("dbResQname").element("localpart").getText().equals(localName))
    		resreg.remove(dbResInfo);
    }
    List<?> dbs = treeRoot.elements("db");
    for(int i=0; i<dbs.size(); i++){
    	Element db = (Element) dbs.get(i);
    	if(db.attribute("label").equals(localName))
    		treeRoot.remove(db);
    }
    dbDic.remove(localName);
    return true;
  }
	public boolean saveAndImport() {
    try {
	    OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter output = new XMLWriter(
			new FileWriter(new File(Constant.etc+"/"+filename)), format);
			output.write(document);
			output.close();
    } catch (Exception e) {
    	return false;
		}
    QueryEngine.reloadResreg(Constant.etc+"/"+filename);
		return true;
  }
	public List<String> getColumn(String table) {
		String dbName = table.substring(table.indexOf('}')+1, table.lastIndexOf('.'));
		DB db = dbDic.get(dbName);
		String tableName = table.substring(table.lastIndexOf('.')+1);
		return db.getColumn(tableName);
	}
	public List<String> getTable(String table) {
		String dbName = table.substring(table.indexOf('}')+1, table.lastIndexOf('.'));
		DB db = dbDic.get(dbName);
		return db.getTable();
	}	
	public DB getDB(String table){
		String dbName = table.substring(table.indexOf('}')+1, table.lastIndexOf('.'));
		return dbDic.get(dbName);
	}
	private static Map<String,DB> dbDic = new HashMap<String,DB>(); 
}
