package network;

import javax.swing.JOptionPane;

import controllers.ServerController;

public class Bootstrap {
	
	private static ServerController controller = ServerController.getInstance();
	
	public static void main (String[] args) {
		String groupAddress = JOptionPane.showInputDialog("Informe o endereço do grupo. (Default: 224.7.35.9)");
		String groupPort = JOptionPane.showInputDialog("Informe a porta do grupo. (Default: 5656)");
		Server server = new Server(groupAddress, groupPort);
		new Thread(server).start();
		
		controller.setServer(server);
		controller.sendJoinMessage();
	}
}
