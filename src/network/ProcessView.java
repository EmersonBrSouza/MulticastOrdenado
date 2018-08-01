package network;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controllers.ServerController;

import javax.swing.JTextPane;
import java.awt.SystemColor;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProcessView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public ProcessView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 252, 168);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setResizable(false);
		setContentPane(contentPane);
		
		JButton button = new JButton("1");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerController.getInstance().sendRandomAction(1);
			}
		});
		button.setBounds(10, 58, 65, 57);
		contentPane.add(button);
		
		JButton btnNewButton = new JButton("2");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerController.getInstance().sendRandomAction(2);
			}
		});
		btnNewButton.setBounds(85, 58, 65, 57);
		contentPane.add(btnNewButton);
		
		JButton button_1 = new JButton("3");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerController.getInstance().sendRandomAction(3);
			}
		});
		button_1.setBounds(160, 58, 65, 57);
		contentPane.add(button_1);
		
		JTextPane txtpnEventos = new JTextPane();
		txtpnEventos.setEditable(false);
		txtpnEventos.setBackground(SystemColor.control);
		txtpnEventos.setText("Disparar Eventos");
		txtpnEventos.setBounds(10, 27, 108, 20);
		contentPane.add(txtpnEventos);
		setVisible(true);
	}
}
