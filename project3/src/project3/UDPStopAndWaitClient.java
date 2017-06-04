package project3;

import java.io.*;
import java.net.*;
import java.nio.*;

class UDPStopAndWaitClient{
	private static final int BUFFER_SIZE = LLC.MAX_PACKET_SIZE;
	private static final int PORT = 6789;
	private static final String HOSTNAME = "localhost";
	private static final int BASE_SEQUENCE_NUMBER = 0;

    public static void main(String args[]) throws Exception{
   		// 소켓 생성.
		DatagramSocket socket = new DatagramSocket();
		
		// 소켓의 타임아웃을 1초로 설정.
		socket.setSoTimeout( 1000 );

		// 시퀀스 번호.
		Integer client_sequence = BASE_SEQUENCE_NUMBER;

		// 송신 데이터와 수신 데이터를 담을 바이트 배열 초기화.
		byte[] sendData = new byte[ BUFFER_SIZE ];
		byte[] receiveData = new byte[ BUFFER_SIZE ];

		// 서버의 IP 주소를 얻는다.
		InetAddress IPAddress = InetAddress.getByName( HOSTNAME );
		
		// UA가 온 이후에 true로 변경.
		boolean accept = false;
		
		// counter -> 전송 시도 횟수.
		for (int counter = 0; counter < 200; counter++) {
			// 128이 넘으면 안됨.
			client_sequence %= 128;
			
			// U frame 전송 시도.
			if(!accept){
				sendData = new UFrame(UFrame.SABME).getData();
				try{
					// Send the UDP Packet to the server
					System.out.println("서버에게 SABME U-frame을 전송합니다.");
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, IPAddress, 6789);
					socket.send( packet );

					// Receive the server's packet
					DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
					socket.receive( received );
					
					UFrame receive_u = new UFrame();
					receive_u.setData(received.getData());
					
					// 코드 번호를 얻는다.
					int u_code_from_server = receive_u.extractCode();
					if(u_code_from_server == UFrame.UA){
						System.out.println( "서버로부터 UA 도착!! 연결되었습니다. ");
						accept = true;	// accept를 true로 변경.
					}
					
				}catch (Exception e) {
					System.out.println("SAMBE U-Frame을 다시 전송합니다.");
				}
			}
			
			// 서버로부터 ACK가 오면 타임아웃을 false로 바꿔서 루프문에서 빠져나오도록.
			boolean timedOut = true;
			
			// accept가 되기 전까지는 메시지를 전송하지 않음.
			while( timedOut && accept){
				client_sequence++;
								
				// 보낼 메시지를 IFrame에 실어서 초기화 후 바이트 배열로 변환..
				String msg = "Hello world!";
				sendData = new IFrame(msg, client_sequence).getData();
				
				System.out.println( "메시지: "+ msg + "(시퀀스 No." + client_sequence + ")" );

				try{
					// UDP 패킷을 서버에 전송.
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, IPAddress, 6789);
					socket.send( packet );

					// 서버로부터 패킷을 받는다.
					DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
					socket.receive( received );
					
					SFrame receive_s = new SFrame();
					receive_s.setData(received.getData());
					
					// 서버로부터 온 패킷의 시퀀스 번호를 확인한다.
					int server_sequence = receive_s.extractSequence();
					
					// 보낸 패킷과 도착한 패킷의 시퀀스 번호가 같은지 확인.
					if(receive_s.extractCode() == SFrame.RR && client_sequence == server_sequence){
						System.out.println( "서버로부터 ACK 도착(시퀀스 No." + server_sequence + ")");
						// 서버로부터 ACK를 수신하면 루프문에서 탈출.
						timedOut = false;
					}
					
				} catch( SocketTimeoutException exception ){
					// 서버로부터 ACK를 수신하지 못하면 재전송.
					System.out.println( "타임아웃 발생ㅠㅠ(시퀀스 No." + client_sequence + " 재전송)" );
					client_sequence--;
				}
			}	
		}

		socket.close();
   	}
}
