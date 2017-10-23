import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class contains the CalendarAppFrame class as well as separate (nested) classes for 
 * task-specific panels and relevant dialogs.
 * 
 * @author Iain Davis
 * @version 1.0
 */
public class CalendarAppFrame extends JFrame implements ChangeListener{
	CalendarAppFrame(CalendarModel model_in)
	{
		model = model_in;
		
		// attach model to views
		calendarPanel = new CalendarPanel(model);
		schedulePanel = new SchedulePanel(model);
		
		// attach views to model
		model.attachListener(calendarPanel);
		model.attachListener(schedulePanel);
		model.attachListener(this);
		
		// make a reference to (this) top-level frame accessible to nested classes
		frame = this;
		
		// update the title bar of this frame with the date currently selected in the model
		updateTitle();
		
		// instantiate and initialize custom dialog for creating new events
		ned = new CreateEventDialog();
		
		// layout top-level panel
		layoutFrame();
		

	} // constructor
	
	/**
	 * Utility method - encapsulates the instructions for laying out the
	 * 		components of the frame - primarily to enhance readability of 
	 * 		constructor and organize panel-initialization steps into categories
	 */
	private void layoutFrame()
	{
		add(navPanel, BorderLayout.NORTH);
		add(calendarPanel, BorderLayout.WEST);
		add(schedulePanel, BorderLayout.CENTER);
	}
	
	/**
	 * Utility method - method listens to the model to ensure the title bar always represents
	 * 						the currently selected date.
	 */
	@Override public void stateChanged(ChangeEvent e) {updateTitle();}
	
	/**
	 * Utility method - updates the frame's title bar with the currently selected date
	 */
	public void updateTitle()
	{setTitle(DAYS.values()[model.getSelectedDate().get(Calendar.DAY_OF_WEEK) - 1] + " - " +  model.getFormattedDate());}
	
	
	// fields
	private static CreateEventDialog ned;
	private static final NavPanel navPanel = new NavPanel();
	
	protected static CalendarModel model;	
	private static CalendarPanel calendarPanel; 	// indirect Observer
	private static SchedulePanel schedulePanel;		// indirect Observer
	
	private static CalendarAppFrame frame; // direct observer - reference visible to nested classes
	
	// enumerated types visible to all nested classes
	private enum DAYS    {Sunday("SU"), Monday("MO"), Tuesday("TU"), Wednesday("WE"), Thursday("TH"), Friday("FR"), Saturday("SA");
							private DAYS(String abbrev_in)
							{abbrev = abbrev_in;};
							
							public String getAbbreviation()
							{return abbrev;}
							
							public static String[] getAbbreviations()
							{return abbrevs;}
							
							public final String abbrev;
							public static final String[] abbrevs = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
						}
	private enum MONTHS  {January, Feburary, March, April, May, June, July, August, September, October, November, December};
	private enum SEGMENTS {AM, PM};
	
	/**
	 * This JPanel subclass encapsulates the top-most bar of the CalendarAppFrame containing the
	 * 		navigation buttons and the quit button
	 * @author Iain Davis
	 * @version 1.0
	 */
	private static class NavPanel extends JPanel
	{
		/**
		 * Constructor - builds the navigation panel
		 */
		private NavPanel()
		{	
			layoutNavPanel();
			initNavPanelComponents();
		}
		
		/**
		 * Utility method - encapsulates the instructions for laying out the
		 * 		components of this panel - primarily to enhance readability of 
		 * 		constructor and organize panel-initialization steps into categories
		 */
		private void layoutNavPanel()
		{
			add(leftPanel);
			add(rightPanel);
			
			leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			leftPanel.add(prevYear_btn);
			navButtons.add(prevYear_btn);
			
			leftPanel.add(prevMonth_btn);
			navButtons.add(prevMonth_btn);
			
			leftPanel.add(prevDay_btn);
			navButtons.add(prevDay_btn);
			
			leftPanel.add(today_btn);
			
			leftPanel.add(nextDay_btn);
			navButtons.add(nextDay_btn);
			
			leftPanel.add(nextMonth_btn);
			navButtons.add(nextMonth_btn);
			
			leftPanel.add(nextYear_btn);
			navButtons.add(nextYear_btn);
			
			
			rightPanel.add(quit_btn);
		}
		
