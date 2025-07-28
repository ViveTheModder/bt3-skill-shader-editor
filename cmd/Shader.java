package cmd;
//BT3 Skill Shader Class, written by ViveTheModder
import java.io.File;
//BT3 Skill Shader Class by ViveTheModder
import java.io.IOException;
import java.io.RandomAccessFile;

public class Shader 
{
	private File ref;
	private RandomAccessFile bin;
	private String fileName;
	private final String[] TEXT = {"RED","GREEN","BLUE","ALPHA"};

	public Shader(File f) throws IOException
	{
		ref = f;
		bin = new RandomAccessFile(f,"rw");
		fileName = f.getName();
	}
	public File getRefFile()
	{
		return ref;
	}
	public int[] copyShaderColor(int[] rgbData, int inColorIdx, int outColorIdx)
	{
		for (int i=0; i<rgbData.length; i+=4)
			rgbData[i+outColorIdx] = rgbData[i+inColorIdx];
		return rgbData;
	}
	public int[] getRgbDataFromV00() throws IOException
	{
		byte[] input = new byte[4];
		int fileSize = (int)bin.length();
		bin.seek(8);
		int pos = LittleEndian.getShort(bin.readShort());
		bin.seek(pos);
		
		int[] data = new int[4*((fileSize-pos)/64)]; //each color has 64 bytes of data, but only 4 are relevant (RGBA)
		int dataCnt=0;
		while (pos<fileSize)
		{
			pos+=48;
			bin.seek(pos);
			bin.read(input);
			for (int i=0; i<4; i++) //convert byte to unsigned byte
			{
				data[dataCnt] = (input[i] & 0xFF);
				dataCnt++;
			}
			pos+=16;
		}
		return data;
	}
	public int[] swapShaderColor(int[] rgbData, int inColorIdx, int outColorIdx)
	{
		for (int i=0; i<rgbData.length; i+=4)
		{
			int tmp = rgbData[i+inColorIdx];
			rgbData[i+inColorIdx] = rgbData[i+outColorIdx];
			rgbData[i+outColorIdx] = tmp;
		}
		return rgbData;
	}
	public float[] copyShaderColor(float[] rgbData, int inColorIdx, int outColorIdx)
	{
		for (int i=0; i<rgbData.length; i+=4)
			rgbData[i+outColorIdx] = rgbData[i+inColorIdx];
		return rgbData;
	}
	public float[] getRgbDataFromDat() throws IOException
	{
		int numFloats = (int)(bin.length()/4);
		float[] data = new float[numFloats];
		bin.seek(0);
		for (int i=0; i<numFloats; i++)
		{
			data[i] = LittleEndian.getFloat(bin.readFloat());
			if (!LittleEndian.isFloat(data[i])) data[i]=-1;
		}
		return data;
	}
	public float[] swapShaderColor(float[] rgbData, int inColorIdx, int outColorIdx)
	{
		for (int i=0; i<rgbData.length; i+=4)
		{
			float tmp = rgbData[i+inColorIdx];
			rgbData[i+inColorIdx] = rgbData[i+outColorIdx];
			rgbData[i+outColorIdx] = tmp;
		}
		return rgbData;
	}
	//0 -> DAT, 1 -> V00, -1 -> INVALID
	public int getFileType() throws IOException
	{
		byte[] input = new byte[4];
		int fileType=-1, floatCnt=0;
		int fileSize = (int)bin.length();
		bin.seek(0);
		
		for (int pos=0; pos<fileSize; pos+=4)
		{
			bin.read(input);
			if (pos==0)
			{
				String header = new String(input);
				if (header.equals("V000")) return 1;
			}
			if (LittleEndian.isFloat(input)) floatCnt++;
		}
		if (floatCnt>0) fileType=0;
		return fileType;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void printRgbDataFromDat(float[] rgbData) throws IOException
	{
		for (int i=0; i<TEXT.length; i++) System.out.printf("%9s ", TEXT[i]);
		System.out.println();
		for (int i=0; i<rgbData.length; i++)
		{
			if (i!=0 && i%4==0) System.out.println();
			System.out.printf("%9.5f ",rgbData[i]);
		}
		System.out.println();
	}
	public void printRgbDataFromV00(int[] rgbData) throws IOException
	{
		for (int i=0; i<TEXT.length; i++) System.out.printf("%5s ", TEXT[i]);
		System.out.println();
		for (int i=0; i<rgbData.length; i++)
		{
			if (i!=0 && i%4==0) System.out.println();
			System.out.printf("%5d ",rgbData[i]);
		}
		System.out.println();
	}
	public void writeRgbDataToDat(float[] rgbData) throws IOException
	{
		bin.seek(0);
		for (int i=0; i<rgbData.length; i++)
		{
			if (rgbData[i]!=-1)
				bin.writeFloat(LittleEndian.getFloat(rgbData[i]));
		}
		bin.close();
	}
	public void writeRgbDataToV00(int[] rgbData) throws IOException
	{
		int dataCnt=0;
		int fileSize = (int)bin.length();
		bin.seek(8);
		int pos = LittleEndian.getShort(bin.readShort());
		bin.seek(pos);
		
		while (pos<fileSize)
		{
			pos+=48;
			bin.seek(pos);
			for (int i=0; i<4; i++)
			{
				bin.writeByte(rgbData[dataCnt]);
				dataCnt++;
			}
			pos+=16;
		}
		bin.close();
	}
}