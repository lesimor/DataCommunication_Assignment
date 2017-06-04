import java.io.*;
import java.net.*;
import java.util.*;


class MessageSender implements Runnable {
    private DatagramSocket sock;	// 데이터그램 소켓 객체.
    private String hostname;		// 호스트 명.
    private int port;				// 포트 번호.
    
    MessageSender(DatagramSocket s, String h, int p) {
        sock = s;
        hostname = h;
        port = p;
    }
    
    // 메시지 전송.
    private void sendMessage(String s) throws Exception {
    	// 스트링값을 바이트로 변환.
        byte buf[] = s.getBytes();
        
        // 호스트명으로부터 주소를 얻는다.
        InetAddress address = InetAddress.getByName(hostname);
        
        // 패킷에 메시지 및 목적지 정보를 담는다.
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        
        // 소켓을 통해 패킷을 보낸다.
        sock.send(packet);
    }
    
    public void run() {
        boolean connected = false;
        do {
            try {
            	// 처음 접속시 인사.
                sendMessage("Hello!!");
                
                // 접속 여부를 true로 설정.
                connected = true;
            } catch (Exception e) {
                
            }
        } while (!connected);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
            	// 입력이 들어오지 않는 경우 다른 스레드에게 양보하기 위해 sleep 모드로 전환.
                while (!in.ready()) {
                    Thread.sleep(100);
                }
                // 입력이 들어온 경우 메시지를 전송한다.
                sendMessage(in.readLine());
            } catch(Exception e) {
            	// 예외처리.
                System.err.println(e);
            }
        }
    }
}
class MessageReceiver implements Runnable {
	// 소켓.
    DatagramSocket sock;
    
    // 문자열을 담을 버퍼.
    byte buf[];
    MessageReceiver(DatagramSocket s) {
        sock = s;
        buf = new byte[1024];
    }
    public void run() {
        while (true) {
            try {
            	// 패킷 초기화.
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                // 소켓을 통해 패킷을 수신.
                sock.receive(packet);
                
                // 패킷에 있는 문자열 정보를 가져온다.
                String received = new String(packet.getData(), 0, packet.getLength());
                
                // 수신 패킷의 내용을 표시한다.
                System.out.println(received);
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}
public class Client {
    
    public static void main(String args[]) throws Exception {
    	
    	int PORT;
    	
    	try {
    		PORT = Integer.parseInt(args[0]);
    	} catch(Exception e){
    		PORT = 8080;
    		System.out.println("[에러발생]디폴트 포트 8080으로 설정됩니다.");
    	}
    	
    	DatagramSocket socket = new DatagramSocket();
        MessageReceiver r = new MessageReceiver(socket);
        MessageSender ms = new MessageSender(socket, "localhost", PORT);
        
        // MessageReceiver 쓰레드 생성.
        Thread rt = new Thread(r);
        
        // MessageSender 쓰레드 생성.
        Thread st = new Thread(ms);
        
        // 각각의 쓰레드 실행.
        rt.start(); st.start();
    }
}