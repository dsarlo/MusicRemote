import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Client.ConnectToServerGUI;
import Server.ServerGUI;

public class Combine
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JButton client = new JButton("Client");
		client.setPreferredSize(new Dimension(200, 50));
		JButton server = new JButton("Server");
		server.setPreferredSize(new Dimension(200, 50));
		panel.add(client, BorderLayout.NORTH);
		panel.add(server, BorderLayout.SOUTH);
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		client.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new ConnectToServerGUI());
			}
			
		});
		
		server.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new ServerGUI());
			}
			
		});
		
	}
}
