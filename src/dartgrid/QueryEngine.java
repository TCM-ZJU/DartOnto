package dartgrid;

import java.io.File;

import mappingService.Constant;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hp.hpl.jena.query.QueryParseException;

import cn.edu.zju.dart.query.DartSparqlQuery;
import cn.edu.zju.dart.query.result.QueryResult;
import cn.edu.zju.dart.registry.impl.OwlRegistry;
import cn.edu.zju.dart.registry.impl.ResRegistry;
import cn.edu.zju.dart.registry.impl.SemRegistry;
//import cn.edu.zju.dart.util.GlobalTool;
import cn.edu.zju.dart.util.RunTimeGlobalPara;

public class QueryEngine {
	protected static boolean inited = false;
	public static void main(String args[]){		
		setup();
		String query = generateQuery();
		System.out.println(xmlSparql(query));
	}
	
	public static void setup(){
		//GlobalTool.setPrintable(true);
		if(inited)
			return;
		//File folder = new File(System.getProperty("user.dir"),Constant.etc);
		File folder = new File("C:/Program Files/apache-tomcat6/",Constant.etc);
		System.out.println(folder.toString());
		OwlRegistry owlreg = new OwlRegistry();
		owlreg.readDir(Constant.etc);
		RunTimeGlobalPara.setOwlRegistry(owlreg);
		ResRegistry resreg = new ResRegistry();		
		resreg.read(Constant.etc + "/tcm.resreg");
		RunTimeGlobalPara.setResRegistry(resreg);
		SemRegistry semreg = new SemRegistry();
		semreg.readDir(Constant.etc);
		//semreg.read(folder.toString() + "/pharmic.semreg");
		RunTimeGlobalPara.setSemRegistry(semreg);
		inited = true;
	}
	
	public static void reload(){
		inited = false;
		setup();
	}
	
	public static void reloadResreg(String file){
		//setup();
		ResRegistry resreg = RunTimeGlobalPara.getResRegistry();
		resreg.read(file);
	}
	
	public static void reloadSemreg(String file){
		//setup();
		SemRegistry semreg = RunTimeGlobalPara.getSemRegistry();
		semreg.read(file);
	}
	
	public static void removeSemreg(String file){
		setup();
		//SemRegistry semreg = RunTimeGlobalPara.getSemRegistry();
		//semreg.remove(file);
		//TODO remove semreg
	}
	
	public static String generateQuery(){
		String queryString = 
		/*"PREFIX ns: <http://cintcm.ac.cn/onto#> "+ 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
		"SELECT distinct ?concept " +
		"WHERE {	?s ns:use ?f." +
		"       	?s skos:prefLabel ?sc. "+
		"       	?f skos:prefLabel ?concept."+
		"		FILTER( ?sc='痰气郁结证').} ";*/
			
			//Alias
			/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
			"SELECT distinct ?concept " +
			"WHERE {	?s skos:prefLabel ?sc. "+
			"       	?s skos:altLabel ?concept."+
			"		FILTER( ?sc='大黄').} ";*/

			/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
			"SELECT distinct ?concept " +
			"WHERE {	?s skos:prefLabel '大黄'. "+
			"       	?s skos:altLabel ?concept.} ";*/
			
			//Defination
			/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
			"SELECT distinct ?concept " +
			"WHERE {	?s skos:prefLabel ?sc. "+
			"       	?s skos:definition ?concept."+
			"		FILTER( ?sc='大黄').} ";*/
			
			//Broader
			/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
			"SELECT distinct ?concept " +
			"WHERE {	?x skos:prefLabel '大黄'. "+
			"       	?x skos:broader ?p."+
			"       	?p skos:prefLabel ?concept.} ";*/

		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"SELECT distinct ?t " +
		"WHERE {	?x skos:prefLabel '李时珍'. "+
		"       	?x rdf:type	?t .} ";
		

		/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"SELECT ?x " +
		"WHERE {	?x skos:prefLabel '大黄'.} ";
		/*"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
		"SELECT distinct ?concept " +
		"WHERE {	?x skos:prefLabel ?sc. "+
		"       	?x skos:narrower ?p."+
		"       	?p skos:prefLabel ?concept."+
		"		FILTER( ?sc='蓼科').} ";*/
		
		//Rela
		/*"PREFIX ns: <http://cintcm.ac.cn/onto#> " +
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+ 
		"SELECT ?rconcept ?rn " +
		"WHERE {?x ns:hasRela ?b." +
		"       ?x skos:prefLabel ?concept." +
		"       ?b ns:relaconcept ?rconcept. "+
		"       OPTIONAL {?b ns:relaname ?rn.}" +
		"				FILTER( ?concept='大黄'). }";*/
		return queryString;
	}
	
	public static QueryResult sparql(String query){
		setup();
		DartSparqlQuery dsq = new DartSparqlQuery(query);
		//System.out.println(query);
		try{
			dsq.generateActivity();
		} catch(cn.edu.zju.dart.registry.exception.SemMappingNotFoundException e){
			System.out.println(e.getMessage());
			return null;
		}
		QueryResult qr = dsq.executeQuery();
		return qr;
	}
	
	public static String xmlSparql(String query){		
		QueryResult qr = null;
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("Results");
		try{
			qr = sparql(query);
		} catch(QueryParseException e){
			root.addElement("message").addText(e.getMessage());
			return doc.asXML();
		}	
		
		query = query.toUpperCase();
		query = query.substring(query.indexOf("SELECT"), query.indexOf("WHERE")).trim();
		String[] columns = query.split("\\ \\?");		
		Element header = root.addElement("header");
		int i=1;
		for(; i<columns.length; i++){
			header.addElement("c"+i).addText(columns[i]);
		}
		int length = i-1;
		header.addAttribute("length", length+"");
		
		while(qr.next()){
			Element result = root.addElement("result");
			for(i=0; i<length; i++){
				result.addElement(columns[i+1]).addText(qr.getString(i));
			}
		}
		return doc.asXML();
	}
}
