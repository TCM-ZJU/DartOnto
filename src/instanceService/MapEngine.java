package instanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ontoService.ModelController;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.edu.zju.dart.query.result.QueryResult;
import dartgrid.QueryEngine;

public class MapEngine {

	public String getMap(String concept){
		String TypeUri = ConceptEngine.skosPrefix+"#Concept";
	  Element root = init();
	  Element centerNode = buildCenterNode(concept, TypeUri);
	  boolean includeConcept = false;
	  
	  String sparql = "PREFIX skos: <" + ConceptEngine.skosPrefix + "#> " +
										"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
										"SELECT distinct ?t " +
										"WHERE {	?x skos:prefLabel '"+concept+"'. "+
										"       	?x rdf:type	?t .} ";
	  QueryResult r;
		try {
			r = QueryEngine.sparql(sparql);
			while(r.next()){
				String typeUri = r.getString(0);
				if(typeUri.equals(TypeUri)){
					includeConcept = true;
					continue;
				}
				Element node = nodeArray.addElement("Node").addAttribute("id", nodeID+"");
			  node.addElement("Label").addText(concept);
			  node.addElement("TypeUri").addText(typeUri);
			  String typeLabel = model.getResLabel(typeUri);
			  node.addElement("Type").addText(typeLabel);
			  nodes.put(concept, node);
			  Element edge = edgeArray.addElement("Edge").addAttribute("id", edgeID+"");
				edge.addElement("Label").addText("作为"+typeLabel);
				edge.addElement("TypeUri").addText("as");
				edge.addElement("FromID").addText("1");
				edge.addElement("ToID").addText(nodeID+"");
				nodeID++;edgeID++;
			}
		} catch (Exception e) {
			logger.error("Dart Grid: Query Error! --> " + concept + "(RDF.type)");
		}

		List<?> nodeList = nodeArray.elements();
		if(includeConcept){
			buildMap(centerNode,true);
		}
		int j;
	  for(j=1; j<nodeList.size(); j++){
	  	Element node = (Element) nodeList.get(j);
		  buildMap(node,false);
	  }
	  nodeList = nodeArray.elements();
	  for(;j<nodeList.size(); j++){
	  	Element node = (Element) nodeList.get(j);
		  buildMap(node,true);
	  }
		return root.asXML();
	}
	
	public String getMap(String concept, String typeUri){
		if(typeUri == null || typeUri.equals(""))
			return getMap(concept);
	  Element root = init();
	  Element centerNode = buildCenterNode(concept, typeUri);
	  
	  buildMap(centerNode,true);
	  List<?> nodeList = nodeArray.elements();
		for(int j=1; j<nodeList.size(); j++){
			Element node = (Element) nodeList.get(j);
			buildMap(node,true);
		}
		return root.asXML();
	}

