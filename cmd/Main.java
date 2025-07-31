package cmd;
//BT3 Skill Shader Editor v1.2, written by ViveTheModder (Tribute to Maycon)
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import gui.App;

public class Main 
{
	private static final String[] OPTIONS = {"Read Shader Files","Copy Shader Color (RGB)","Swap Shader Colors (RGB)"};
	public static final String[] COLORS = {"RED","GREEN","BLUE"};
	public static boolean skipOdd;
	public static int inColorIdx=-1, outColorIdx=-1;
	public static void read(Shader[] shaders) throws IOException
	{
		for (Shader sh: shaders)
		{
			String shaderName = sh.getFileName();
			int shaderType = sh.getFileType();
			int shaderNum = Integer.parseInt(shaderName.split("_")[0]);
			if (shaderNum==3 || shaderNum==5) continue; //03_.dat is NOT a skill shader file, but it does control them
			if (skipOdd == (shaderNum%2==0))
			{
				System.out.println("* "+sh.getFileName());
				if (shaderType==0) sh.printRgbDataFromDat(sh.getRgbDataFromDat());
				else if (shaderType==1) sh.printRgbDataFromV00(sh.getRgbDataFromV00());
				else System.out.println("Invalid skill shader!");
			}
		}
	}
	public static void readParams(Scanner sc, boolean write)
	{
		char answer=0;
		int[] indices = {-1,-1};
		String[] text = {"input","output"};
		if (write)
		{
			for (int i=0; i<2; i++)
			{
				while (indices[i]==-1)
				{
					System.out.print("Enter RED, GREEN or BLUE as the "+text[i]+" color: ");
					String input = sc.nextLine();
					for (int j=0; j<3; j++)
						if (COLORS[j].equals(input.toUpperCase())) indices[i]=j;
				}
			}
		}
		while (answer==0)
		{
			System.out.print("Should oddly numbered skill shaders be skipped (Y) or evenly numbered ones (N)? ");
			String input = sc.nextLine();
			answer = input.toCharArray()[0];
			if (answer=='Y') skipOdd=true;
			else if (answer=='N') skipOdd=false;
			else answer=0;
		}
		inColorIdx = indices[0];
		outColorIdx = indices[1];
	}
	public static void write(Shader[] shaders, int inColorIdx, int outColorIdx, boolean copy) throws IOException
	{
		int textIdx=0;
		String[] text = {"Replacing","Swapping"};
		for (Shader sh: shaders)
		{
			int shaderType = sh.getFileType();
			int shaderNum = Integer.parseInt(sh.getFileName().split("_")[0]);
			if (shaderNum==3) continue; //03_.dat is NOT a skill shader file, but it does control them
			if (shaderType==0) 
			{
				boolean autoSkipResult = skipOdd == (shaderNum%2==0);
				if (shaders.length==1) autoSkipResult = true;
				if (autoSkipResult)
				{
					if (!copy) textIdx=1; 
					System.out.println(text[textIdx]+" "+COLORS[outColorIdx]+" values with "+COLORS[inColorIdx]+" for "+sh.getFileName()+"...");
					if (copy) sh.writeRgbDataToDat(sh.copyShaderColor(sh.getRgbDataFromDat(), inColorIdx, outColorIdx));
					else sh.writeRgbDataToDat(sh.swapShaderColor(sh.getRgbDataFromDat(), inColorIdx, outColorIdx));
				}
			}
			else if (shaderType==1)
			{
				if (!copy) textIdx=1;
				System.out.println(text[textIdx]+" "+COLORS[outColorIdx]+" values with "+COLORS[inColorIdx]+" for "+sh.getFileName()+"...");
				if (copy) sh.writeRgbDataToV00(sh.copyShaderColor(sh.getRgbDataFromV00(), inColorIdx, outColorIdx));
				else sh.writeRgbDataToV00(sh.swapShaderColor(sh.getRgbDataFromV00(), inColorIdx, outColorIdx));
			}
		}
	}
	public static void main(String[] args) 
	{
		try
		{
			if (args.length>0 && args[0]=="-c")
			{
				int option=-1;
				Shader[] shaders=null;
				Scanner sc = new Scanner(System.in);
				while (shaders==null)
				{
					System.out.println("Enter a valid path to a skill shader OR a folder containing skill shaders:");
					String path = sc.nextLine();
					File tmp = new File(path);
					if (tmp.isFile())
					{
						String tmpName = tmp.getName().toLowerCase();
						if (tmpName.endsWith("dat") || tmpName.endsWith("v00")) 
						{
							shaders = new Shader[1];
							shaders[0] = new Shader(tmp);
						}
						else System.out.println("Not a skill shader! File extension must be DAT or V00.");
					}
					else if (tmp.isDirectory())
					{
						File[] tmpFiles = tmp.listFiles((dir, name) -> 
						name.toLowerCase().endsWith(".dat") || name.toLowerCase().contains(".v00"));
						if (tmpFiles.length>0)
						{
							shaders = new Shader[tmpFiles.length];
							for (int i=0; i<tmpFiles.length; i++) shaders[i] = new Shader(tmpFiles[i]);
						}
						else System.out.println("Directory does NOT contain skill shaders!");
					}
				}
				while (option==-1)
				{
					System.out.println("Enter an option number out of the following:");
					for (int i=0; i<OPTIONS.length; i++) System.out.println(i+". "+OPTIONS[i]);
					String input = sc.nextLine();
					if (input.matches("[0-"+(OPTIONS.length-1)+"]")) option = Integer.parseInt(input);
				}
				long start = System.currentTimeMillis();
				switch (option)
				{
					case 0: 
						if (shaders.length>1) readParams(sc,false);
						read(shaders);
						break;
					case 1: 
						if (shaders.length>1) readParams(sc,true);
						write(shaders,inColorIdx,outColorIdx,true); 
						break;
					case 2: 
						if (shaders.length>1) readParams(sc,true); 
						write(shaders,inColorIdx,outColorIdx,false); 
						break;
				}
				long finish = System.currentTimeMillis();
				System.out.println("Time: "+(finish-start)/1000.0+" s");
			}
			else App.main(args);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}