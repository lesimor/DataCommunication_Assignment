package project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class Server {

	public static void main(String[] args) throws IOException {
		byte buf[] = new byte[65507];
		int port = 6789;
		String k = "CONNECT";
		SACKmanager sack = new SACKmanager(); //송신 후에 키보드 입력을 제한 할 클래스
		DatagramSocket socket = new DatagramSocket(port);
		DatagramPacket conPacket = new DatagramPacket(buf, buf.length);
		socket.receive(conPacket);
		buf = k.getBytes();
		conPacket = new DatagramPacket(buf, buf.length, conPacket.getAddress(), conPacket.getPort());

		socket.send(conPacket); //연결되었다고 클라이언트에게 메시지를 보냅니다.
		

		receive receiver = new receive(socket, port, sack); //수신쓰레드를 생성
		receiver.start();
		Sender sender = new Sender(socket, conPacket.getAddress(), conPacket.getPort(), sack);
		sender.start();//송신쓰레드 생성
	}

}

class receive extends Thread{//수신쓰레드
	
	 DatagramSocket Socket = null;
	 private boolean stopped = false;
	 DatagramPacket receivePacket = null;
	 DatagramPacket AckPacket = null;
	 String ackS = "ACK";
	 byte []ack = ackS.getBytes();
	 SACKmanager sack = null;
	 
	 public DatagramPacket getPacket(){
		 
		 return this.receivePacket;
	 }
	
	 public DatagramSocket getSocket() {
		    return this.Socket;
	 }
	 
	 public void halt() {
		 	this.stopped = true;
	 }
	  
	public receive(DatagramSocket socket, int port, SACKmanager sack) throws SocketException{
		this.Socket = socket;
		this.sack = sack;
	}
	
	
	public void run(){
		try{
			while(true){
				byte buf[] = new byte[65507];

				if (stopped)
			        return;
				String ackk = "ACK";
				receivePacket = new DatagramPacket(buf, buf.length);
				Socket.receive(receivePacket); //패킷을 수신합니다.
				
				String s = new String(receivePacket.getData(), 0, receivePacket.getLength());
		        if (ackk.equals(s)){
		        	//수신한 패킷이 ACK라면
		        	System.out.println("ACK is received");
		        	sack.confirmACK();//ACK를 수신했다고 출력하고, wait중인 송신 쓰레드(키보드 입력)을 다시 깨웁니다.

		        	
		        }else{      //수신한 패킷이 일반 메시지라면,
		       
				String msg = new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println("Client에서 보낸 메세지 : " + msg); //메시지출력
				AckPacket = new DatagramPacket(ack, ack.length, receivePacket.getAddress(), receivePacket.getPort());
				//Socket.send(AckPacket); //ACK패킷을 클라이언트에 전송합니다.
		        }
				Thread.yield();
				
				
				
				
				
			}
			
			
			
		} catch (SocketException e) {
			e.printStackTrace();
			} catch (IOException e) {
			e.printStackTrace();
			}
	}
	
}

class Sender extends Thread{ //송신쓰레드(키보드 입력하는 쓰레드)
	  private InetAddress server;

	  private DatagramSocket socket;
	  
	  private boolean stopped = false;
	  SACKmanager ACKM;
	  private int port;
	  DatagramPacket outputP;
	  
	  public DatagramSocket getSocket() {
		    return this.socket;
	  }
	  public void halt() {
		    this.stopped = true;
		  }
	  
	  public Sender(DatagramSocket socket, InetAddress address, int port, SACKmanager sack) throws SocketException {
	    this.server = address;
	    this.port = port;
	    this.socket = socket;
	    this.socket.connect(server, port);
	    this.ACKM = sack;
	  }

	  
	public void run() {

	    try {
	      BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));//키보드입력
	      while (true) {
	        if (stopped)
	          return;
	        String theLine = userInput.readLine();
	        if (theLine.equals("."))
	          break;
	        byte[] data = theLine.getBytes();
	        outputP = new DatagramPacket(data, data.length, server, port);//입력받은 것을 패킷에 담습니다.
	        STimer timer = new STimer(this.socket, outputP, ACKM);//타임아웃을 측정할 쓰레드
	        socket.send(outputP);//패킷전송
	        timer.start();//타이머 시작
	        this.ACKM.waitingACK();//ACK를 받기전까지는 키보드입력을 막기 위해, 송신쓰레드는 대기
	        
	        
	        
	        
	        
	        Thread.yield();
	      }
	    }
	    catch (IOException ex) {
		      System.err.println(ex);
	    } 
	  }
	
}


class SACKmanager{
	//ACK 수신 여부에 따라서, 송신쓰레드를 관리할 클래스
	int ACK = 0;
	void confirmACK(){//receive에서 ACK를 받았다면, 키보드 입력 스레드를 살립니다.
		synchronized (this) {
			ACK=1;
			notify();
		
		}
	}
	
	void waitingACK(){ //sender에서 키보드입력받은 메시지를 전송 후에, 키보드입력을 쉽니다.
		
		synchronized(this){
			try {
				ACK=-1;
				wait();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}


class STimer extends Thread{ //타임아웃관리하는 쓰레드
	DatagramSocket socket;
	DatagramPacket packet;
	SACKmanager ACKm;
	
	public STimer (DatagramSocket socket, DatagramPacket packet, SACKmanager ACKm){
		this.socket= socket;
		this.packet = packet;
		this.ACKm = ACKm;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(5000); //timeout 시간 5초
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ACKm.ACK == -1){
				try {//ACK를 못받았다면, 
					System.out.println("재전송");//재전송할거라고 띄웁니다.
					socket.send(packet);//재전송합니다.
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else if (ACKm.ACK == 1) {
				break;//ACK를 받았다면, 타임아웃 쓰레드를 종료합니다.
				
			}else{
				
			}
			
		}
		
			
		
		
	}
	
	
}
