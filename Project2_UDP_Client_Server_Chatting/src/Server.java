import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
	// 버퍼 사이즈.
    private final static int BUFFER = 1024;
    
    private DatagramSocket socket;
    
    // 클라이언트의 주소들을 담을 배열.
    private ArrayList<InetAddress> clientAddresses;
    
    // 클아이언트의 포트를 담을 배열.
    private ArrayList<Integer> clientPorts;
    
    // 현재 접속중인 클라이언트.
    private HashSet<String> existingClients;
    
    public Server(int port) throws IOException {
    	// 소켓 초기화.
        socket = new DatagramSocket(port);
        clientAddresses = new ArrayList();
        clientPorts = new ArrayList();
        existingClients = new HashSet();
    }
    
    public void run() {
        byte[] buf = new byte[BUFFER];
        while (true) {
            try {
                Arrays.fill(buf, (byte)0);
                // 패킷 초기화.
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                // 패킷 수신을 기다린다.
                socket.receive(packet);
                
                // 패킷이 수신되면 패킷의 내용을 String으로 변환.
                String content = new String(buf, buf.length);
                
                // 패킷을 전송한 클아이언트의 주소를 얻는다.
                InetAddress clientAddress = packet.getAddress();
                
                // 패킷을 전송한 클라이언트의 포트번호를 얻는다.
                int clientPort = packet.getPort();
                
                // 문자열 출력을 위해 클라이언트의 포트 번호를 String타입으로 변환.
                String id = String.valueOf(clientPort);
                
                // 해당패킷을 전송한 클아이언트가 existingClients 배열에 존재하지 않으면 새로 추가한다.
                if (!existingClients.contains(id)) {
                    existingClients.add( id );
                    clientPorts.add( clientPort );
                    clientAddresses.add(clientAddress);
                }
                
                System.out.println(id + " : " + content);
                
                // 패킷에 담기위해 수신된 컨텐츠를 바이트로 변환.
                byte[] data = (id + " : " +  content).getBytes();
                
                // 현재 접속해있는 모든 클라이언트에게 수신된 컨텐츠를 보낸다.
                for (int i=0; i < clientAddresses.size(); i++) {
                    InetAddress cl = clientAddresses.get(i);
                    int cp = clientPorts.get(i);
                    packet = new DatagramPacket(data, data.length, cl, cp);
                    socket.send(packet);
                }
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
    
    public static void main(String args[]) throws Exception {
		int PORT;
    	try {
    		PORT = Integer.parseInt(args[0]);
    	} catch(Exception e){
    		PORT = 8080;
    		System.out.println("[에러발생]디폴트 포트 8080으로 설정됩니다.");
    	}
		Server s = new Server(PORT);
        s.start();
    }
}