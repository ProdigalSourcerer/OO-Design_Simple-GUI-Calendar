import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CalendarModel {
	public CalendarModel()
	{
		readInEvents();
		notifyListeners();
	}

	/**
	 * Accessor method - gets the calendar portion of the current model state
	 * @return
	 */
	GregorianCalendar getSelectedDate()
	{return (GregorianCalendar) selectedDate.clone();}
	
	/**
	 * Accessor method - gets the events portion of the current model state
	 * @return
	 */
	SortedSet<CalendarEvent> getEvents()
	{
		GregorianCalendar lowBoundDate = (GregorianCalendar) selectedDate.clone();
		lowBoundDate.set(Calendar.HOUR_OF_DAY, 0);
		lowBoundDate.set(Calendar.MINUTE, 0);
		lowBoundDate.set(Calendar.SECOND, 0);
		lowBoundDate.set(Calendar.MILLISECOND, 0);
		
		GregorianCalendar highBoundDate = (GregorianCalendar) lowBoundDate.clone();
		highBoundDate.add(Calendar.DAY_OF_MONTH, 1);
		
		CalendarEvent lowBound = new CalendarEvent(lowBoundDate, lowBoundDate, null);
		CalendarEvent highBound = new CalendarEvent(highBoundDate, highBoundDate, null);
		return events.subSet(lowBound, highBound);
	}
	
	/**
	 * Utility method - gets the subset of events that are scheduled on the given date
	 * @param date_in	A GregorianCalendar representing the desired date. Time fields are ignored
	 * @return			The subset of the set of all events that consists of all events on the given date
	 * @precondition	date_in is an initialized GregorianCalendar
	 */
	SortedSet<CalendarEvent> getEvents(GregorianCalendar date_in)
	{
		if(date_in == null) return null;
		
		// get a copy of the current date and set it to the earliest time value for the given day
		GregorianCalendar lowBoundDate = (GregorianCalendar) date_in.clone();
		lowBoundDate.set(Calendar.HOUR_OF_DAY, 0);
		lowBoundDate.set(Calendar.MINUTE, 0);
		lowBoundDate.set(Calendar.SECOND, 0);
		lowBoundDate.set(Calendar.MILLISECOND, 0);
		
		// get a copy of the lower bound date and set it to the earliest time value for the next day
		GregorianCalendar highBoundDate = (GregorianCalendar) lowBoundDate.clone();
		highBoundDate.add(Calendar.DAY_OF_MONTH, 1);
		
		// retrieve the set all events following the lower bound (inclusive) and preceding
		// the upper bound (exclusive)
		CalendarEvent lowBound = new CalendarEvent(lowBoundDate, lowBoundDate, null);
		CalendarEvent highBound = new CalendarEvent(highBoundDate, highBoundDate, null);
		return events.subSet(lowBound, highBound);
	} // getEvents() 		(subset)
	
	/**
	 * Boolean accessor method checks whether any events exist on the given date
	 * @param date_in	A GregorianCalendar representing the date about which the client is enquiring
	 * @return			true if model has any events on the given date
	 * 					false if model has no events on the given date
	 */
	public boolean hasEvents(GregorianCalendar date_in)
	{
		SortedSet<CalendarEvent> eventsOnDate = getEvents(date_in);
		return eventsOnDate.size() != 0;
	}
	
	
	/**
	 * Mutator method - move the selected date by the specified
	 * number of the specified units (months, days, years)
	 * 
	 * @param calendarField an int representing the set of fields in
	 * 							the Calendar type, 
	 * 							Calendar.DAY_OF_MONTH,
	 * 							Calendar.DAY_OF_WEEK, 
	 * 							Calendar.MONTH, 
	 * 							Calendar.YEAR
	 * 							and so on. See the Calendar 
	 * 							documentation for all available 
	 * 							options
	 * @param value an int representing how many of the specified
	 * 					unit to move, and in which direction
	 */
	public void goTo(int calendarField, int value)
	{
		selectedDate.add(calendarField, value);
		notifyListeners();
		return;
	}
	
	/**
	 * Mutator method - moves the selected date directly to today (represented by the 
	 * current system time, obtained by instantiating a new GregorianCalendar)
	 */
	public void goToToday()
	{
		selectedDate = new GregorianCalendar();
		notifyListeners();
	}
	
	/**
	 * Mutator method - moves the selected date to a specified target date
	 * @param year		an int representing the desired year
	 * @param month		an int representing the desired month
	 * @param date		an int represeting the desired day-of-month
	 * @precondition	year, month, date must be positive integers
	 * @postcondition	the time fields of selectedDate will be unchanged
	 */
	public void goToDate(int year, int month, int date)
	{
		selectedDate.set(year, month, date);
		notifyListeners();
	}
	
	/**
	 * Mutator method - adds an event to the model
	 * @param ce_in		a CalendarEvent passed in to be added
	 * @precondition 	ce_in must be non-null and have no conflicting events previously loaded
	 * 					in the calendar
	 */
	public boolean addEvent(CalendarEvent ce_in)
	{
		// validate preconditions
		if(ce_in == null || hasConflictingEvent(ce_in)) return false;
		
		// add event and notify listeners
		events.add(ce_in);
		notifyListeners();
		return true;
	} // addEvent()
	
	public boolean deleteEvent(CalendarEvent ce_in)
	{
		boolean result = events.remove(ce_in);
		notifyListeners();
		return true;
	}
	
	/**
	 * Accessor method - checks a client-supplied CalendarEvent for conflicts with events
	 * 						already existing in the collection
	 * @param ce_in		the client-supplied CalendarEvent
	 * @return			true if the time-span represented by this event overlaps the time-span
	 * 						representing any event in the collection
	 * 					false otherwise
	 * @precondition	ce_in is non-null
	 */
	public boolean hasConflictingEvent(CalendarEvent ce_in)
	{
		// check preconditions
		if(ce_in == null) return true;
		
		for(CalendarEvent ce : events)
		{
			// events start at the same time => conflict
			if(ce_in.getStart().get(Calendar.YEAR) == ce.getStart().get(Calendar.YEAR)
					&& ce_in.getStart().get(Calendar.MONTH) == ce.getStart().get(Calendar.MONTH)
					&& ce_in.getStart().get(Calendar.DAY_OF_MONTH) == ce.getStart().get(Calendar.DAY_OF_MONTH)
					&& ce_in.getStart().get(Calendar.HOUR_OF_DAY) == ce.getStart().get(Calendar.HOUR_OF_DAY)
					&& ce_in.getStart().get(Calendar.MINUTE) == ce.getStart().get(Calendar.MINUTE))
			{
				return true;
			}

			// get references to the two events that specify their starting order
			CalendarEvent earliest = ce_in.getStart().before(ce.getStart()) ? ce_in : ce;
			CalendarEvent latest = earliest == ce ? ce_in : ce;
			
			// later event begins before earlier event ends => conflict
			if(latest.getStart().before(earliest.getEnd())) return true;
		}
		return false;
	}
	
	/**
	 * Utility method - Notifies all listeners of change in state.
	 */
	private void notifyListeners()
	{
		for(ChangeListener cl: listeners) {cl.stateChanged(new ChangeEvent(this));}
		return;
	}
	
	/**
	 * Utility method - adds a listener to the collection
	 * @param newListener - an object that implements the ChangeListener interface
	 * 							and wants to be notified of changes in this model's state
	 */
	public void attachListener(ChangeListener newListener)
	{listeners.add(newListener);}
	
	/*
	 * The following three snippets of code are adapted from one acquired from 
	 * StackOverflow.com courtesy of user assylias.
	 * I've adapted and renamed them to suit my purposes.
	 */
	/**
	 * Accessor method - produces a formatted version of the date portion of the 
	 * 						GregorianCalendar object field selectedDate
	 * @return			 a String representing the formatted date
	 * @author Iain Davis after StackOverflow user <a href="http://stackoverflow.com/users/829571/assylias">assylias </a>
	 */
	public String getFormattedDate(){
	    SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, yyyy");
	    fmt.setCalendar(selectedDate);
	    String dateFormatted = fmt.format(selectedDate.getTime());
	    return dateFormatted;
	}
	
	/**
	 * Static Accessor Method - produces a formatted version of the date portion of a 
	 * 								client-provided GregorianCalendar object
	 * @param gc_in		the GregorianCalendar object
	 * @return			A string representing the formatted date OR
	 * 						an empty string if gc_in is null
	 * @author Iain Davis after StackOverflow user <a href="http://stackoverflow.com/users/829571/assylias">assylias </a>
	 */
	public static String getFormattedDate(GregorianCalendar gc_in)
	{
		// verify precondition
		if(gc_in == null) return "";
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
	    fmt.setCalendar(gc_in);
	    return fmt.format(gc_in.getTime());
	}
	
	/**
	 * Static Accessor Method - produces a formatted version of the time portion of a 
	 * 								client-provided GregorianCalendar object
	 * @param gc_in		the GregorianCalendar object
	 * @return			A string representing the formatted time (24 hr clock) OR
	 * 						an empty string if gc_in is null
	 * @author Iain Davis after StackOverflow user <a href="http://stackoverflow.com/users/829571/assylias">assylias </a>
	 */
	public static String getFormattedTime(GregorianCalendar gc_in)
	{
		if(gc_in == null) return "";
		
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
		fmt.setCalendar(gc_in);
		return fmt.format(gc_in.getTime());
	}
	
	/**
	 * Utility Method - prints out a listing of all events currently in the model
	 */
	public void printEvents()
	{
		System.out.println("========CURRENT EVENTS========="); // har har
		int num = 0;
		for(CalendarEvent ce : events)
		{
			System.out.println(++num);
			System.out.println(ce.toString());
		}
	}
	
	private void readInEvents()
	{
		File file = new File("events.dat");
		if(file.exists())
		{
			try {
				ObjectInputStream  eventsFile_in = new ObjectInputStream(new
						FileInputStream(file));
				events = (TreeSet<CalendarEvent>) eventsFile_in.readObject();
				eventsFile_in.close();
				printEvents();
				notifyListeners();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else
		{System.out.println("The file \"events.dat\" does not exist.");}
		
		file = null;
		notifyListeners();
	}
	
	// data structures for model contents
	private static GregorianCalendar selectedDate = new GregorianCalendar();
	private static TreeSet<CalendarEvent> events = new TreeSet<CalendarEvent>(
			CalendarEvent.getComparator(CalendarEvent.Field.EVENT_START));
	
	// data structure for listeners collection
	private static final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void flushToDisk() {
		File file = new File("events.dat");
		
		ObjectOutputStream eventsFile_out;
		try {
			eventsFile_out = new ObjectOutputStream(new FileOutputStream(file));
			eventsFile_out.writeObject(events);
			eventsFile_out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file = null;
	}
}
