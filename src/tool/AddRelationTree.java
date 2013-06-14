package tool;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

import ontoService.ModelController;

public class AddRelationTree {
	public static void main(String args[]){
		ModelController mc = new ModelController();
		m = mc.getModel();
		OntProperty related = m.getOntProperty("http://www.w3.org/2004/02/skos/core#related");
		Document document = DocumentHelper.createDocument();
		SAXReader saxReader = new SAXReader();
		//File file = new File("src/tool/rela.xml");
	  try {
	    document = saxReader.read(AddRelationTree.class.getClassLoader().getResourceAsStream("tool/rela.xml"));
    } catch (DocumentException e) {
	    e.printStackTrace();
    }
	  Element rela = document.getRootElement();
	  addSubProperty(related, rela);
	  mc.output();
		System.out.println("over");
	}
	
	private static void addSubProperty(OntProperty pre, Element rela){
		List<?> children = rela.elements();
		for(int i=0; i<children.size(); i++){
			Element child = (Element) children.get(i);
			String label = child.attributeValue("name");
			OntProperty p = m.createOntProperty(base+(id++));
			p.addSuperProperty(pre);
			p.addLabel(label, "zh");
			addSubProperty(p, child);
		}
	}
	
	static int id = 0;
	static String base = "http://cintcm.ac.cn/onto#r";
	static OntModel m;
}
