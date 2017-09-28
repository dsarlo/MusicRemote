package Client;
import javax.swing.SwingUtilities;

public class ClientDriver 
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new ConnectToServerGUI());
	}
}
