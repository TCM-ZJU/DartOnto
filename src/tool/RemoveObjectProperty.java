package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mappingService.Constant;

import ontoService.ModelController;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RemoveObjectProperty {
	private static ModelController mc = new ModelController();
	public static void main(String args[]) throws DocumentException{
		ExtendedIterator propIter = mc.getModel().listObjectProperties();
		List<String> propertyList = new ArrayList<String>();
		Map<String, Property> propertyMap = new HashMap<String, Property>();
		while(propIter.hasNext()){
			Property property = (Property)propIter.next();
			propertyList.add(property.getURI());
			propertyMap.put(property.getURI(), property);
		}
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
      List<Element> allTable = new ArrayList<Element>();
      List<?> tables = document.getRootElement().elements("table");
	    List<?>  views = document.getRootElement().elements("view");
	    for(int j=0; j<tables.size(); j++){
	    	allTable.add((Element) tables.get(j));
			}
	    for(int j=0; j<views.size(); j++){
	    	allTable.add((Element) views.get(j));
			}
	    for(int j=0; j<allTable.size(); j++){
	    	Element table = allTable.get(j);
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
	  				String ontologyURI = e.elementText("ontologyURI");
	  				System.out.println(ontologyURI);
	  				if(propertyList.contains(ontologyURI)){
	  					propertyList.remove(ontologyURI);
	  					propertyMap.remove(ontologyURI);
	  				}
	  			}
	  		}
	    }	    
		}
		for(int i=0; i<propertyList.size(); i++){
			Property p = propertyMap.get(propertyList.get(i));
			if(p.getNameSpace().equals("http://cintcm.ac.cn/onto#")){
				mc.getModel().removeAll(p,RDFS.domain,null);
				mc.getModel().removeAll(p,RDFS.range,null);
				System.out.print("Remove: ");
			}
			System.out.println(propertyList.get(i));
		}
		mc.output();
		System.out.println("over");
	}
}
