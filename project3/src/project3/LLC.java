package project3;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.io.IOException;
import java.lang.Object;


abstract class LLC {
	public static int DESTINATION_ADDRESS_INDEX = 0;
	public static int SOURCE_ADDRESS_INDEX = 6;
	public static int PDU_INDEX = 12;
	public static int SAP_INDEX = 14;
	public static int CONTROL_INDEX = 16;
	
	public static int IS_ERROR = 0;
	public static int IS_IFRAME = 1;
	public static int IS_SFRAME = 2;
	public static int IS_UFRAME = 3;
	
	
	public static int MAX_PACKET_SIZE = 518;
	
	public byte data[] = new byte[MAX_PACKET_SIZE];
	ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
	
	// 컨트롤 코드 사이즈.
	protected int control_size = 2;
	
	public LLC(){

	}
	
	// 패킷을 가져오는 부분.
	public byte[] getData(){
		return data;
	}
	
	// 패킷을 인스턴스에 저장
	public void setData(byte[] bytes){
		this.data = bytes;
	}
	
	// 패킷의 크기.
	public static int getDataSize(byte[] bytes){
		// PDU부분을 읽는다.
		byte[] pdu_tmp = new byte[2];
		System.arraycopy(bytes, PDU_INDEX, pdu_tmp, 0, 2);
		ByteBuffer wrapped = ByteBuffer.wrap(pdu_tmp);
		
		return wrapped.getShort();
	}
	
	public static long getCRC32Value(byte[] bytes) {
		 
		Checksum checksum = new CRC32();
		
		// update the current checksum with the specified array of bytes
		checksum.update(bytes, 0, bytes.length);
		
		long checksumValue = checksum.getValue(); 
//		System.out.println("CRC32 checksum for input string is: " + checksumValue);
		
		// get the current checksum value
		return checksumValue;
		
	}
	
	// CRC 체크 메소드.
	// 전체를 다 받는다.
	public static boolean checkCRC(byte[] bytes){
		int total_size = getDataSize(bytes);
		int target_size = total_size;
		
		// crc 검증 부분을 임시로 담아둘 바이트배열.
		byte[] target_byte = new byte[target_size];
		System.arraycopy(bytes, 0, target_byte, 0, target_size);
		
		// 해당 데이터의 crc를 저장할 바이트배열.
		byte[] target_crc = new byte[4];
		// 해당 데이터의 crc를 가져온다.
		System.arraycopy(bytes, target_size - 4, target_crc, 0, 4);
		
		// 해당 데이터의 crc 결과값을 가져올 바이트버퍼 공간.
		ByteBuffer crc_buffer = ByteBuffer.allocate(4);
		// 하위 4자리를 0으로 바꾼다.
		for(int i = 0; i < 4 ; i++){
			target_byte[total_size-i-1] = (byte)0x00;
		}
		crc_buffer.putInt((int)getCRC32Value(target_byte));
		
		// 결과 crc를 담아둘 바이트배열.
		byte[] result_crc = crc_buffer.array();
		
		// 두 crc가 같으면 true
		if (Arrays.equals(target_crc, result_crc)){
			return true;
		} else {
			return false;
		}
		
	}
	// 디버깅용 패킷 출력 메소드.
	public abstract String byteArrayToHex();
	
	// 패킷 타입 판단 메소드.
	public static int whichFrame(byte[] bytes){
		// Control 바이트의 MSB를 일단 확인.
		int msb = (bytes[CONTROL_INDEX] & 0xff) >> 7;
		if(msb == 0){
			return IS_IFRAME;
		} else if (msb == 1){
			int msb2 = (bytes[CONTROL_INDEX] & 0xff) >> 6;
			if(msb2 == 2){
				return IS_SFRAME;
			} else if(msb2 == 3) {
				return IS_UFRAME;
			} else {
				return IS_ERROR;
			}
		} else {
			return IS_ERROR;
		}
	}

}



