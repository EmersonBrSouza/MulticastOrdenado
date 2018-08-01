package controllers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Enumeration;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import models.LogicalClock;
import models.Message;
import network.Server;

public class ServerController {

	
	private static ServerController serverController = null;
	private LogicalClock clock = new LogicalClock();
	private PriorityQueue<Message> orderedMessages = new PriorityQueue<Message>();
	private Server server;
	private String processID;
	private ConcurrentHashMap<String, Boolean> connectedMembers = new ConcurrentHashMap<String, Boolean>();
	private Thread runner;
	
	// Singleton Implementation
	private ServerController () {}
	
	public static ServerController getInstance () {
		if (serverController == null) {
			serverController = new ServerController();
			serverController.processID = serverController.generateProcessID();
			
		}
		return serverController;
	}
	
	/**
	 * Generate a random ID to process
	 * 
	 * @param void
	 * @return processID - A random key to process.
	 * */
	private String generateProcessID() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Mount a message to send
	 * 
	 * @param String protocolHeader - An action to be executed in server
	 * @param Object content - The message's content
	 * @return void
	 * */
	public void sendJoinMessage () {
		try {
			clock.tick();
			Message message = new Message("001", "", clock.getTimestamp(), processID);
			this.send(message.serialize());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Mount a message to send
	 * 
	 * @param String protocolHeader - An action to be executed in server
	 * @param Object content - The message's content
	 * @return void
	 * */
	public void sendMessage (String protocolHeader, Object content) {
		try {
			Message message = new Message(protocolHeader, content, clock.getTimestamp(), processID);
			this.send(message.serialize());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Mount a message to send
	 * 
	 * @param String protocolHeader - An action to be executed in server
	 * @param Object content - The message's content
	 * @return void
	 * */
	public void sendMessage (String protocolHeader, Object content, ConcurrentHashMap<String, Boolean> confirmationsNeeded) {
		try {
			Message message = new Message(protocolHeader, content, clock.getTimestamp(), processID);
			message.setConfirmations(confirmationsNeeded);
			this.send(message.serialize());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receive a message and put in queue
	 * 
	 * @param String protocolHeader - An action to be executed in server
	 * @param Object content - The message's content
	 * @return void
	 * */
	public void registerEvent (Message message) {
		clock.tick(message.getTimestamp());
		orderedMessages.add(message);
	}
	
	/**
	 * Send a message to group
	 * 
	 * @param byte[] message - A serialized message
	 * @return void
	 * */
	public synchronized void send (byte[] message) throws IOException {
		DatagramPacket packet = new DatagramPacket(message, message.length, server.getFormattedGroupAddress(), server.getGroupPort());
		MulticastSocket socket = new MulticastSocket();
		socket.send(packet);
		socket.close();
	}
	
	/**
	 * Define a server to this controller
	 * 
	 * @param Server server - The application's server
	 * @return void
	 * */
	public void setServer (Server server) {
		this.server = server;
	}
	
	/**
	 * Get the processID
	 * 
	 * @param void
	 * @return String processID - A process ID
	 * */
	public String getProcessID () {
		return this.processID;
	}
	
	public synchronized void addToGroup (String processID) {
		this.connectedMembers.put(processID, false);
	}
	
	public int getGroupSize () {
		return this.connectedMembers.size();
	}
	
	public void clearGroup () {
		this.connectedMembers = new ConcurrentHashMap<String, Boolean>();
	}

	public void sendConfirmation(String processID, Integer messageTimestamp, Message message) {
		if (!message.getConfirmations().containsKey(this.getProcessID())) return;
		System.out.println("Enviando confirmação do evento: " + (Integer)orderedMessages.peek().getContentMessage()+ " -- Tempo Lógico: " + orderedMessages.peek().getTimestamp());
		this.sendMessage("005", new String[] {this.getProcessID(), message.getProcessID()});
	}

	public void validateEvent(String processID) {
		orderedMessages.peek().receiveConfirmation(processID);
	}

	public void sendToApplication(String processID, Integer messageTimestamp) {
		
		if(orderedMessages.peek().getProcessID().equals(processID) && orderedMessages.peek().getTimestamp().equals(messageTimestamp)) {
			System.out.println("Mensagem entregue à aplicacação. Evento: " +  (Integer)orderedMessages.peek().getContentMessage() + 
								" -- Tempo Lógico: " + orderedMessages.peek().getTimestamp());
			orderedMessages.poll();
		}
		
	}
	
	public void reorderQueue (String processID, Integer messageTimestamp) {
		if (orderedMessages.size() == 0 ) { return;}
		Message head = orderedMessages.peek();
		
		while(!orderedMessages.peek().getProcessID().equals(processID) 
				&& !orderedMessages.peek().getTimestamp().equals(messageTimestamp)) {
			orderedMessages.add(orderedMessages.poll());
			
			if(orderedMessages.peek().getProcessID().equals(head.getProcessID())
				&& !orderedMessages.peek().getTimestamp().equals(head.getTimestamp())) {
				this.sendMessage("008", new Object[] {processID, messageTimestamp});
			}
		}
	}
	
	public void queueProcessor () {
		this.runner = new Thread () {
			public void run () {
				while(true) {
					try {
						sleep(500);
						int currentSize = orderedMessages.size();
						if (currentSize > 0 ) {
							Message currentMessage = orderedMessages.peek();
							sendMessage("004", new Object[] {currentMessage.getProcessID(), currentMessage.getTimestamp()}, connectedMembers); // Request a confirmation
							sleep(2000);
							if (orderedMessages.size() > 0) {
								if (orderedMessages.peek().isConfirmed()) {
									sendMessage("006", new Object[] {currentMessage.getProcessID(), currentMessage.getTimestamp()});
									sleep(500);
									sendToApplication(currentMessage.getProcessID(), currentMessage.getTimestamp());
								}								
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}				
			}
		};
		
		runner.start();
	}

	public void sendRandomAction(int value) {
		try {
			clearGroup();
			sendMessage("001", ""); // Find active nodes
			Thread.sleep(2000);
			// Send a message with event
			clock.tick();
			this.sendMessage("003", value, this.connectedMembers);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
