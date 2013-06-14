package dart
{
	import flash.events.Event;

	public class MyListEvent extends Event
	{
		public static const SEND_XML:String = "sendXML";
		public var xml:XML;
		public function MyListEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
	}
}