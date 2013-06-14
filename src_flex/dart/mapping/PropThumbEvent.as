package dart.mapping
{
    
import flash.events.Event;

public class PropThumbEvent extends Event
{
    public static const ADD:String = "add";
    public static const DELETE:String = "delete";
    public static const UPDATE:String = "update";
    
    public var prop:XML;
    public var oldProp:XML;
    
    public function PropThumbEvent(type:String, prop:XML, oldProp:XML=null)
    {
        super(type);
        this.prop = prop;
        this.oldProp = oldProp;
    }
    
    override public function clone():Event
    {
        return new PropThumbEvent(type, prop, oldProp);
    }
}

}