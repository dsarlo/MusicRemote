package Server;
import javax.swing.SwingUtilities;

public class ServerDriver 
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new ServerGUI());
	}
}
