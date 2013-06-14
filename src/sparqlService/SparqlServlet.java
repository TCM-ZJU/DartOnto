package sparqlService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import dartgrid.QueryEngine;

public class SparqlServlet extends HttpServlet {

	/**
   * 添加本体类
   */
  private static final long serialVersionUID = -2552838843527128354L;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {//doPut
  	String sparql = req.getParameter("sparql");
  	try {
  		sparql = new String(sparql.getBytes("ISO8859_1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("cannot decode word: " + sparql);
			e.printStackTrace();
		}
  	System.out.println("执行Sparql语句：" + sparql);
    String xml = QueryEngine.xmlSparql(sparql);
		System.out.println(xml);
		logger.info("执行Sparql语句：" + sparql);
		
		resp.setContentType("text/xml"); 
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Cache-Control", "no-cache");
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			ou.write(xml);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	  
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
		String xml = QueryEngine.xmlSparql(QueryEngine.generateQuery());
		System.out.println(xml);
		t2=(new Date()).getTime();
    System.out.println("执行Sparql语句：" + (t2-t1)+"毫秒");
	}
	
	private final static Logger logger = Logger.getLogger(SparqlServlet.class);
}
