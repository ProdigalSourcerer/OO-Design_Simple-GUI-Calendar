import javax.swing.JFrame;

public class SimpleCalendar {
	public static void main(String[] args)
	{
		CalendarAppFrame frame = new CalendarAppFrame(model);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private static final CalendarModel model = new CalendarModel();
}
