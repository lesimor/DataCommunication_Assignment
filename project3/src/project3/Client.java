package project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client {
	final static int MAX = 65536;
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("실행방법 틀렸어요. 인자1 : localhost, 인자2:1301");
			System.exit(0);
		}
		InetAddress ia = InetAddress.getByName(args[0]); //주소를 받습니다.
		int port = Integer.parseInt(args[1]);// port를 받습니다.
		 
		ACKmanager ac = new ACKmanager(); //ACK의 여부에 따라 senderThread를 관리하는 클래스입니다.
		SenderThread sender = new SenderThread(ia, port, ac); //메시지 송신 쓰레드 생성
		sender.start();
		ReceiverThread receiver = new ReceiverThread(sender.getSocket(), ac);// 메시지 수신 쓰레드 생성
		receiver.start();
		
	}

}


class SenderThread extends Thread{  //메시지 전송 쓰레드
	  private InetAddress server;

	  private DatagramSocket socket;
	  
	  private boolean stopped = false;
	  ACKmanager ACKM;
	  private int port;
	  DatagramPacket outputP;
	  
	  
	  public DatagramSocket getSocket() {
		    return this.socket;
	  }
	  public void halt() {
		    this.stopped = true;
		  }
	  
	  public SenderThread(InetAddress address, int port, ACKmanager ac) throws SocketException {
	    this.server = address;
	    this.port = port;
	    this.socket = new DatagramSocket();
	    this.socket.connect(server, port); // 입력받았던 정보를 바탕으로 소켓을 연결합니다.
	    this.ACKM = ac;
	  }

	  
	public void run() {

	    try {
	      BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
	      String conn = "USER1"; 
	      byte[] connect = conn.getBytes();
	      outputP = new DatagramPacket(connect, connect.length, server, port);
	      socket.send(outputP); //접속을 하면, 서버에 USER1으로 접속할거라고 전송합니다.
	      // TODO: U frame insert
	      
	      while (true) {
	    	  // TODO: I Frame 
	        if (stopped)
	          return;
	        

	        String theLine = userInput.readLine(); //키보드 입력부분
	        if (theLine.equals("."))
	          break;
	        byte[] data = theLine.getBytes();
	        
	        outputP = new DatagramPacket(data, data.length, server, port); // 전송할 패킷
	        CTimer timer = new CTimer(this.socket, outputP, ACKM); // TimeOut를 관리할 쓰레드를 생성합니다.
	        socket.send(outputP);//패킷을 전송합니다.
	        timer.start();//타이머 쓰레드를 시작합니다.

	        this.ACKM.waitingACK(); // 패킷을 전송하였으므로, ACK를 기다립니다. ACK가 올 때까지 전송쓰레드는 쉽니다.
	        
	        
	        
	        Thread.yield();
	      }
	    }
	    catch (IOException ex) {
		      System.err.println(ex);
	    } 
	  }
	
}


class ReceiverThread extends Thread{ // 수신 쓰레드
	  DatagramSocket socket;
	  ACKmanager ACKM;
	  private boolean stopped = false;

	  public ReceiverThread(DatagramSocket ds, ACKmanager ac) throws SocketException {
	    this.socket = ds;
	    this.ACKM = ac; 
	    
	  }
	  public void halt() {
		    this.stopped = true;
		  }
	  public void run() {
	    byte[] buffer = new byte[65507];
	    while (true) {
	      if (stopped)
	        return; 
	      DatagramPacket dp = new DatagramPacket(buffer, buffer.length);//수신받을 패킷 준비
	      
	      try {
	    	
	        socket.receive(dp); //수신을 받습니다.
	        String ack = "ACK"; // ACK메시지(가정)
	        String con = "CONNECT"; //연결메시지(가정)
	        String s = new String(dp.getData(), 0, dp.getLength());
	        if (ack.equals(s)){ // ACK메시지를 받았다면 // TODO: S frame
	        	
	        	System.out.println("ACK is received"); //ACK를 받았습니다.
	        	ACKM.confirmACK();//ACK를 받았으므로, senderThread를 다시 시작합니다.

	        	
	        }else if(con.equals(s)){
	        	System.out.println("connected!"); //연결이 완료되었다는 메시지를 서버측으로 받았다면 연결되었다고 출력.
	        	
	        }
	        
	        else{ // 서버로부터 일반적인 메시지를 받았을 때,

		        System.out.print("fromServer : "); 
		        System.out.println(s.trim());
		        byte ackB[] = ack.getBytes(); 
		        DatagramPacket ackP = new DatagramPacket(ackB, ackB.length, dp.getAddress(), dp.getPort());
		        socket.send(ackP);// 서버로부터 온 메시지를 받았다고, 서버에 ACK를 보냅니다.
		        
	        }
	        
	        Thread.yield();
	      } catch (IOException ex) {
	        System.err.println(ex);
	      }
	    }
	  }

}

class ACKmanager{
	//ACK수신에 따라 senderThread를 관리하는 클래스
	int ACK = 0;
	void confirmACK(){ //receive에서 사용할 메소드, 잠자고 있는 sender쓰레드를 깨웁니다.
		synchronized (this) {
			ACK=1;
			notify();
		
		}
	}
	
	void waitingACK(){//sender에서 쓸 메소드, sender를 재울 때 씁니다.
		
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


class CTimer extends Thread{ //타임아웃을 할 관리할 쓰레드입니다.
	DatagramSocket socket;
	DatagramPacket packet;
	ACKmanager ACKm;
	
	public CTimer (DatagramSocket socket, DatagramPacket packet, ACKmanager ACKm){
		this.socket= socket;
		this.packet = packet;
		this.ACKm = ACKm;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(5000); //타임아웃 제한시간 5초
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ACKm.ACK == -1){
				try { //ACK가 도착하지 않았을때
					System.out.println("재전송");
					socket.send(packet); //재전송합니다.
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else if (ACKm.ACK == 1) {
				break;//ACK가 도착하였다면, Timer를 나갑니다.
				
			}else{
				
			}
			
		}
		
			
		
		
	}
	
	
}