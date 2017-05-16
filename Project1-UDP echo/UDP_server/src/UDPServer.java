import java.io.*;
import java.net.*;

class UDPServer
{
  public static void main(String args[]) throws Exception
  {   
    // 포드번호를 임의로 설정하고 소켓을 연다.
    DatagramSocket serverSocket = new DatagramSocket(8080);
    // 받은 데이터를 저장할 공간 초기화.
    byte[] receiveData = new byte[1024];
    // 전송할 데이터를 저장할 공간 초기화.
    byte[] sendData = new byte[1024];
    while(true)
    {
      // 수신할 데이터를 담을 데이터패킷 선언.
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      
      // 소켓으로부터 패킷을 받는다.
      serverSocket.receive(receivePacket);
      
      // 받은 패킷의 데이터 부분을 스트링으로 변환.
      String sentence = new String( receivePacket.getData());
      
      // 변환한 스트링을 출력.
      System.out.println("클라이언트로부터의 메시지: " + sentence);
      
      // 수신한 패킷의 출처 주소를 얻는다.
      InetAddress IPAddress = receivePacket.getAddress();
      
      // 해당 주소로부터 포트번호를 얻는다.
      int port = receivePacket.getPort();
      
      // 스트링을 바이트값으로 변환.
      sendData = sentence.getBytes();
      
      // 보낼 데이터패킷을 선언하고 스트링값을 넣는다.
      DatagramPacket sendPacket =
          new DatagramPacket(sendData, sendData.length, IPAddress, port);
      
      // 소켓을 통해 해당 데이터패킷을 보낸다.
      serverSocket.send(sendPacket);
    }
  }
}