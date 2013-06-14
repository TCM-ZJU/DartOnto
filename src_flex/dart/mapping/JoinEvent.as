package dart.mapping
{
    
import flash.events.Event;

public class JoinEvent extends Event
{
    public static const ADD:String = "add";
    public static const EDIT:String = "edit";
    
    public var join:XML;
    
    public function JoinEvent(type:String, join:XML)
    {
        super(type);
        this.join = join;
    }
    
    override public function clone():Event
    {
        return new JoinEvent(type, join);
    }
}

}