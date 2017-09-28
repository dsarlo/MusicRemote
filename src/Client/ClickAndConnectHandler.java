package Client;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

import javax.swing.SwingUtilities;

public class ClickAndConnectHandler implements ActionListener 
{
	ConnectToServerGUI connect;
	Socket socket;
	public ClickAndConnectHandler(ConnectToServerGUI connectToServerGUI) 
	{
		connect = connectToServerGUI;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		SwingUtilities.invokeLater(new ClientGUI(this));
		connect.get_frame().setVisible(false);
	}
}
