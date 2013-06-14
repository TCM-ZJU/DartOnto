package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mappingService.Constant;

import ontoService.ModelController;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class AddPropertyLabel {
	private static ModelController mc = new ModelController();
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
	    List<?> tables = document.getRootElement().elements();
	    for(int j=0; j<tables.size(); j++){
	    	Element table = (Element) tables.get(j);
	    	Element ontologies = table.element("ontologies");
	    	if(ontologies == null)
	    		continue;
	    	List<?> ontologyList = ontologies.elements("ontology");
	    	add(ontologyList,true);
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
	
	private static void add(List<?> elements, boolean isOnto){
		for(int i=0;i<elements.size(); i++){
			Element e = (Element) elements.get(i);
			Element ontologyURI = e.element("ontologyURI");
			String uri = "";
			try {
				uri = ontologyURI.getText();
			} catch (NullPointerException e1) {
				System.out.println(e.asXML());
				System.out.println(ontologyURI.asXML());
	      e1.printStackTrace();
      }
			ontologyURI.addAttribute("label", mc.getResLabel(uri));
			/*try {
	      ontologyURI.addAttribute("label", mc.getResLabel(uri));
	      // ontologyURI.addAttribute("label", new String(mc.getResLabel(uri).getBytes("UTF-8"),"GBK"));
      } catch (UnsupportedEncodingException e1) {
	      // TODO Auto-generated catch block
	      e1.printStackTrace();
      }*/
			if(isOnto){
				Element ontoProps = e.element("ontoProps");
	    	if(ontoProps == null)
	    		continue;
	    	List<?> ontoPropList = ontoProps.elements("ontoProp");
	    	add(ontoPropList,false);
			}
		}
	}
}
