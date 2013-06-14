
package dart.mapping
{
    
import flash.events.Event;

public class TableThumbEvent extends Event
{
    public static const ADD:String = "add";
    public static const DELETE:String = "delete";
    public static const UPDATE:String = "update";
    public static const BROWSE:String = "browse";
    
    public var table:XML;
    
    public function TableThumbEvent(type:String, table:XML)
    {
        super(type);
        this.table = table;
    }
    
    override public function clone():Event
    {
        return new TableThumbEvent(type, table);
    }
}

}