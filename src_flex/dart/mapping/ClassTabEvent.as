package dart.mapping
{
    
import flash.events.Event;

public class ClassTabEvent extends Event
{
    public static const ADD:String = "add";
    public static const DELETE:String = "delete";
    public static const UPDATE:String = "update";
    public static const BROWSE:String = "browse";
    
    public var onto:XML;
    
    public function ClassTabEvent(type:String, onto:XML)
    {
        super(type);
        this.onto = onto;
    }
    
    override public function clone():Event
    {
        return new ClassTabEvent(type, onto);
    }
}

}