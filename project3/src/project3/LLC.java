package project3;

import java.nio.ByteBuffer;

abstract class LLC {
	public static int DESTINATION_ADDRESS_INDEX = 0;
	public static int SOURCE_ADDRESS_INDEX = 6;
	public static int PDU_INDEX = 12;
	public static int DATA_PADDING_INDEX = 14;
	public static int CONTROL_INDEX = 16;
	
	
	int MAX_PACKET_SIZE = 518;
	
	public byte data[] = new byte[MAX_PACKET_SIZE];
	ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
	
	public LLC(){

	}
	
	public byte[] get_bytes(){
		return data;
	}
	
}

// 채팅 메시지.
class IFrame extends LLC {
	// 메시지가 들어갈 위치.
	private int information_index = 18;
	private int sequence;
	
	public IFrame(String message, int _sequence){
		
		// 1. PDU set (2bytes -> short)
		buffer.putShort(super.PDU_INDEX, (short)(information_index + message.length() + 4));
				
		// 2. Control 설정.
		_sequence %= 128;	// 128 이상 되면 안됨...
		// 시퀀스 설정.
		sequence = _sequence;
		// N(S) 설정.(MSB 포함 1byte, MSB는 무조건 0)
		buffer.put(super.CONTROL_INDEX, (byte)_sequence);
		
		// N(R) 은 무조건 0이므로 무시.
		
		// 3. 메시지 삽입.
		// 바이트 값으로 변경한 메시지를 임시로 저장하는 바이트배열..
		byte[] message_bytes = message.getBytes();
		// 위치에 값 추가.
		buffer.put(message_bytes, information_index, message.length());
		
				
		// TODO: 4. CRC set
		
		
		
		data = buffer.array();
	}
}

// ACK/NACK
class SFrame extends LLC {
	public static int RR = 1;
	public static int RNR = 2;
	public static int REJECT = 3;
	
	private int sequence;
	
	public SFrame(int code, int _sequence){
		// 시퀀스 설정.
		sequence = _sequence;
		
		// 1. PDU 설정
		// 고정 -> 6(dest address) + 6(source address) + 2(PDU) + 4(SAP:2+control:2) + 3(CRC)
		buffer.putShort(super.PDU_INDEX, (short)21);
			
		// 2. control 설정.
		//SS 설정.
		switch (code) {
		case 1:
			buffer.put(super.CONTROL_INDEX, (byte)0x80);
			break;
		case 2:
			buffer.put(super.CONTROL_INDEX, (byte)0xA0);
			break;
		case 3:
			buffer.put(super.CONTROL_INDEX, (byte)0x90);
			break;
		default:
			// 이외의 경우에는 NACK 처리.
			buffer.put(super.CONTROL_INDEX, (byte)0x90);
			break;
		}
		
		// N(R)설정.
		buffer.put(super.CONTROL_INDEX + 1, (byte)sequence);
		
		// 3. TODO: CRC 설정 
		
	}
}

// 연결.
class UFrame extends LLC {
	public static int SABME = 1;
	public static int UA = 2;
	
	public UFrame(int code){
		// 1. PDU 설정
		// 고정 -> 6(dest address) + 6(source address) + 2(PDU) + 3(SAP:2+control:1) + 3(CRC) = 20
		buffer.putShort(super.PDU_INDEX, (short)20);
		
		// 2. Control 설정.
		switch (code) {
		case 1:	// SABME(11110110)
			buffer.put(super.CONTROL_INDEX, (byte)0xF6);
			break;
		case 2:	// UA(11000110)
			buffer.put(super.CONTROL_INDEX, (byte)0xC6);
			break;
		default:
			break;
		}
		
		// 3. TODO: CRC 설정
	}
}
