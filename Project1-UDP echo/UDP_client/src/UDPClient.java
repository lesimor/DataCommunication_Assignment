import java.io.*;
import java.net.*;

class UDPClient
{
  public static void main(String args[]) throws Exception
  {
    while(true){
	  // IP주소
      String ip_address = args[0];
      
      // 포트번호
      int port_num = Integer.parseInt(args[1]);
      
      // 문자열 입력을 기다린다.
      BufferedReader inFromUser =
          new BufferedReader(new InputStreamReader(System.in));
      
      // 소켓 생성.
      DatagramSocket clientSocket = new DatagramSocket();
      
      // 로컬호스트의 ip주소를 받는다.
      InetAddress IPAddress = InetAddress.getByName(ip_address);
      
      // 보낼 데이터를 담을 공간 초기화.
      byte[] sendData = new byte[1024];
      
      // 받을 데이터를 담을 공간 초기화.
      byte[] receiveData = new byte[1024];
      
      // 입력받은 문자열을 스트링 타입으로 변환.
      String sentence = inFromUser.readLine();
      
      // 입력받은 문자열을 바이트값으로 변환.
      sendData = sentence.getBytes();
      
      // 보낼 데이터그램 인스턴스를 선언하고 변환한 바이트값을 넣는다.
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port_num);
      
      // 소켓을 통해 패킷을 보낸다.
      clientSocket.send(sendPacket);
      
      // 받을 패킷을 담을 데이터패킷 인스턴스를 선언.
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      
      // 소켓으로부터 데이터패킷 수신을 기다린다.
      clientSocket.receive(receivePacket);
      
      // 수신한 데이터패킷을 스트링타입으로 변환.
      String receivedSentence = new String(receivePacket.getData());
      
      // 받은 데이터를 출력.
      System.out.println("낯선 상대:" + receivedSentence);
      
      // 소켓을 닫는다.
      clientSocket.close();
      
    }

  }
}