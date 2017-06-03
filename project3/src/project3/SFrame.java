package project3;

import java.util.Arrays;

//ACK/NACK
public class SFrame extends LLC {
	public static int UNKNOWN = 0;
	public static int RR = 1;
	public static int RNR = 2;
	public static int REJECT = 3;
	
	private int sequence;
	
	public SFrame(){}
	
	public SFrame(int code, int _sequence){
		// 시퀀스 설정.
		_sequence %= 127;
		
		// 1. PDU 설정
		// 고정 -> 6(dest address) + 6(source address) + 2(PDU) + 4(SAP:2+control:2) + 4(CRC)
		buffer.putShort(super.PDU_INDEX, (short)22);
			
		// 2. control 설정.
		//SS 설정.
		switch (code) {
		case 1:		// RR
			buffer.put(super.CONTROL_INDEX, (byte)0x80);
			break;
		case 2:		// RNR
			buffer.put(super.CONTROL_INDEX, (byte)0xA0);
			break;
		case 3:		// REJECT
			buffer.put(super.CONTROL_INDEX, (byte)0x90);
			break;
		default:
			// 이외의 경우에는 REJECT 처리.
			buffer.put(super.CONTROL_INDEX, (byte)0x90);
			break;
		}
		
		// N(R)설정.
		buffer.put(super.CONTROL_INDEX + 1, (byte)_sequence);
		
		// 3. CRC 설정 
		data = buffer.array();
		data = Arrays.copyOfRange(data, 0, CONTROL_INDEX + 2);
		
		// CRC앞까지의 바이트 길이
		buffer.putInt(super.CONTROL_INDEX + 2, (int)LLC.getCRC32Value(data));
		
		data = buffer.array();
		data = Arrays.copyOfRange(data, 0, CONTROL_INDEX + 2 + 4);
		
	}
	
	public int extractCode(){
		if(data[CONTROL_INDEX] == (byte)0x80){
			return RR;
		} else if(data[CONTROL_INDEX] == (byte)0xA0){
			return RNR;
		} else if (data[CONTROL_INDEX] == (byte)0x90){
			return REJECT;
		} else {
			return UNKNOWN;
		}
	}

	@Override
	public String byteArrayToHex() {
		StringBuilder sb = new StringBuilder();
		sb.append("[SFrame]");
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
			else if (i == this.getDataSize(data) - 4){
				sb.append("(CRC)");
			}
	    	sb.append(String.format("%02x ", data[i]&0xff));
	    }
	        
	    return sb.toString();
	}
}

