package project3;

import java.util.Arrays;

//연결.
public class UFrame extends LLC {
	public static int UNKNOWN = 0;
	public static int SABME = 1;
	public static int UA = 2;
	
	public UFrame(){}
	
	public UFrame(int code){
		control_size = 1;
		
		// 1. PDU 설정
		// 고정 -> 6(dest address) + 6(source address) + 2(PDU) + 3(SAP:2+control:1) + 4(CRC) = 20
		buffer.putShort(super.PDU_INDEX, (short)21);
		
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
		
		// 3. CRC 설정
		data = buffer.array();
		data = Arrays.copyOfRange(data, 0, CONTROL_INDEX + control_size);
		// CRC앞까지의 바이트 길이
		buffer.putInt(super.CONTROL_INDEX + control_size, (int)LLC.getCRC32Value(data));
		
		data = buffer.array();
		data = Arrays.copyOfRange(data, 0, CONTROL_INDEX + control_size + 4);
	}
	
	public int extractCode(){
		if(data[CONTROL_INDEX] == (byte)0xF6){
			return SABME;
		} else if(data[CONTROL_INDEX] == (byte)0xC6){
			return UA;
		} else{
			return UNKNOWN;
		}
	}

	@Override
	public String byteArrayToHex() {
		StringBuilder sb = new StringBuilder();
		sb.append("[UFrame]");
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
	    sb.append("[총 길이: " + this.getDataSize(data) + "]");
	    return sb.toString();
	}
}

