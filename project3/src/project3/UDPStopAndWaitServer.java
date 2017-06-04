package project3;


import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

class UDPStopAndWaitServer{
	private static final int BUFFER_SIZE = LLC.MAX_PACKET_SIZE;
//	private static final int PORT = 6789;

	public static void main(String[] args) throws IOException {
		// 포트번호.
		int PORT;
    	try {
    		PORT = Integer.parseInt(args[0]);
    	} catch(Exception e){
    		PORT = 5678;
    		System.out.println("[에러발생]디폴트 포트 5678으로 설정됩니다.");
    	}
    	
		// 서버 소켓 생성.
		DatagramSocket serverSocket = new DatagramSocket( PORT );

		// 송수신 바이트 배열을 초기화.
        byte[] receiveData = new byte[ BUFFER_SIZE ];
        byte[] dataForSend = new byte[ BUFFER_SIZE ];

        // 연결 확인을 위한 무한 루프. 
        while(true){

        	// 패킷을 수신한다.
        	DatagramPacket received = new DatagramPacket( receiveData, receiveData.length );
          	serverSocket.receive( received );
          	
          	// 패킷의 IP주소와 포트번호를 얻는다.
        	InetAddress IPAddress = received.getAddress();
        	int port = received.getPort();
        	
          	// 우선 CRC체크를 한다.
          	if(LLC.checkCRC(received.getData())){
          		System.out.println("CRC 통과.");
          		if (LLC.whichFrame(received.getData()) == LLC.IS_IFRAME){
          			System.out.println("I Frame입니다.");
          			// 	Get the message from the packet
      	  		  	IFrame receive_i = new IFrame();
      	  		  	receive_i.setData(received.getData());
      	  		  	
      	  		  	String message = receive_i.extractMessage();
      	        	int sequence = receive_i.extarctSequence();

          			try {        		  		
    		            Random random = new Random( );
    		            int chance = random.nextInt( 100 );
    		            System.out.println("클라이언트로부터의 메시지: " + message + "(시퀀스No." + sequence + ")");
    	  	            // 절반의 확률로 응답을 해준다.
    	  	            if( ((chance % 2) == 0) ){
    	  	            	// ACK 초기화.
    	  	            	dataForSend = new SFrame(SFrame.RR, sequence).getData();
    	
    	  	            	// 클라이언트에게 패킷을 전송.
    	  	            	DatagramPacket packet = new DatagramPacket( dataForSend, dataForSend.length, IPAddress, port );
    	  	            	serverSocket.send( packet ); 
    	  	            	System.out.println("클라이언트에게 ACK를 보냅니다.");
    	  	            } else {
    	  	            	// 절반의 확률로 아무것도 응답해주지 않는다. 의도적으로 타임아웃 발생.
    	  	            	System.err.println("타임아웃을 발생시키기 위해 아무것도 하지 않습니다.");
    	  	            }
					} catch (Exception e) {
						// 에러 발생시 NACK 발생.
						// NACK 초기화.
	  	            	dataForSend = new SFrame(SFrame.REJECT, sequence).getData();
	
	  	            	// 클라이언트에게 패킷을 전송.
	  	            	DatagramPacket packet = new DatagramPacket( dataForSend, dataForSend.length, IPAddress, port );
	  	            	serverSocket.send( packet );
	  	            	System.out.println("클라이언트에게 NACK를 보냅니다.");
					}
  	    		  	
  		      } else if (LLC.whichFrame(received.getData()) == LLC.IS_SFRAME){
  		    	  // 서버에서 s frame을 보내는 경우는 없음.
  		    	  System.out.println("S Frame입니다.");
  		    	  SFrame receive_s = new SFrame();				// S프레임 객체 생성.
  		    	  receive_s.setData(received.getData());		// 객체에 바이트배열 입력.
  		    	    		    	  
  			      System.out.println(receive_s.byteArrayToHex());	// 패킷 시각화.
  			      System.out.println("s_frame code: " + receive_s.extractCode()); // 코드 추출.
  			      
  		      } else if (LLC.whichFrame(received.getData()) == LLC.IS_UFRAME){
  		    	  System.out.println("U Frame입니다.");
  		    	  UFrame receive_u = new UFrame();				// U프레임 객체 생성.
  		    	  receive_u.setData(received.getData());		// 객체에 바이트배열 입력.
  		    	  
  		    	  // 도착한 U-format 패킷의 코드가 SABME인 경우 UA응답을 준다.
  		    	  if(receive_u.extractCode() == UFrame.SABME){
  		    		  System.out.println("U-format SABME 패킷 도착.");
  		    		  // UA 패킷 생성.
  		    		  dataForSend = new UFrame(UFrame.UA).getData();
  		    		  // 클라이언트에게 전송,
  		    		  System.out.println("클라이언트에게 UA패킷을 응답합니다.");
  		    		  DatagramPacket packet = new DatagramPacket( dataForSend, dataForSend.length, IPAddress, port );
  		    		  serverSocket.send( packet ); 
  		    	  }

  		      }
          		
          	} else {
          		// CRC 불합격.
          		System.out.println("CRC error!!");
          	}
          	
       	}
	}
}