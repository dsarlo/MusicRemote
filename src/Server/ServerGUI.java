package Server;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ServerGUI implements Runnable 
{
	
	private JTextPane serverText;
	private JButton startServer;
	
	@Override
	public void run()
	{
		createGUI();
	}
	
	public void createGUI()
	{
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		
		startServer = new JButton("Start Server");
		startServer.setPreferredSize(new Dimension(600, 50));
		startServer.setFont(new Font("Calibri", 0, 24));
		
		serverText = new JTextPane();
		serverText.setPreferredSize(new Dimension(600, 500));
		serverText.setEditable(false);
		serverText.setFont(new Font("Calibri", 0, 24));
		
		JScrollPane scroll = new JScrollPane(serverText);
		scroll.setPreferredSize(new Dimension(600, 500));
		
		panel.setLayout(new BorderLayout());
		panel.add(startServer, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		frame.setLayout(new FlowLayout());
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		startServer.addActionListener(new ServerStartClickHandler(this));
	}
	
	public void appendText(String text)
	{
		serverText.setText(serverText.getText() + "\n" + text);
	}
	
	public void enableServerFalse()
	{
		startServer.setEnabled(false);
	}
}
