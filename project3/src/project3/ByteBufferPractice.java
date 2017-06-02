package project3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ByteBufferPractice {
	enum AA {A1,A2,A3};
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ByteBuffer buffer = ByteBuffer.allocate(232);
		buffer.order( ByteOrder.BIG_ENDIAN ); // 자바는 big_endian방식


//		buffer.put(0,(byte)0x01); // 4
//		buffer.put(1, (byte)0x02 ); // 2
//		buffer.put(1, (byte)0x03 ); // 2
		 
		byte[] results = buffer.array( ); 
		
		ByteBuffer rbuffer = ByteBuffer.wrap(results);
		
		System.out.println(rbuffer.get());
		System.out.println(rbuffer.get());
		
		String str = "hello!! hey man";   
	      byte[] bytes = str.getBytes(Charset.forName("UTF-8" ));
//	      ByteBuffer buf = ByteBuffer.wrap(bytes);   // java.nio 는 java.io 에서 메모리상에서 변경하는 것을 지원하지 못해서 nio가 나왔습니다. 그래서 nio 는 생성자가 없습니다. 메모리에서 주소값이 변경되는것을 방지하기 위해서죠.. wrap 메서드는  버퍼에서 byte[] 를 담는다고 생각하시면됩니다.
//	      System.out.println(buf.toString()); // 이렇게 하면 java.nio.HeapByteBuffer[pos=0 lim=21 cap=21] 이런식으로 출력됩니다. 시작위치 , 끝 ,  용량 들을 출력합니다.
//	      for(int i = 0; i < buf.limit(); i++)
//	       System.out.println(buf.get(i));
	  // 다시 스트링으로 변환 
//	      byte[] rtnBytes = buf.array();
//	      String rtnStr = new String(rtnBytes);
//	      System.out.println(rtnStr);

	      // step1: string값을 임시 바이트버퍼에 넣기.
	      
	      
	      
	      
	      byte[] rtnBytes2 = new byte[1000];
	      System.arraycopy(bytes, 0, rtnBytes2, 0, str.length());
	      String rtnStr2 = new String(rtnBytes2, Charset.forName("UTF-8"));
	      System.out.println(rtnStr2);
		





//         buffer.putShort( ( short ) this.packetSendSize ); // 2바이트 -총 길이(바디+헤더)
//         buffer.putInt( ( int ) 0x0000 ); // 4 -0x0000:요청 -0x0001:응답
//         buffer.putInt( Integer.parseInt( seq ) ); // 4
//         buffer.putInt( ( int ) 0x0100 ); // 4
//         buffer.put( ret00.getBytes( ) ); // 40
//         buffer.put( ret01.getBytes( ) ); // 40
//         buffer.put( ret02.getBytes( ) ); // 40
//         buffer.putInt( ( int ) flag0 ); // 4
//         buffer.put( cotent ); // 84
//         buffer.putInt( ( int ) flag1 ); // 4
         

	      int a = 1222;
	      System.out.println((byte)a);
	      
	      byte[] str1 = str.getBytes();
	      rbuffer.put(str1, 0, str.length());
//	      System.arraycopy(str1, 0, rbuffer, 0, str.length());
	      
	      byte[] tmp = rbuffer.array();
	      System.out.println(new String(tmp));
	      

	      System.out.println(AA.A1.values());
	      

	}

}
