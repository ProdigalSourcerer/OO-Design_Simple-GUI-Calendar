import java.io.Serializable;
import java.util.Comparator;
import java.util.GregorianCalendar;

public class CalendarEvent implements Serializable {
	public CalendarEvent(GregorianCalendar eventStart, GregorianCalendar eventEnd, String eventTitle)
	{
		start = eventStart;
		end = eventEnd;
		title = eventTitle;
	}
	
	public enum Field {EVENT_START, EVENT_END}
	
	/**
	 * Utility method - Factory constructs and returns a comparator. 
	 * @param field		an instance of the enumerated type CalendarEvent.Field (either 
	 * 						EVENT_START or EVENT_END) specifying which field to compare between
	 * 						two CalendarEvents
	 * @return			a Comparator<CalendarEvent> that will compare two CalendarEvent 
	 * 						objects based on the specified field. If no field is  specified 
	 * 						i.e., field is null), a default comparator is returned based on
	 * 						event start time
	 */
	public static Comparator<CalendarEvent> getComparator(Field field)
	{
		if(field == null || field == Field.EVENT_START)
		{return new ComparatorByStart();}
		
		if(field == Field.EVENT_END)
		{return new ComparatorByEnd();}
		
		else return null;
	} // getComparator()
	
	/**
	 * Utility class - Serializing TreeSet requires comparators to implement Serializable
	 * interface, therefore comparators cannot be anonymous (anonymous classes cannot implement
	 * more than one interface)
	 * @author Iain Davis
	 */
	private static class ComparatorByStart implements Comparator<CalendarEvent>, Serializable
	{@Override
		public int compare(CalendarEvent thisOne, CalendarEvent thatOne) {
			return thisOne.getStart().compareTo(thatOne.getStart());
		}
	}
	
	/**
	 * Utility class - Serializing TreeSet requires comparators to implement Serializable
	 * interface, therefore comparators cannot be anonymous (anonymous classes cannot implement
	 * more than one interface)
	 * @author Iain Davis
	 */
	private static class ComparatorByEnd implements Serializable, Comparator<CalendarEvent>
	{@Override
		public int compare(CalendarEvent thisOne, CalendarEvent thatOne) {
			return thisOne.getEnd().compareTo(thatOne.getEnd());
		}
	}
	
	/**
	 * Accessor method
	 * @return	a copy of the GregorianCalendar object representing the event start time.
	 */
	protected GregorianCalendar getStart(){return (GregorianCalendar) start.clone();}
	
	/**
	 * Accessor method
	 * @return	a copy of the GregorianCalendar object representing the event end time.
	 */
	protected GregorianCalendar getEnd(){return (GregorianCalendar) end.clone();}
	
	/**
	 * Accessor method
	 * @return a String representing the title of this event
	 */
	protected String getTitle(){return title;}	// Strings immutable => okay to pass reference to private member
	
	/**
	 * Utility method - gets a summary of this instance
	 * @return		a String representing a summary of this instance's fields
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("TITLE: " + title + "\n");
		sb.append("DATE: "  + CalendarModel.getFormattedDate(start) + "\n");
		sb.append("START: " + CalendarModel.getFormattedTime(start) + "\n");
		sb.append("END: "   + CalendarModel.getFormattedTime(end) + "\n");
		
		return sb.toString();
	}

	private GregorianCalendar start;
	private GregorianCalendar end;
	private String description; // not used yet - may add longer-form description if time allows
	private String title;
}