	private Element init(){
		Document doc = DocumentHelper.createDocument();
	  Element root = doc.addElement("motif");
	  nodeArray = root.addElement("NodeArray");
	  edgeArray = root.addElement("EdgeArray");	  
		return root;
	}
	private Element buildCenterNode(String concept, String typeUri){	  
	  Element centerNode = nodeArray.addElement("Node").addAttribute("id", nodeID+++"");
	  centerNode.addElement("Label").addText(concept);
	  centerNode.addElement("TypeUri").addText(typeUri);
	  centerNode.addElement("Type").addText(model.getResLabel(typeUri));
	  nodes.put(concept, centerNode);
		return centerNode;
	}
	private void buildMap(Element centerNode, boolean includeConcept){
		String concept = centerNode.elementText("Label");
		String typeUri = centerNode.elementText("TypeUri");
	  Element objects;
	  if(objectMap.containsKey(typeUri))
	  	objects = objectMap.get(typeUri);
	  else{
	  	objects = model.getClassObjectElement(typeUri,false,includeConcept);
	  	objectMap.put(typeUri, objects);
	  }
	  addRelations(centerNode, concept, objects.elements("relation"), false);
	  Element subjects;
	  if(subjectMap.containsKey(typeUri))
	  	subjects = subjectMap.get(typeUri);
	  else{
	  	subjects = model.getClassSubjectElement(typeUri,includeConcept);
	  	subjectMap.put(typeUri, subjects);
	  }
    addRelations(centerNode, concept, subjects.elements("relation"), true);
	}
	private void addRelations(Element centerNode, String concept, List<?> relations, Boolean isOpposed){
		if(!isOpposed)
			System.out.println("++++++++++++++"+concept+"++++++++++++++++++"+centerNode.asXML());
		for(int i=0; i<relations.size(); i++){
    	Element relation = (Element) relations.get(i);
    	String propertyUri = relation.attributeValue("propertyUri");
		  if(propertyUri.equals(ConceptEngine.ontoPrefix+"#hasRela")){
		  	buildRelaMap(centerNode);
				continue;
			}
		  if(propertyUri.equals(ConceptEngine.ontoPrefix+"#relaConcept"))
		  	continue;
    	String propertyLabel = relation.attributeValue("property");
    	String objectUri = relation.attributeValue("objectUri");
    	String objectLabel = relation.attributeValue("object");
    	String sparql = ConceptEngine.getContentSparql(concept, propertyUri, false, isOpposed);
    	QueryResult r;
  		try {
  			r = QueryEngine.sparql(sparql);
  			if(r==null)
  				continue;
  			int ct=0;
  			while(r.next() && ct<count){
  				ct++;
  				String content = r.getString(0);
  				Element node = null;
  				Boolean isNewNode = true;
  				if(nodes.containsKey(content)){
  					Element e = (Element) nodes.get(content);
  					if(e.elementText("TypeUri").equals(objectUri)){
  						isNewNode = false;
  						node = e;
  					}
  				}
  				if(isNewNode) {
  					node = nodeArray.addElement("Node").addAttribute("id", nodeID+++"");
    			  node.addElement("Label").addText(content);
    			  node.addElement("TypeUri").addText(objectUri);
    			  node.addElement("Type").addText(objectLabel);
    			  nodes.put(content, node);
  				}
  				Element edge = edgeArray.addElement("Edge").addAttribute("id", edgeID+++"");
  				edge.addElement("Label").addText(propertyLabel);
  				edge.addElement("TypeUri").addText(propertyUri);
  				edge.addElement("FromID").addText(isOpposed?node.attributeValue("id"):centerNode.attributeValue("id"));
  				edge.addElement("ToID").addText(isOpposed?centerNode.attributeValue("id"):node.attributeValue("id"));
  			}
  		}catch (java.lang.NullPointerException e) {
  			logger.error("Dart Grid: Query Error! --> " + concept + "("+propertyLabel+")");
  			r = null;
  			e.printStackTrace();
  		}
    }
		//if(isOpposed)
		//	System.out.println("++++++++++++++++++++++++++++++++");
	}
	
	private void buildRelaMap(Element centerNode){
		String concept = centerNode.elementText("Label");
		Element relaXML = ConceptEngine.getContent(concept, "关联词");
		//<result db="中医药学语言系统" rela="被…使用">论利湿清热</result>
		String objectUri = ConceptEngine.skosPrefix+"#Concept";
		List relas = relaXML.elements();
		for(int i=0; i<relas.size() && i<count; i++){
			Element rela = (Element) relas.get(i);
			String content = rela.getText();
			Element node = null;
			Boolean isNewNode = true;
			if(nodes.containsKey(content)){
				Element e = (Element) nodes.get(content);
				if(e.elementText("TypeUri").equals(objectUri)){
					isNewNode = false;
					node = e;
				}
			}
			if(isNewNode) {
				node = nodeArray.addElement("Node").addAttribute("id", nodeID+++"");
			  node.addElement("Label").addText(content);
			  node.addElement("TypeUri").addText(objectUri);
			  node.addElement("Type").addText("事物");
			  nodes.put(content, node);
			}
			Element edge = edgeArray.addElement("Edge").addAttribute("id", edgeID+++"");
			edge.addElement("Label").addText(rela.attributeValue("rela"));
			edge.addElement("TypeUri").addText("rela");
			edge.addElement("FromID").addText(centerNode.attributeValue("id"));
			edge.addElement("ToID").addText(node.attributeValue("id"));
		}			
	}
	
	private ModelController model = ModelController.getController();
  private Element nodeArray = null;
  private Element edgeArray = null;
  private Map<String,Element> nodes = new HashMap<String,Element>();
  private Map<String,Element> objectMap = new HashMap<String,Element>();
  private Map<String,Element> subjectMap = new HashMap<String,Element>();
  private int nodeID=1, edgeID=1;
	private final static Logger logger = Logger.getLogger(MapEngine.class);  
	int count = 3;
}
