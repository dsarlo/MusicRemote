package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
//import java.util.Scanner;


public class Client implements Runnable
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String IP;
	//private Scanner scanner;
	private String songName;
	
	ClientGUI cGui;
	
	public Client(ClientGUI clientGUI) 
	{
		cGui = clientGUI;
	}

	@Override
	public void run()
	{
		try 
		{
			startClient();
		} 
		catch (IOException e) 
		{
			cGui.paneSetText("Failed to start client");
			e.printStackTrace();
		}
	}
	
	public void startClient() throws IOException
	{
		//scanner = new Scanner(System.in);
		//System.out.print("Type the server IP here: ");
		IP = cGui.handler.connect.getIPText();
		try {
			socket = new Socket(IP, 7777);
			cGui.getFrame().setVisible(true);
		} catch (IOException e) {
			ClientDriver.main(null);
			cGui.getFrame().dispose();
			//startClient();
			e.printStackTrace();
		}
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		cGui.paneSetText("Connected to Server " + IP);
		
		while(true)
		{
			out.writeInt(0);
			cGui.setText("");
			//System.out.print("\n\nType name of song here: ");
			//scanner.reset();
			//songName = scanner.nextLine();
			synchronized (cGui.holder) 
			{
		        // wait for input from field
		        while (cGui.holder.isEmpty())
		        {
		            try {
						cGui.holder.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }

		        String nextLine = cGui.holder.remove(0);
		        songName = nextLine;
		    }
			out.writeInt(1);
			out.writeUTF(songName.toLowerCase());
			String textFromServer = in.readUTF();
			if(textFromServer.equals("Server Closed!"))
			{
				socket.close();
				cGui.paneSetText(textFromServer);
				System.exit(0);
			}
			else
			{
				cGui.paneSetText(textFromServer);
			}
		}
	}
}