		/**
		 * Utility method - encapsulates the methods that specify behavior of the
		 * 		components on this panel - primarily to enhance readability of
		 * 		constructor and organize panel-initialization steps into categories
		 */
		private void initNavPanelComponents()
		{
			// all navButtons (except today_btn) share the same listener
			for(NavButton nb : navButtons)
			{
				nb.addMouseListener(new
						MouseAdapter()
						{@Override
							public void mouseClicked(MouseEvent e){model.goTo(nb.calendarField, nb.value);}
						});
			}
			
			// unique listener for today_btn causes model to instantiate new GregorianCalendar
			today_btn.addMouseListener( new
					MouseAdapter()
					{@Override
						public void mouseClicked(MouseEvent e)
						{
							model.goToToday();
							System.out.println(model.getFormattedDate());
						}});
			
			// unique listener for quit button
			quit_btn.addMouseListener(new 
					MouseAdapter()
					{@Override
						public void mouseClicked(MouseEvent e) 
						{
							model.flushToDisk();
							frame.dispose();
						}});
			
			// initialize toolTip text
			prevYear_btn.setToolTipText("Go back one year");
			prevMonth_btn.setToolTipText("Go back one month");
			prevDay_btn.setToolTipText("Go back one day");
			today_btn.setToolTipText("Go to today's date");
			nextDay_btn.setToolTipText("Go forward one day");
			nextMonth_btn.setToolTipText("Go forward one month");
			nextYear_btn.setToolTipText("Go forward one year");
			
			quit_btn.setToolTipText("Save events and exit the program");
		}
		
		// fields
		private final static NavButton prevYear_btn	 = new NavButton("<<<", Calendar.YEAR, -1);
		private final static NavButton prevMonth_btn = new NavButton("<<", Calendar.MONTH, -1);
		private final static NavButton prevDay_btn   = new NavButton("<", Calendar.DAY_OF_MONTH, -1);
		private final static JButton   today_btn     = new JButton("Today");
		private final static NavButton nextDay_btn   = new NavButton(">", Calendar.DAY_OF_MONTH, 1);
		private final static NavButton nextMonth_btn = new NavButton(">>", Calendar.MONTH, 1);
		private final static NavButton nextYear_btn  = new NavButton(">>>", Calendar.YEAR, 1);
		private final static JButton quit_btn        = new JButton("Quit");
		
		private final static JPanel leftPanel = new JPanel();
		private final static JPanel rightPanel = new JPanel();
		
		private final static ArrayList<NavButton> navButtons = new ArrayList<NavButton>();
	}
	
	/** 
	 * This JPanel subclass encapsulates the layout and behavior of the lower-left panel of
	 * 		the CalendarAppFrame featuring the month-view of the currently selected date
	 * 		and the button for creating new events.
	 * @author Iain Davis
	 */
	private static class CalendarPanel extends JPanel implements ChangeListener
	{
		/**
		 * Default constructor
		 * @param model 
		 */
		private CalendarPanel(CalendarModel model_in)
		{
			model = model_in;
			
			layoutCalendarPanel();
			initCalendarPanelComponents();
			
			// initialize components with starting values
			displayDate(model.getSelectedDate());
		}
		
		/**
		 * Utility method - performs the layout of CalendarPanel components - mostly
		 * 						for readability of constructor and segregation of different
		 * 						categories of setup actions
		 */
		private void layoutCalendarPanel()
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(buttonPanel);
			initDaysGrid();
			gridPanel.add(daysGrid);
			add(gridPanel);
			buttonPanel.add(create_btn);
		}
		
