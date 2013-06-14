package instanceService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import connection.ConnectionPool;
import connection.HierarchyOperator;
import dartgrid.QueryEngine;
import cn.edu.zju.dart.query.result.QueryResult;

public class ConceptEngine {

	/**
	 * get concepts list with word as alias and concept
	 * @param word
	 * @return concepts list
	 */
	public static List<String> getConcept(String word){
		List<String> concepts = null;
		//在Alias里查询
		concepts = getConceptByAlias(word);
	  //在Concept里查询
		if(isConcept(word) && !concepts.contains(word))
			concepts.add(0, word);
		return concepts;
	}
	
	/**
	 * get concepts list with word as alias and concept
	 * @param word
	 * @param fuzzyRange 模糊查询范围
	 * @return concepts list
	 */
	public static List<String> getConcept(String word, int fuzzyRange){
		List<String> concepts = null;
	  //在Concept里查询
		concepts = getConceptByConcept(word, true, fuzzyRange);
		//在Alias里查询
		if(concepts.size() < fuzzyRange){
			List<String> conceptsByAlias = getConceptByAlias(word, true, fuzzyRange-concepts.size());
			for(int i=0; i<conceptsByAlias.size(); i++ )
				if(!concepts.contains(conceptsByAlias.get(i)) )
					concepts.add(conceptsByAlias.get(i)); 
		}
		return concepts;
	}
	
	public static List<String> getConceptByAlias(String word){
		return getConceptByAlias(word, false,0); 
	}
	
	public static List<String> getConceptByAlias(String word, boolean fuzzy, int fuzzyRange){    
		List<String> concepts = new ArrayList<String>();
		String getConcept =	"PREFIX skos: <" + skosPrefix + "#> " +
												"SELECT distinct ?concept " +
												"WHERE {	?x skos:altLabel ?alias. "+
												"       	?x skos:prefLabel ?concept.";
		getConcept += fuzzy?"					FILTER(regex( ?alias, '" + word + "')).}"/* +
												"					LIMIT " + fuzzyRange */
											 :"					FILTER( ?alias='" + word + "').}";
		QueryResult r;
    try {
	    r = QueryEngine.sparql(getConcept);
    } catch (Exception e) {
	    e.printStackTrace();
	    return new ArrayList<String>();
    }
    while(r.next()){
    	concepts.add(r.getString(0));
		}
    return concepts;	
	}
	
	public static boolean isConcept(String word){		
		return !getConceptByConcept(word, false, 0).isEmpty(); 
	}
	public static List<String> getConceptByConcept(String word, boolean fuzzy, int fuzzyRange){    
    List<String> concepts = new ArrayList<String>();
		String getConcept = "PREFIX skos: <" + skosPrefix + "#> " +
										  	"SELECT distinct ?concept " +
										  	"WHERE { 	?x skos:prefLabel ?concept.";
    getConcept += fuzzy ?
    							"					FILTER(regex( ?concept, '" + word + "')).}"
    							// + "					LIMIT " + fuzzyRange 
								: "					FILTER( ?concept='" + word + "').}";
    QueryResult r;
    try {
	    r = QueryEngine.sparql(getConcept);
    } catch (Exception e) {
	    e.printStackTrace();
	    return concepts;
    }
    while(r.next()){
    	concepts.add(r.getString(0));
		}
    return concepts;	
	}

	public static List<String> getAliasByConcept(String word){
		return getAliasByConcept(word, false); 
	}
	public static List<String> getAliasByConcept(String word, boolean fuzzy){    
    List<String> concepts = new ArrayList<String>();
    String sparql =	"PREFIX skos: <" + skosPrefix + "#> " +
										"SELECT distinct ?alias " +
										"WHERE {?x skos:altLabel ?alias. "+
										"       ?x skos:prefLabel ?concept.";
		sparql += fuzzy ?
    							"					FILTER(regex( ?concept, '" + word + "')).}" 
								: "					FILTER( ?concept='" + word + "').}";
		QueryResult r;
    try {
	    r = QueryEngine.sparql(sparql);
    } catch (Exception e) {
	    e.printStackTrace();
	    return new ArrayList<String>();
    }
    while(r.next()){
    	String concept = r.getString(0);
    	if( !concept.equals("") )
    		concepts.add(concept);
		}		
    return concepts;	
	}

	public static Element getContent(String concept, String property){
		return getContent(concept, property,1);		
	}
	
	public static Element getContent(String concept, String property, int tier){
    long t1=(new Date()).getTime();
    
		String sparql = getSparql(concept,property);
		Document reDoc = DocumentHelper.createDocument();
		Element root = reDoc.addElement("Results");
		Element item = null;
		QueryResult r;
		String db = "";
    try {
	    r = QueryEngine.sparql(sparql);
	    while(r.next()){
	    	String content = r.getString(0);
	    	if(content.equals(""))
	    		continue;
	    	if(content.contains(";"))
	    		content.replace(';', '；');
				//item = root.addElement(property);
				item = root.addElement("result");
				item.setText(content);
				if(property.equals("父类")||property.equals("子类"))
					item.addAttribute("db", r.getDBName());
				else
					item.addAttribute("db", ConnectionPool.getDB(r.getDBName()));			
				if(property.equals("关联词"))
					item.addAttribute("rela", r.getString(1));
	    }
    	if(property.equals("父类")||property.equals("子类")){
				List<?> elements = root.elements();
				for(int i=0; i<elements.size(); i++)
					HierarchyOperator.getChildren((Element)elements.get(i), property, tier-1);
				HierarchyOperator.getChildren(root, "ztc", property, concept, tier, true);
				HierarchyOperator.getChildren(root, "pharmic", property, concept,tier, true);
				elements = root.elements();
				for(int i=0; i<elements.size(); i++){
					item = (Element)elements.get(i);
					db = item.attributeValue("db");
					item.addAttribute("db", ConnectionPool.getDB(db));
				}
    	}

			long t2=(new Date()).getTime();
      System.out.println("查询'"+concept+"'("+property+")"+"的时间：" + (t2-t1)+"毫秒");
			logger.info("查询'"+concept+"'("+property+")"+"的时间：" + (t2-t1)+"毫秒");
			System.out.println(reDoc.asXML());
			
    } catch (Exception e) {
	    logger.error("Dart Grid: Query with DB("+db+") Error! --> " + concept + "(" + property +")");
	    e.printStackTrace();
    }
    return root;
	}
	
