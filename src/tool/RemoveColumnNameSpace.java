package tool;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import mappingService.Constant;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class RemoveColumnNameSpace {
	public static void main(String args[]){
		File folder = new File(System.getProperty("user.dir"),Constant.etc);
		String[] all = folder.list();
		List<String> files = new ArrayList<String>();
		for (int i = 0; i < all.length; i++) {
			String filename = all[i];
			File single = new File(Constant.etc + "\\" + all[i]);
			if (single.isFile() && (filename.endsWith(".semreg")))
				files.add(filename);
		}
		for(int i=0; i<files.size(); i++){
			Document document = DocumentHelper.createDocument();
			SAXReader saxReader = new SAXReader();
			//File file = new File("src/tool/rela.xml");
		  try {
		    document = saxReader.read(AddRelationTree.class.getClassLoader().getResourceAsStream("etc/"+files.get(i)));
	    } catch (DocumentException e) {
		    e.printStackTrace();
	    }
	    List<?> joins = document.getRootElement().elements("join");
	    for(int j=0; j<joins.size(); j++){
	    	Element join = (Element) joins.get(j);
	    	remove(join);
	    }
	    OutputFormat format = OutputFormat.createPrettyPrint();
	    try{ 
				XMLWriter output = new XMLWriter(new FileWriter(new File(Constant.etc + "\\"+files.get(i))), format);
				output.write(document);
				output.close();	    	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		}
		System.out.println("over");
	}
	
	private static void remove(Element join){
		if(join == null)
			return;
		Element left = join.element("left");
		Element right = join.element("right");
		Element constraint = join.element("constraint");
		List<?> columns = constraint.elements("column");
		for(int i=0;i<columns.size(); i++){
			Element column = (Element) columns.get(i); 
			String columnString = column.getText();
			column.setText(columnString.substring(columnString.lastIndexOf('.')+1));
		}
		remove(left.element("join"));
		remove(right.element("join"));
	}
}
