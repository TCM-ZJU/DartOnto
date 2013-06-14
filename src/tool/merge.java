package tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import mappingService.Constant;
import ontoService.ModelController;

import org.dom4j.DocumentException;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;

public class merge {
	public static void main(String args[]) throws DocumentException{
		Model m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
//	use the FileManager to find the input file
		InputStream in = ModelController.class.getClassLoader().getResourceAsStream("etc/newonto.owl");
		//InputStream in = FileManager.get().open( inputFileName );
		if (in == null)
			System.out.println("File: " + "etc/newonto.owl" + " not found");
//	read the RDF/XML file
		m.read(in, "");
		Model m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
//	use the FileManager to find the input file
		InputStream in1 = ModelController.class.getClassLoader().getResourceAsStream("etc/onto.owl");
		//InputStream in = FileManager.get().open( inputFileName );
		if (in1 == null)
			System.out.println("File: " + "etc/onto.owl" + " not found");
//	read the RDF/XML file
		m1.read(in1, "");
		m.add(m1);
		OutputStreamWriter writer;
		try {
      FileOutputStream os = new FileOutputStream(Constant.etc+"/newonto.owl");
      //writer = new OutputStreamWriter(os, "gb2312") ;
      writer = new OutputStreamWriter(os, "UTF-8") ;
      m.write(writer,"RDF/XML-ABBREV");//,"N-TRIPLE","RDF/XML-ABBREV"
	  } catch (IOException e) {
	   	System.out.println( "IO Exception");
	    e.printStackTrace();
	  } catch (InvalidPropertyURIException e) {
	  	System.out.println(e.getMessage());
	   	System.out.println( "Property URI Exception");
	  	e.printStackTrace();
	  }
		System.out.println("over");
	}
}
