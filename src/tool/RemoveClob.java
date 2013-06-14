package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mappingService.Constant;
import mappingService.DB;
import mappingService.DBController;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class RemoveClob {
	private static DBController dc = DBController.getController();
	public static void main(String args[]) throws DocumentException{
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
		  try {
		  	System.out.println(files.get(i));
		  	document = saxReader.read(new InputStreamReader(new FileInputStream(new File(Constant.etc + "\\" + files.get(i))),"utf-8"));
	    } catch (DocumentException e) {
		    e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
      } catch (FileNotFoundException e) {
	      e.printStackTrace();
      }
	    List<?> tables = document.getRootElement().elements("table");
	    for(int j=0; j<tables.size(); j++){
	    	Element table = (Element) tables.get(j);
	    	String tableUri = table.elementText("uri");
	    	DB db = dc.getDB(tableUri);	    	
	    	Element ontologies = table.element("ontologies");
	    	if(ontologies == null)
	    		continue;
	    	List<?> ontologyList = ontologies.elements("ontology");
	    	for(int k=0;k<ontologyList.size(); k++){
	  			Element ontoProps = ((Element) ontologyList.get(k)).element("ontoProps");
	  	    if(ontoProps == null)
	  	    	continue;
	  	    List<?> ontoPropList = ontoProps.elements("ontoProp");
	  	    for(int l=0;l<ontoPropList.size(); l++){
	  				Element e = (Element) ontoPropList.get(l);
	  				String column = e.elementText("column");
	  				if(column == null)
	  					continue;
	  				String type = db.getColumnType(tableUri, column);
	  				System.out.println(tableUri+"-"+column+":"+type);
	  				if(type.equals("CLOB")){
	  					ontoProps.remove(e);
	  					System.out.println("[remove]");
	  				}
	  			}
	  		}
	    }
	    OutputFormat format = OutputFormat.createPrettyPrint();
	    format.setEncoding("UTF-8");
	    try{ 
				XMLWriter output = new XMLWriter(new FileOutputStream("src\\etc\\"+files.get(i)), format);
				output.write(document);
				output.close();	    	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		}
		System.out.println("over");
	}
}
