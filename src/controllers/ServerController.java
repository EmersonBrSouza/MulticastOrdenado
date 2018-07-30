package controllers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.PriorityQueue;
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
			clock.tick();
			Message message = new Message(protocolHeader, content, clock.getTimestamp(), processID);
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
	public void receiveMessage (Message message) {
		clock.tick(message.getTimestamp());
		orderedMessages.add(message);
		
		while(orderedMessages.size() > 0) {
			System.out.println(orderedMessages.poll().getTimestamp());			
		}
		
		this.sendMessage("003", this.getProcessID());
		System.out.println(clock.getTimestamp());
		// System.out.println(orderedMessages.poll().getTimestamp());
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
		this.connectedMembers.put(processID, true);
	}
	
	public void getGroupSize () {
		this.connectedMembers.size();
	}
	
	public void clearGroup () {
		this.connectedMembers = new ConcurrentHashMap<String, Boolean>();
	}
}