	public static Element getContent(String prefLabel, String propertyUri, boolean isProperty){
    String sparql = getContentSparql(prefLabel, propertyUri, isProperty);
		Document reDoc = DocumentHelper.createDocument();
		Element root = reDoc.addElement("Results");
		Element item = null;
		QueryResult r;
		String db = "";
    try {
	    r = QueryEngine.sparql(sparql);
	    if(r == null)
	    	return root;
	    while(r.next()){
	    	String content = r.getString(0);
	    	if(content.equals(""))
	    		continue;
	    	if(content.contains(";"))
	    		content.replace(';', '；');
				//item = root.addElement(property);
				item = root.addElement("result");
				item.setText(content);
				item.addAttribute("db", ConnectionPool.getDB(r.getDBName()));	
	    }			
    } catch (Exception e) {
    	System.out.println(sparql);
	    logger.error("Dart Grid: Query with DB("+db+") Error! --> " + prefLabel + "(" + propertyUri +")");
	    e.printStackTrace();
    }
    return root;
	}
		
	public static String getContentSparql(String prefLabel, String propertyUri, boolean isProperty){
    return getContentSparql(prefLabel, propertyUri, isProperty, false);
	}
	
	public static String getContentSparql(String prefLabel, String propertyUri, boolean isProperty, boolean isOpposed){
    String propertyLocalName = propertyUri
															.replaceFirst(ConceptEngine.ontoPrefix+"#", "ns:")
															.replaceFirst(ConceptEngine.skosPrefix+"#", "skos:")
    													.replaceFirst(ConceptEngine.dcPrefix, "dc:");
    String sparql = "PREFIX skos: <" + skosPrefix + "#> \n" +
										"PREFIX ns: <" + ConceptEngine.ontoPrefix + "#> \n" +
										"PREFIX dc: <" + ConceptEngine.dcPrefix + "> \n" +
										"SELECT ?p \n"+
										"WHERE {?x skos:prefLabel '" + prefLabel + "'. \n"+
			 (isProperty? "       ?x "+ propertyLocalName +" ?p. }\n"
									:(
				(isOpposed? "       ?c "+ propertyLocalName +" ?x. \n"
									: "       ?x "+ propertyLocalName +" ?c. \n") +
										"       ?c skos:prefLabel ?p. } \n"));
    return sparql;
	}
	
	private static String getSparql(String concept, String tabName){
		if(tabName.equals("定义")){// distinct
			return	"PREFIX skos: <" + skosPrefix + "#> " +
							"SELECT ?definition " +
							"WHERE {?x skos:definition ?definition. "+
							"       ?x skos:prefLabel '" + concept + "'. }";
		}else if(tabName.equals("异名")){// distinct
			return 	"PREFIX skos: <" + skosPrefix + "#> " +
							"SELECT ?alias " +
							"WHERE {?x skos:altLabel ?alias. "+
							"       ?x skos:prefLabel '" + concept + "'. }";
		}else if(tabName.equals("父类")){
			return	"PREFIX skos: <" + skosPrefix + "#> " +
							"SELECT ?pconcept " +
							"WHERE {?x skos:broader ?p." +
							"       ?p skos:prefLabel ?pconcept. "+
							"       ?x skos:prefLabel '" + concept + "'. }";
		}else if(tabName.equals("子类")){
			return	"PREFIX skos: <" + skosPrefix + "#> " +
							"SELECT ?cconcept " +
							"WHERE {?x skos:narrower ?c." +
							"       ?c skos:prefLabel ?cconcept. "+
							"       ?x skos:prefLabel '" + concept + "'. }";
		}else if(tabName.equals("关联词")){
			return	"PREFIX skos: <" + skosPrefix + "#> " +
							"PREFIX ns: <" + ontoPrefix + "#> " +
							"SELECT ?rconcept ?rn " +
							"WHERE {?x ns:hasRela ?b." +
							"       ?x skos:prefLabel '" + concept + "'. " +
							"       ?b ns:relaConcept ?rc. "+
							"       ?rc skos:prefLabel ?rconcept. "+
							"				?b ns:relaname ?rn.}";//
							//"       ?rc skos:prefLabel ?rconcept. "+
							//"       OPTIONAL {?b ns:relaname ?rn.} }";
		}else
			return "";
	}
	
	public static void main(String args[]){
		/*List<String> concepts = getConcept("大黄");
		for(int i=0; i<concepts.size(); i++)
			System.out.println(concepts.get(i));*/
		//getContent("大黄","父类");
		//getContent("大黄","子类");
		System.out.println("大黄" + isConcept("大黄"));//true
		System.out.println("将军" + isConcept("将军"));//false
		System.out.println("小大黄" + isConcept("小大黄"));//true
		System.out.println("山大黄" + isConcept("山大黄"));//true
  }	
	public static String ontoPrefix = "http://cintcm.ac.cn/onto";
	public static String skosPrefix = "http://www.w3.org/2004/02/skos/core";
	public static String dcPrefix = "http://purl.org/dc/elements/1.1/";
	private final static Logger logger = Logger.getLogger(ConceptEngine.class);
}
