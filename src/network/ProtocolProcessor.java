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
				System.out.println("A comunicação funciona");
				System.out.println("Mensagem Recebida:" + (String)message.getContentMessage());
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
