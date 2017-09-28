package Server;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

public class ServerStartClickHandler implements ActionListener {

	ServerGUI serverGui;
	private String path;
	
	public ServerStartClickHandler(ServerGUI serverGUI) {
		serverGui = serverGUI;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		runChooser();
	    serverGui.enableServerFalse();
		new Thread(new Server(this)).start();
	}
	public void runChooser()
	{
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new File("."));
	    chooser.setDialogTitle("Select Your Music Directory");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
	    { 
	    	setPath(chooser.getSelectedFile().toString());
	    }
	    else 
	    {
	    	runChooser();
	    }
	}

	public String getPath() 
	{
		return path;
	}

	public void setPath(String path) 
	{
		this.path = path;
	}
}
