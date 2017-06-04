package project3;

import java.util.Arrays;

//채팅 메시지.
public class IFrame extends LLC {
	// 메시지가 들어갈 위치.
	public static int INFORMATION_INDEX = 18;
	private int sequence;
	
	public IFrame(){ }
	
	public IFrame(String message, int _sequence){
		
		
		// 1. PDU set (2bytes -> short)
		buffer.putShort(super.PDU_INDEX, (short)(INFORMATION_INDEX + message.length() + 4));
		
		// 2. Control 설정.
		_sequence %= 128;	// 128 이상 되면 안됨...
		// 시퀀스 설정.
		sequence = _sequence;
		// N(S) 설정.(MSB 포함 1byte, MSB는 무조건 0)
		buffer.put(super.CONTROL_INDEX, (byte)_sequence);
		
		// N(R) 은 무조건 0이므로 무시..
		buffer.put(super.CONTROL_INDEX + 1, (byte)0x00);
		
		// 3. 메시지 삽입.
		// 바이트 값으로 변경한 메시지를 임시로 저장하는 바이트배열..
		byte[] message_bytes = message.getBytes();
		// 버퍼에 메시지 추가.
		buffer.position(INFORMATION_INDEX);
		buffer.put(message_bytes);
		
		data = buffer.array();
		// 크기에 맞게 자르기
		data = Arrays.copyOfRange(data, 0, INFORMATION_INDEX + message.length());
		
		// TODO: data를 앞부분만 짤라서 crc에 넣어야한다.
//		System.out.println("원래 body size: " + data.length);
		
//		/////////////////////
//		StringBuilder sb = new StringBuilder();
//		System.out.println("검사 대상 data: ");
//	    for(final byte b: data)
//	        sb.append(String.format("%02x ", b&0xff));
//	    System.out.println(sb);
//	    System.out.println((int)LLC.getCRC32Value(data));
 	/////////////////////
		// TODO: 4. CRC set
		// CRC앞까지의 바이트 길이
		buffer.putInt((int)LLC.getCRC32Value(data));
		
		// 버퍼 위치 재설정.
		buffer.position(0);
		
		data = buffer.array();
		data = Arrays.copyOfRange(data, 0, INFORMATION_INDEX + message.length() + 4);
		
	}
	
	// 패킷으로부터 메시지를 뽑아냄.
	public String extractMessage(){
		// PDU로부터 전체 데이터 크기를 얻어낸다.
		int total_size = LLC.getDataSize(data);
		int information_size = total_size - INFORMATION_INDEX - 4;
		
		// information 부분의 바이트배열을 임시로 옮겨둘 바이트배열.
		byte[] bytes_tmp = new byte[information_size];
		System.arraycopy(data, INFORMATION_INDEX, bytes_tmp, 0, information_size);
		
		String message = new String(bytes_tmp);
		
		return message;
	}
	
	// 패킷으로부터 시퀀스를 뽑아냄.
	public int extarctSequence(){
		return data[CONTROL_INDEX];
	}
	
	@Override
	public String byteArrayToHex() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[IFrame]");
	    for(int i = 0  ; i < this.getDataSize(data); i++){
	    	if(i == DESTINATION_ADDRESS_INDEX){
	    		sb.append("(DESTINATION_ADDRESS)");
	    	}
 		else if (i == SOURCE_ADDRESS_INDEX){
 			sb.append("(SOURCE_ADDRESS)");
 		}
			else if (i == PDU_INDEX){
				sb.append("(PDU)");
 		}
			else if (i == SAP_INDEX){
				sb.append("(SAP)");
 		}
			else if (i == CONTROL_INDEX){
				sb.append("(CONTROL)");
			}
	    	// information이 없는 경우의 처리.
			else if (i == CONTROL_INDEX + control_size && CONTROL_INDEX + control_size < this.getDataSize(data) - 4){			
				sb.append("(INFORMATION)");
			} else if (i == this.getDataSize(data) - 4){
				sb.append("(CRC)");
			}
	    	sb.append(String.format("%02x ", data[i]&0xff));
	    }
	        
	    return sb.toString();
	}
}