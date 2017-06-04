package project3;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class TestZone {

	public static void main(String[] args) {
		
	      // 송신측 인스턴스 초기화 방법.(셋중에 하나씩만 테스트할 것)
		   IFrame test_frame = new IFrame("hello world!!", 3);
//		   SFrame test_frame = new SFrame(SFrame.RR, 55);
//	      UFrame test_frame = new UFrame(UFrame.SABME);

	      // 인스턴스에서 데이터 부분을 뽑아냄...
	      byte[] test_bytes = test_frame.getData();
	      
	      // 어찌어찌 해서 데이터를 포함한 패킷이 전달됨....
	      
	      //CRC 체크
	      if(LLC.checkCRC(test_bytes)){
	    	  System.out.println("CRC check pass!!");
	    	  if (LLC.whichFrame(test_bytes) == LLC.IS_IFRAME){
	    		  System.out.println("I Frame입니다.");
	    		  IFrame i_frame = new IFrame();	// I프레임 객체 생성.
	    		  i_frame.setData(test_bytes);		// 객체에 바이트배열 입력.
	    		  
	    		  System.out.println(i_frame.byteArrayToHex()); // 패킷 시각화.
	    		  System.out.println("I-Frame message : "+i_frame.extractMessage()); // 메시지 추출.
	    	      System.out.println("I-Frame sequence : "+i_frame.extarctSequence()); // 시퀀스 추출.
	    		  
		      } else if (LLC.whichFrame(test_bytes) == LLC.IS_SFRAME){
		    	  System.out.println("S Frame입니다.");
		    	  SFrame s_frame = new SFrame();	// S프레임 객체 생성.
		    	  s_frame.setData(test_bytes);		// 객체에 바이트배열 입력.
		    	  
			      System.out.println(s_frame.byteArrayToHex());	// 패킷 시각화.
			      System.out.println("s_frame code: " + s_frame.extractCode()); // 코드 추출.
		      } else if (LLC.whichFrame(test_bytes) == LLC.IS_UFRAME){
		    	  System.out.println("U Frame입니다.");
		    	  UFrame u_frame = new UFrame();	// U프레임 객체 생성.
		    	  u_frame.setData(test_bytes);		// 객체에 바이트배열 입력.
		    	  
			      System.out.println(u_frame.byteArrayToHex());	// 패킷 시각화.
			      System.out.println("u_frame code: " + u_frame.extractCode()); // 코드 추출.

		      }   
	      } else {
	    	  System.out.println("패킷 손상...");
	      }
//	      System.out.println("-----------------");
//	      IFrame i_frame = new IFrame();	// I프레임 객체 생성.
//		  i_frame.setData(test_bytes);		// 객체에 바이트배열 입력.
//		  System.out.println(i_frame.byteArrayToHex());
//		  
//		  Checksum checksum = new CRC32();
//		  checksum.update(i_frame.getData());
//		  System.out.println(i_frame.byteArrayToHex());
		  
	}	

}
