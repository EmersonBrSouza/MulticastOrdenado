package network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

import controllers.ServerController;
import models.Message;

public class ProtocolProcessor implements Runnable{
	
	private Object receivedMessage;
	
	public ProtocolProcessor (DatagramPacket packet) {
		this.setReceivedMessage(this.deserialize(packet.getData()));
	}
	
	@Override
	public void run() {
		if (!this.isValidMessage()) { System.err.println("Corrupted message!"); return; }
		
		
		Message message = (Message) this.getReceivedMessage();
		if (this.isSelfMessage(message)) return;
		
		switch(message.getProtocolHeader()) {
			case "001": 
				System.out.println("O processo " + message.getProcessID() + " uniu-se ao grupo.");
				ServerController.getInstance().addToGroup(message.getProcessID());
				break;
			case "002":
				System.out.println("O processo "+ message.getProcessID() + " está ativo.");
				ServerController.getInstance().addToGroup(message.getProcessID());
				break;
			case "003":
				System.out.println("O processo "+ message.getProcessID() + " quer saber quem está ativo.");
				ServerController.getInstance().sendMessage("002", ServerController.getInstance().getProcessID());
				break;
			case "004":
				System.out.println("O processo "+ message.getProcessID() + " realizou um evento. Aguardando confirmação...");
				break;
			case "005":
				System.out.println("O processo "+ message.getProcessID() + " quer confirmar um evento.");
				break;
			case "006":
				System.out.println("O processo "+ message.getProcessID() + " confirmou um evento.");
				break;
			case "007":
				System.out.println("O processo "+ message.getProcessID() + " permitiu a entrega da mensagem para a aplicação.");
				break;
		}
		
	}
	
	private Object deserialize(byte[] data) {
        ByteArrayInputStream message = new ByteArrayInputStream(data);

        try {
            ObjectInput reader = new ObjectInputStream(message);
            return (Object)reader.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

	private boolean isValidMessage () {
		return this.getReceivedMessage() instanceof Message;
	}
	
	private boolean isSelfMessage (Message message) {
		return message.getProcessID().equals(ServerController.getInstance().getProcessID());
	}
	
	public Object getReceivedMessage() {
		return receivedMessage;
	}

	public void setReceivedMessage(Object receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

}
