package udp_server;
import java.net.*;
import java.io.*;


public class UDPServer {
	public final static int discardPort = 3000;

	static byte[] buffer = new byte[65507];

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port;
		try{
			port=Integer.parseInt(args[0]);
			
		}catch(Exception e){
			port=discardPort;
		}
		System.out.println(port + "번 포트를 엽니다...");
		try{
			DatagramSocket ds = new DatagramSocket(port);
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			while(true){
				try{
					ds.receive(dp);
					String s = new String(dp.getData(), 0, 0, dp.getLength());
					System.out.println(dp.getAddress()+""+ dp.getPort() +"번 포트로부터의 메시지: "+s);
				}catch(IOException e){
					System.err.println(e);
				}

			}

		}catch(SocketException se){
			System.out.println("소켓 에러 발생");
			System.err.println(se);
		}
	}

}
