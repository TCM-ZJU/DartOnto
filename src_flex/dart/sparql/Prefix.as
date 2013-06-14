package dart.sparql
{
import mx.controls.Alert;
public class Prefix {
	public function Prefix(token:String, pattern:RegExp, str:String) {
		this.pattern = pattern;
		this.token = token+":";
		this.str = str;
		this.preStr = "PREFIX "+this.token+" <"+str+">\n";
	}
	
	public var pattern:RegExp;
	public var token:String;
	private var str:String;
	public var preStr:String;
	
	public function match(where:String):Boolean {
		return where.indexOf(str)>0;
	}
}
}