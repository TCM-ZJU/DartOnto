package mappingService;

public class RemoteDB {
	public String sayHelloTo(String str) {
		System.out.println("Hello " + str);		
		return "Hi~ " + str;
	}
	
	public String addDB(String localName, String url, String user, String pwd, String type){
		System.out.println("添加数据库：" + localName + " " + localName + " " + url + " " + pwd + " " + type);
		return DBController.getController().addDB(localName, url, user, pwd, type);
	}
	
	public boolean deleteDB(String localName){
		System.out.println("删除数据库：" + localName);		
		return DBController.getController().deleteDB(localName);
	}
	
	public boolean save(){
		System.out.println("保存数据库注册文件，并导入数据本体");
		return DBController.getController().saveAndImport();
	}
	
	public Object[] getColumn(String table) {
		System.out.println("查询数据表域列表：" + table);
		return DBController.getController().getColumn(table).toArray();
	}
	
	public Object[] getTable(String table) {
		System.out.println("查询数据库表列表：" + table);
		return DBController.getController().getTable(table).toArray();
	}
	
	public static void main(String args[]){
		System.out.println(DBController.getController().getTable("{http://ccnt.cn/mt}ztc.ZTC0"));
	}
}
