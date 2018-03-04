package Client;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class ClientGUI implements Runnable
{
	private JFrame frame;
	private JTextField sendText;
	private JTextPane clientText;
	ClickAndConnectHandler handler;
	final List<String> holder = new LinkedList<String>();
	
	public ClientGUI(ClickAndConnectHandler connect)
	{
		handler = connect;
	}
	
	@Override
	public void run() {
		createGUI();
	}
	
	public void createGUI()
	{
		frame = new JFrame();
		JPanel panel = new JPanel();
		
		sendText = new JTextField();
		sendText.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				synchronized (holder) 
				{
	                holder.add(sendText.getText());
	                holder.notify();
	            }
			}
	    });
		sendText.setPreferredSize(new Dimension(600, 100));
		sendText.setText("");
		sendText.setFont(new Font("Calibri", 0, 24));
		
		clientText = new JTextPane();
		clientText.setPreferredSize(new Dimension(600, 500));
		clientText.setEditable(false);
		clientText.setFont(new Font("Calibri", 0, 24));
		
		JScrollPane scroll = new JScrollPane(clientText);
		scroll.setPreferredSize(new Dimension(600, 500));
		
		panel.setLayout(new BorderLayout());
		panel.add(sendText, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		frame.setLayout(new FlowLayout());
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		new Thread(new Client(this)).start();
	}
	
	public void setText(String text)
	{
		sendText.setText(text);
	}
	
	public String getText()
	{
		return sendText.getText();
	}
	
	public JFrame getFrame()
	{
		return frame;
	}
	
	public void paneSetText(String text)
	{
		clientText.setText("\n" + text);
	}
}
