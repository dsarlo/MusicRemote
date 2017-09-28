package Client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectToServerGUI implements Runnable
{
	private JFrame frame;
	private JTextField connectText;
	
	@Override
	public void run()
	{
		frame = new JFrame();
		JPanel panel = new JPanel();
		
		JButton connect = new JButton("Connect to Server");
		connect.setPreferredSize(new Dimension(400, 50));
		connect.setFont(new Font("Calibri", 0, 24));
		
		connectText = new JTextField();
		connectText.setPreferredSize(new Dimension(180, 50));
		connectText.setText("192.168.1.22");
		connectText.setFont(new Font("Calibri", 2, 24));
		connectText.setForeground(Color.LIGHT_GRAY);
		
		connectText.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				connectText.setText("");
				connectText.setFont(new Font("Calibri", 0, 24));
				connectText.setForeground(Color.BLACK);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		
		panel.setLayout(new BorderLayout());
		panel.add(connect, BorderLayout.NORTH);
		panel.add(connectText, BorderLayout.SOUTH);
		frame.setLayout(new FlowLayout());
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		connect.addActionListener(new ClickAndConnectHandler(this));
	}

	public JFrame get_frame() 
	{
		return frame;
	}
	
	public void setText(String text)
	{
		connectText.setText(text);
	}
	
	public String getIPText()
	{
		return connectText.getText();
	}
}
