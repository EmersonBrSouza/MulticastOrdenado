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
			case "001": // When one process join to group
				System.out.println("O processo " + message.getProcessID() + " uniu-se ao grupo.");
				ServerController.getInstance().addToGroup(message.getProcessID());
				break;
			case "002": // When some process notifies if is active
				System.out.println("O processo "+ message.getProcessID() + " está ativo.");
				ServerController.getInstance().addToGroup(message.getProcessID());
				break;
			case "003": // When some process wants know how many processes are active
				System.out.println("O processo "+ message.getProcessID() + " quer saber quem está ativo.");
				ServerController.getInstance().sendMessage("002", ServerController.getInstance().getProcessID());
				break;
			case "004":
				System.out.println("O processo "+ message.getProcessID() + " realizou um evento. Aguardando confirmação...");
				break;
			case "005": // When some process wants confirm one event
				System.out.println("O processo "+ message.getProcessID() + " quer confirmar um evento.");
				Object[] data = (Object[])message.getContentMessage();
				ServerController.getInstance().sendConfirmation((String)data[0], (Integer)data[1], message);
				break;
			case "006": // When some process sends a event confirmation
				System.out.println("O processo "+ message.getProcessID() + " confirmou um evento.");
				Object[] event = (Object[])message.getContentMessage();
				ServerController.getInstance().validateEvent((String)event[0], (Integer)event[1], message);
				break;
			case "007":
				System.out.println("O processo "+ message.getProcessID() + " permitiu a entrega da mensagem para a aplicação.");
				Object[] validMessage = (Object[])message.getContentMessage();
				ServerController.getInstance().sendToApplication((String)validMessage[0], (Integer)validMessage[1], message);
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
