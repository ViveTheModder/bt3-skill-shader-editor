package cmd;
//Little Endian class by ViveTheModder
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LittleEndian 
{
	public static boolean isFloat(float data)
	{
		if (data==0) return true;
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.asFloatBuffer().put(data);
		
		byte[] arr = bb.array();
		if (arr[0]>=0xB9 && arr[0]<=0xCB) return true; //negative float
		else if (arr[0]>=0x39 && arr[0]<=0x4B) return true; //positive float
		return false;
	}
	public static boolean isFloat(byte[] data)
	{
		if (data[0]==0 && data[1]==0 && data[2]==0 && data[3]==0) return true; //I regret writing this
		if (data[3]>=0xB9 && data[3]<=0xCB) return true; //negative float
		else if (data[3]>=0x39 && data[3]<=0x4B) return true; //positive float
		return false;
	}
	public static float getFloat(float data)
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.asFloatBuffer().put(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	public static short getShort(short data)
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.asShortBuffer().put(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}
}