		/**
		 * Utility method - performs initial behavior and appearance setup of 
		 * 						CalendarPanel components - mostly for readability of
		 * 						constructor and segregation of different categories of
		 * 						setup actions
		 */
		private void initCalendarPanelComponents()
		{
			create_btn.setBackground(buttonColor);
			create_btn.setForeground(Color.WHITE);
			create_btn.setBorderPainted(false);
			
			create_btn.addActionListener(new 
					ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e) {
							ned.showNewEventDialog();
						}
					});
		}
		
		/**
		 * Utility method - performs initial setup of the actual calendar display panel.
		 * 						Contains both layout and behavior setup actions to avoid
		 * 						unnecessary loop-duplication
		 */
		private void initDaysGrid()
		{
			// create row of headers
			for(int i = 0; i < 7; i++)
			{
				dayLabels[0][i] = new JLabel();
				dayLabels[0][i].setHorizontalAlignment(JLabel.CENTER);
				dayLabels[0][i].setVerticalAlignment(JLabel.BOTTOM);
				dayLabels[0][i].setPreferredSize(new Dimension(40, 30));
				dayLabels[0][i].setText(DAYS.getAbbreviations()[i]);
				dayLabels[0][i].setOpaque(true);
				daysGrid.add(dayLabels[0][i]);
			}
			
			// create and perform default formatting of calendar grid
			for(int week = 1; week < 7; week++)
			{
				for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++)
				{
					dayLabels[week][dayOfWeek] = new JLabel();
					dayLabels[week][dayOfWeek].setPreferredSize(new Dimension(40, 40));
					dayLabels[week][dayOfWeek].setHorizontalAlignment(JLabel.RIGHT);
					dayLabels[week][dayOfWeek].setVerticalAlignment(JLabel.TOP);
					dayLabels[week][dayOfWeek].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
					dayLabels[week][dayOfWeek].setOpaque(true);
					daysGrid.add(dayLabels[week][dayOfWeek]);
					
					// initialize dayLabel click behavior
					dayLabels[week][dayOfWeek].addMouseListener(new 
							MouseAdapter()
							{
								@Override 
								public void mouseClicked(MouseEvent e)
								{
									if(((JLabel)e.getSource()).isEnabled())
									{
										int year = model.getSelectedDate().get(Calendar.YEAR);
										int month = model.getSelectedDate().get(Calendar.MONTH);
										int day = Integer.parseInt(((JLabel)e.getSource()).getText());
										model.goToDate(year, month, day);
									}}});
				}
			}
		} // initDaysGrid()
		
		/**
		 * Utility method - update calendar grid to represent the month and year in which 
		 * 						the client-provided date is found, including formatting 
		 * 						identifying:
		 * 							- days in the current month
		 * 							- days before or after the current month
		 * 							- highlights on days with events
		 * 							- an identifying marker on the currently selected date
		 * @param targetDate	a GregorianCalendar instance representing the client-supplied date
		 * @precondition		targetDate is non-null
		 */
		private void displayDate(GregorianCalendar targetDate)
		{
			// handle precondition
			if(targetDate == null) return;
			
			// update calendar header
			daysGrid.setBorder(BorderFactory.createTitledBorder(
					MONTHS.values()[targetDate.get(Calendar.MONTH)] 
					+ " " + targetDate.get(Calendar.YEAR)));
			
			// get a copy of the date, set it to the first day of the month. If that day is
			// not already a Sunday, back up to the prior Sunday (a day in the previous month)
			GregorianCalendar CalendarWalker = (GregorianCalendar) targetDate.clone();
			CalendarWalker.set(Calendar.DAY_OF_MONTH, 1);
			
			while(CalendarWalker.get(Calendar.DAY_OF_WEEK) != 1) 
				CalendarWalker.add(Calendar.DAY_OF_MONTH, -1);
			

			// step through each day present on the counter and format it accordingly	
			for(int row = 1; row < 7; row++)
			{
				for(int col = 0; col < 7; col++)
				{	
					// days before or after end of month
					if(CalendarWalker.get(Calendar.MONTH) != targetDate.get(Calendar.MONTH))
					{
						dayLabels[row][col].setForeground(Color.GRAY);
						dayLabels[row][col].setBackground(Color.LIGHT_GRAY);
						dayLabels[row][col].setBorder(BorderFactory.createLineBorder(Color.GRAY));
						dayLabels[row][col].setFont(new Font("ARIAL", Font.PLAIN, 18));
						dayLabels[row][col].setEnabled(false);
						
						if(model.hasEvents(CalendarWalker))
						{dayLabels[row][col].setBackground(new Color(0x94AAAA));}
					}
					// days during the current month that are not also the current selected date
					else if (CalendarWalker.get(Calendar.DAY_OF_MONTH) == targetDate.get(Calendar.DAY_OF_MONTH))
					{
						dayLabels[row][col].setForeground(Color.RED);
						dayLabels[row][col].setBackground(Color.WHITE);
						dayLabels[row][col].setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
						dayLabels[row][col].setFont(new Font("ARIAL", Font.BOLD, 18));
						dayLabels[row][col].setEnabled(true);
						if(model.hasEvents(CalendarWalker))
						{dayLabels[row][col].setBackground(new Color(0xC8F0EF));}
					}
					else // current selected date
					{
						dayLabels[row][col].setForeground(Color.BLACK);
						dayLabels[row][col].setBackground(Color.WHITE);
						dayLabels[row][col].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
						dayLabels[row][col].setFont(new Font("ARIAL", Font.PLAIN, 18));
						dayLabels[row][col].setEnabled(true);
						if(model.hasEvents(CalendarWalker))
						{dayLabels[row][col].setBackground(new Color(0xC8F0EF));}
					}
					
					// apply numbering of current day
					dayLabels[row][col].setText(Integer.toString(CalendarWalker.get(Calendar.DAY_OF_MONTH)));
					// increment walker in preparation of processing the next day
					CalendarWalker.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
		}
		
		/**
		 * Utility method - prompts update of daysGrid with the currently selected date
		 * 						from the model
		 */
		@Override
		public void stateChanged(ChangeEvent d) {
			displayDate(model.getSelectedDate());
		} // stateChanged()
		
		// component fields
		private final static JButton    create_btn = new JButton("Create");
		private final static JLabel[][] dayLabels = new JLabel[7][7];
		private final static JPanel     buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		private final static JPanel     daysGrid  = new JPanel(new GridLayout(7,7));
		private final static JPanel		gridPanel = new JPanel();
		
		// other fields
		private final static Color      buttonColor = new Color(0xED, 0x6A, 0x5A);
		private static CalendarModel model;
	}
	
	/**
	 * This JPanel subclass encapsulates the layout and behavior of the lower-right panel of
	 * 		the CalendarAppFrame, featuring the daily-schedule view of the currently selected
	 * 		date
	 * @author Iain Davis
	 *
	 */
	private class SchedulePanel extends JPanel implements ChangeListener
	{
		/**
		 * Default constructor
		 * @param model_in		the model that will issue events prompting this panel to update
		 */
		private SchedulePanel(CalendarModel model_in)
		{
			model = model_in;
			layoutSchedulePanel();
			updateHeader();
			loadScheduleEvents();
		}
		
		/**
		 * Utility method - lays out and formats components for this panel
		 */
		private void layoutSchedulePanel()
		{

			setLayout(new BorderLayout());
			add(headerPanel, BorderLayout.NORTH);
			
			headerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			headerPanel.add(header);
			
			//scrollPanel.add(scheduleScroller);
			scrollPanel.add(scheduleLayers);
			
			add(scheduleScroller, BorderLayout.CENTER);
			
			scheduleScroller.setMinimumSize(new Dimension(618, 300));
			scheduleScroller.setPreferredSize(new Dimension(618, 300));
			scheduleScroller.setMaximumSize(new Dimension(618, 960));
			
			scheduleLayers.setLayout(null);
			scheduleLayers.setPreferredSize(new Dimension(600, 960));
			
			refreshSchedulePage();
		}
		
		private void refreshSchedulePage()
		{
			scheduleLayers.removeAll();
			JTextArea leftBlock;
			JTextArea rightBlock;
			StringBuffer sb;
			final int ROW_HEIGHT = 20;
			final int LEFT_COL_WIDTH = 50;
			final int RIGHT_COL_WIDTH = 550;
			for(int i = 0; i < 48; i++)
			{
				if(i % 2 == 0)
				{
					sb = new StringBuffer();
					
					int hr = (i/2) % 12;
					if(hr == 0) hr = 12;
					boolean am = i / 24 == 0;
					
					sb.append(("" + hr + (am ? "am" : "pm")));
					leftBlock = new JTextArea();
					leftBlock.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
					leftBlock.setText(sb.toString());
					leftBlock.setBounds(0, i * ROW_HEIGHT, LEFT_COL_WIDTH, ROW_HEIGHT * 2);
					scheduleLayers.add(leftBlock, PAGE_LAYER);
					
				}
				rightBlock = new JTextArea();
				rightBlock.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				rightBlock.setBounds(LEFT_COL_WIDTH, i * ROW_HEIGHT, RIGHT_COL_WIDTH, ROW_HEIGHT);
				scheduleLayers.add(rightBlock, PAGE_LAYER);
			}
		}
		
		/**
		 * Updates the header at the top of the schedule view with the model's 
		 * 		currently selected date
		 */
		private void updateHeader()
		{	
			GregorianCalendar date = model.getSelectedDate();
			String dayOfWeek = DAYS.values()[date.get(Calendar.DAY_OF_WEEK) - 1].toString();
			int month = date.get(Calendar.MONTH) + 1;
			int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
			header.setText(dayOfWeek + " " + month + "/" + dayOfMonth);
		}
		
		private void loadScheduleEvents()
		{
			// TODO finish this method
			refreshSchedulePage();
			
			TreeSet<CalendarEvent> daysEvents = 
					(TreeSet<CalendarEvent>) model.getEvents(model.getSelectedDate());
			
			for(CalendarEvent ce : daysEvents)
			{
				EventMarker em = new EventMarker(ce);

				scheduleLayers.add(em, EVENTS_LAYER);
			}
			

			scheduleLayers.scrollRectToVisible(new Rectangle(0,200,10,10));
		}
		
		/**
		 * Listener method - receives notifications from model
		 */
		@Override
		public void stateChanged(ChangeEvent e) 
		{
			updateHeader();
			loadScheduleEvents();
		}
		
		// Component fields
		private final JLabel header = new JLabel();
				
		private final JPanel headerPanel = new JPanel();

		private final JLayeredPane scheduleLayers = new JLayeredPane();
		private final JPanel		scrollPanel = new JPanel();
		private final JScrollPane scheduleScroller = new JScrollPane(scrollPanel);
		
		// other fields
		private CalendarModel model;
		final int PAGE_LAYER = 1;
		final int EVENTS_LAYER = 0;
	}
	
	/**
	 * This JDialog subclass encapsulates the layout and behavior of a user-input dialog that
	 * 		captures the required information to create a new event and add it to the model.
	 * @author Iain Davis
	 */
	private static class CreateEventDialog extends JDialog
	{
		/**
		 * Constructor
		 */
		private CreateEventDialog()
		{
			// make sure this dialog pops up located relative to the CalendarAppFrame window
			// also force the user to close this dialog before interacting with CalendarAppFrame further
			super(frame, true);
			
			collectSpinners();
			
			layoutDialogComponents();
			initDialogComponentValues();
			initDialogComponentBehavior();

			
			// attach the components to this dialog
			setContentPane(new JOptionPane
					(topLevelPanel, 
							JOptionPane.PLAIN_MESSAGE, 
							JOptionPane.DEFAULT_OPTION, 
							null, options, options[0]));
			
			// verify that current values can be used to create a valid CalendarEvent instance
			// with no conflicts in the model (if not, this series of methods will update
			// various flags, the status message, and disable the Save button
			validateTitle();
			validateSpinners();
			checkForConflicts();
			
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	        pack();
	        
		} // constructor
		
		/**
		 * Utility method - handles the layout of components on the NewEventDialog instance
		 * 						primarily for readability of constructor and segregation of 
		 * 						different categories of initialization behavior
		 */
		private void layoutDialogComponents()
		{
			topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.Y_AXIS));
			eventTitlePanel.setLayout(new BoxLayout(eventTitlePanel, BoxLayout.X_AXIS));
						
			eventTitlePanel.add(eventTitle_lbl);
			eventTitlePanel.add(eventTitle_txt);
			
			eventDatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			eventDatePanel.add(eventMonth_spn);
			eventDatePanel.add(eventDay_spn);
			eventDatePanel.add(eventYear_spn);
			
			eventMonth_spn.setPreferredSize(new Dimension(80, 20));
			eventDay_spn.setPreferredSize(new Dimension(35, 20));
			eventYear_spn.setPreferredSize(new Dimension(50, 20));
			
			
			eventOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			eventStartPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			eventStartPanel.add(startHour_spn);
			eventStartPanel.add(startMinute_spn);
			eventStartPanel.add(startSegment_spn);
			
			eventEndPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			eventEndPanel.add(endHour_spn);
			eventEndPanel.add(endMinute_spn);
			eventEndPanel.add(endSegment_spn);
			
			eventOptionsPanel.add(eventDatePanel);
			eventOptionsPanel.add(eventStartPanel);
			eventOptionsPanel.add(eventEndPanel);
			
			topLevelPanel.add(eventTitlePanel);
			topLevelPanel.add(eventOptionsPanel);
			topLevelPanel.add(status_txt);
		} // layoutDialogComponents()
		
		/**
		 * Utility method - handles initialization of values on the NewEventDialog instance
		 * 						during construction or resetting to suitable values when
		 * 						showing the dialog
		 * 						Primarily for readability of constructor and methods, and
		 * 						compartmentalization of different categories of initialization
		 * 						behavior
		 */
		private void initDialogComponentValues()
		{
			GregorianCalendar selectedDate = model.getSelectedDate();
			
			setTitle("Create New Event");
			eventTitle_txt.setText("Untitled Event");
			topLevelPanel.setBorder(BorderFactory.createTitledBorder("Create a new event:"));
			eventDatePanel.setBorder(BorderFactory.createTitledBorder("date"));
			eventStartPanel.setBorder(BorderFactory.createTitledBorder("start"));
			eventEndPanel.setBorder(BorderFactory.createTitledBorder("end"));
			
			eventMonth_spn.setValue(MONTHS.values()[selectedDate.get(Calendar.MONTH)]);
			
			eventDay_spn.setModel(new SpinnerNumberModel(
					selectedDate.get(Calendar.DAY_OF_MONTH),
					1,
					selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH),
					1));

			// NumberEditor Object removes the thousands separator from the year spinner
			JSpinner.NumberEditor year_edt = new JSpinner.NumberEditor(eventYear_spn, "#"); 
			eventYear_spn.setEditor(year_edt);
			eventYear_spn.setValue(selectedDate.get(Calendar.YEAR));
			
			startHour_spn.setValue(0);
			startMinute_spn.setValue(0);
			startSegment_spn.setValue(SEGMENTS.AM);
			
			endHour_spn.setValue(0);
			endMinute_spn.setValue(0);
			endSegment_spn.setValue(SEGMENTS.AM);
			
			status_txt.setForeground(Color.RED);
			Font currentFont = status_txt.getFont();
			status_txt.setFont(new 
					Font(currentFont.getFontName(), 
							Font.BOLD, 
							currentFont.getSize()));
			status_txt.setOpaque(false);
			status_txt.setText("");
		}
		
		/**
		 * Utility method - handles initialization of behavior of NewEventDialog components
		 */
		private void initDialogComponentBehavior()
		{
			// creates a new CalendarEvent and saves it (to model)
			save_btn.addActionListener(new 
					ActionListener()
					{@Override
						public void actionPerformed(ActionEvent e) {
							model.addEvent(new 
									CalendarEvent((GregorianCalendar)eventStart.clone(), 
												  (GregorianCalendar)eventEnd.clone(), 
												  eventTitle_txt.getText()));
							// TODO remove debugging statement
							model.printEvents();
							dispose();
						}});
			
			// prompts model to save events (to disk) and exits program
			cancel_btn.addActionListener(new 
					ActionListener()
					{@Override
						public void actionPerformed(ActionEvent e) {
							dispose();
						}});
			
			for(JSpinner js : spinners)
			{
				// whenever a spinner is changed, confirms the new dates and times are still
				//		valid or displays an error message and disables the save button
				js.addChangeListener(new
						ChangeListener()
						{@Override
							public void stateChanged(ChangeEvent arg0) {
								validateSpinners();
								checkForConflicts();								
								updateStatus();
							}});
			} // for(spinners)
			
			// whenever the title changes, confirms the change results in a valid title
			//		or displays an error message and disables the save key
			eventTitle_txt.addKeyListener(new 
					KeyAdapter()
					{@Override
						public void keyReleased(KeyEvent e) {
							validateTitle();							
						}});
		}

		/**
		 * Utility method - Group all JSpinners in a collection to facilitate assigning 
		 * 						the same ChangeListener to all of them - purely for
		 * 						convenience and brevity of constructor
		 */
		private void collectSpinners()
		{
			spinners.add(eventMonth_spn);
			spinners.add(eventDay_spn);
			spinners.add(eventYear_spn);
			spinners.add(startHour_spn);
			spinners.add(startMinute_spn);
			spinners.add(startSegment_spn);
			spinners.add(endHour_spn);
			spinners.add(endMinute_spn);
			spinners.add(endSegment_spn);
		}
		
		/**
		 * Utility method - Collects all values from spinners, packages them into 
		 * 						GregorianCalendar instances with the specified values,
		 * 						and confirms they represent valid inputs for a CalendarEvent
		 * 						constructor (e.g., start time is not after end time).
		 * 		
		 * 					Also sets a boolean field that remembers the result - saves a 
		 * 						change in the text input field prompting an unnecessary 
		 * 						validation of all spinners
		 * 
		 * @return		true if eventSpinners represent valid start and end times
		 * 				false otherwise
		 */
		private boolean validateSpinners()
		{
			// CalendarApp assumes start and end times occur on the same day
			Integer year = (Integer) eventYear_spn.getValue();
			Integer month = ((MONTHS) eventMonth_spn.getValue()).ordinal();
			Integer day = (Integer) eventDay_spn.getValue();
			
			String segment = startSegment_spn.getValue().toString();
			Integer hour = (Integer) startHour_spn.getValue();
			hour = segment.equals("AM") ? hour : hour + 12;
			Integer minute = (Integer) startMinute_spn.getValue();
												
			eventStart.set(year, month, day, hour, minute);
			
			segment = endSegment_spn.getValue().toString();
			hour = (Integer) endHour_spn.getValue();
			hour = segment.equals("AM") ? hour : hour + 12;
			minute = (Integer) endMinute_spn.getValue();
			
			eventEnd.set(year,  month, day, hour, minute);
			
			// perform actual validation
			if(eventStart.after(eventEnd))
			{
				errorMessages.add("Event end cannot be after event start.");
				validSpinners = false;
				updateStatus();
				return false;
			}
			validSpinners = true;
			updateStatus();
			return true;
		}
		
		/**
		 * Utility method - Verifies that eventTitle_txt.getText() is neither an empty string, nor an all-
		 * 						whitespace String.
		 * 
		 * Also sets a boolean field that remembers the result - saves a change in the 
		 * 		spinners field prompting an unnecessary validation of a text field
		 * 
		 * @return		true if eventTitle_txt.getText().trim() returns a non-empty String
		 * 				false otherwise
		 */
		private boolean validateTitle()
		{
			if (eventTitle_txt.getText().trim().equals(""))
			{
				errorMessages.add("This event must have a title.");
				validTitle = false;
				updateStatus();
				return false;
			}
			validTitle = true;
			updateStatus();
			return true;
		} // validateTitle()
		
		/**
		 * Utility method - Constructs a scratch CalendarEvent using the current values 
		 * 						and checks the model for scheduling conflicts.
		 * @return			true if model contains conflicting events
		 * 						false otherwise
		 */
		private boolean checkForConflicts()
		{
			noConflicts = false;
			scratch = new CalendarEvent(eventStart, eventEnd, null);
			if(model.hasConflictingEvent(scratch))
			{
				errorMessages.add("This event conflicts with an existing event in the calendar.");
				noConflicts = false;
				updateStatus();
				return false;
			}
			noConflicts = true;
			updateStatus();
			return true;
		} // checkForConflicts()
		
		/**
		 * Utility method - to be called after every validation - checks boolean status
		 * 						variables, updates status message, and controls save button
		 * 						enabled state
		 */
		private void updateStatus()
		{
			StringBuffer sb = new StringBuffer();
			if(validTitle && validSpinners && noConflicts)
			{
				save_btn.setEnabled(true);
			}
			else
			{
				if(!validTitle) sb.append("This event requires a title\n");
				if(!validSpinners) sb.append("Start time must not be after end time\n");
				if(!noConflicts) sb.append("There is a conflicting event in the calendar already.");
				save_btn.setEnabled(false);
			}
			status_txt.setText(sb.toString());
		}
		
		/**
		 * Utility method - show the NewEventDialog and give access to it's functionality
		 * 						also re-initializes all values
		 */
		private void showNewEventDialog()
		{
			initDialogComponentValues();
			validateTitle();
			validateSpinners();
			checkForConflicts();
			
			setVisible(true);
		};
 
		// Component fields
		private static final JLabel eventTitle_lbl = new JLabel("Title:  ");
		private static final JTextField eventTitle_txt = new JTextField("Untitled Event", 35);
		private static final JTextArea status_txt = new JTextArea(3, 50);
				
		private static final JSpinner eventMonth_spn = new JSpinner(new SpinnerListModel(MONTHS.values()));
		private static final JSpinner eventDay_spn = new JSpinner();
		private static final JSpinner eventYear_spn = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		private static final JSpinner startHour_spn = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		private static final JSpinner startMinute_spn = new JSpinner(new SpinnerNumberModel(0, 0, 45, 15));
		private static final JSpinner startSegment_spn = new JSpinner(new SpinnerListModel(SEGMENTS.values()));
		private static final JSpinner endHour_spn = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		private static final JSpinner endMinute_spn = new JSpinner(new SpinnerNumberModel(0, 0, 45, 15));
		private static final JSpinner endSegment_spn = new JSpinner(new SpinnerListModel(SEGMENTS.values()));

		private final ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
		
		private static final JPanel topLevelPanel = new JPanel();
		private static final JPanel eventTitlePanel = new JPanel();
		private static final JPanel eventOptionsPanel = new JPanel();
		private static final JPanel eventDatePanel = new JPanel();
		private static final JPanel eventStartPanel = new JPanel();
		private static final JPanel eventEndPanel = new JPanel();
		
		private static final JButton save_btn = new JButton("Save");
		private static final JButton cancel_btn = new JButton("Cancel");
		
		private static final JButton[] options = {save_btn, cancel_btn};
		
		// other fields
		private CalendarEvent scratch;
		
		private static final GregorianCalendar eventStart = new GregorianCalendar();
		private static final GregorianCalendar eventEnd = new GregorianCalendar();
		
		private boolean validSpinners = true;
		private boolean validTitle = true;
		private boolean noConflicts = true;
		
		private final ArrayList<String> errorMessages = new ArrayList<String>();
		
	}

	/**
	 * This JButton subclass incorporates two additional fields
	 * 		allowing all the stepwise navigation functions 
	 * 		(increment day, increment month, increment year) etc.
	 * 		to use the same ActionListener
	 * @author Iain
	 *
	 */
	private static class NavButton extends JButton
	{
		/**
		 * Default constructor
		 * @param buttonText		the text for this particular button
		 * @param calendarFieldIn	The Calendar field to alter
		 * @param valueIn			the amount by which to increment it (to decrement,
		 * 								set this to a negative value)
		 * 
		 * Note: the SimpleCalendar assumes navigation is always by 1 unit at a time, but any
		 * 			value could be used for another application
		 */
		private NavButton(String buttonText, int calendarFieldIn, int valueIn)
		{
			super(buttonText);
			calendarField = calendarFieldIn;
			value = valueIn;
		} // constructor
		
		/**
		 * Accessor method - return the magnitude of a single unit of navigation for this button
		 * @return 		an integer from the set of Calendar.FIELD options;
		 */
		int getCalendarField()
		{return calendarField;}
		
		/**
		 * Accessor method
		 * @return		the quantity and direction of navigation units of the increment 
		 * 					pressing this button should initiate
		 */
		int getValue()
		{return value;}
		
		// fields
		private int calendarField;
		private int value;
	}

	private class EventMarker extends JLabel
	{
		private EventMarker(CalendarEvent event_in)
		{ 
			//super();
			event = event_in;
			setBackground(new Color(0x88D3C5));
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			sb = new StringBuffer();
			
			sb.append(CalendarModel.getFormattedTime(event.getStart()));
			sb.append(" - " + CalendarModel.getFormattedTime(event.getEnd()));
			sb.append(":  " + event.getTitle());
			setText(sb.toString());
			
			setToolTipText(sb.toString());
			
			setOpaque(true);
			
			double start_h = event.getStart().get(Calendar.HOUR_OF_DAY);
			double start_m = event.getStart().get(Calendar.MINUTE);
			double start_y = 2.0/3.0 * (start_h*60 + start_m); 
			
			double end_h = event.getEnd().get(Calendar.HOUR_OF_DAY);
			double end_m = event.getEnd().get(Calendar.MINUTE);
			double end_y = 2.0/3.0 * (end_h*60 + end_m);
			
			double height = end_y - start_y;
			if(Math.abs(height) <= 0.00001)
			{
				setText("");
				height = 6.0;
				
				if(event.getStart().get(Calendar.HOUR_OF_DAY) != 0)
				{
					start_y -= 3;
				}
			}
			
			setBounds(50, (int) start_y, 550, (int) height);
			
			this.addMouseListener(new
					MouseAdapter()
					{
						@Override
						public void mouseClicked(MouseEvent e)
						{
							final int DELETE = 0;
							final int OKAY = 1;
							System.out.println("====EVENT CLICKED====");
							System.out.println(event.toString());
							
							int result = JOptionPane.showOptionDialog(
												frame, 
												event.toString(), 
												event.getTitle(), 
												JOptionPane.DEFAULT_OPTION, 
												JOptionPane.PLAIN_MESSAGE, 
												null, 
												new String[] {"Delete", "OK"}, 
												"OK");
							
							if(result == DELETE)
							{
								model.deleteEvent(event);
							}
						}
						
						private final JButton delete_btn = new JButton();
						private final JButton ok_btn = new JButton();
					});
		}
		
		private StringBuffer sb;
		private CalendarEvent event;
	}
}
