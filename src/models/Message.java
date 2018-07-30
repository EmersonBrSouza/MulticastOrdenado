package models;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable, Comparable{
	
	private static final long serialVersionUID = 1L;
	private String protocolHeader;
	private Object contentMessage;
	private Integer timestamp;
	private String processID;
	private boolean isConfirmed;
	
	public Message (String protocolHeader, Object contentMessage, Integer timestamp, String processID) {
		this.protocolHeader = protocolHeader;
		this.contentMessage = contentMessage;
		this.timestamp = timestamp;
		this.processID = processID;
		this.setConfirmed(false);
	}
	
	public String getProtocolHeader () {
		return this.protocolHeader;
	}
	
	public Object getContentMessage () {
		return this.contentMessage;
	}
	
	public Integer getTimestamp () {
		return this.timestamp;
	}
	
	public String getProcessID() {
		return processID;
	}
	
	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	/**
	 * Serialize a message
	 * 
	 * @param void
	 * @return byte[] serializedMessage
	 * */
	public byte[] serialize () {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
            ObjectOutput out = new ObjectOutputStream(b);
            out.writeObject(this);
            out.flush();
            return b.toByteArray();
        } catch (IOException e) {
        	e.printStackTrace();
            System.err.println("Falha ao serializar mensagem");
        }
		return null;
	}

	@Override
	public int compareTo(Object o) {
		Message m1 = (Message)o;
		if(m1.getTimestamp() > this.getTimestamp()) { 
			return 1;
		} else if (m1.getTimestamp() == this.getTimestamp()) {
			return 0;
		} else {
			return -1;
		}
	}
}
