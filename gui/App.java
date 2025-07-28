package gui;
//BT3 Skill Shader Editor v1.1, written by ViveTheModder (Tribute to Maycon)
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.Main;
import cmd.Shader;

public class App 
{
	private static boolean openColorChooser=false, openFolderDialog=false;
	private static int shaderType=-1;
	private static File lastFile, lastFolder;
	private static Shader shader=null;
	private static JButton[] colorBtns;
	private static JTextField[] opacityFields;
	private static final Dimension BUTTON_SIZE = new Dimension(96,32);
	private static final Dimension FIELD_SIZE = new Dimension(96,32);
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font BOLD_S = new Font("Tahoma", 1, 12);
	private static final Font MED = new Font("Tahoma", 0, 14);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "BT3 Skill Shader Editor v1.1";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	
	private static boolean autoSkipOdd(Shader[] shaders) throws IOException
	{
		int shaderNum=0;
		for (Shader sh: shaders)
		{
			int shaderType = sh.getFileType();
			String shaderName = sh.getFileName();
			shaderNum = Integer.parseInt(shaderName.split("_")[0]);
			if (shaderNum==3) continue; //skip 03_.dat since it is NOT a shader
			if (shaderType==0)
			{
				float[] rgbData = sh.getRgbDataFromDat();
				for (int i=0; i<rgbData.length; i++)
					if (rgbData[i]<0) break;
			}
			else
			{
				int[] rgbData = sh.getRgbDataFromV00();
				for (int i=0; i<rgbData.length; i++)
					if (rgbData[i]<0) break;
			}
		}
		if (shaderNum%2!=0) return true;
		return false;
	}
	private static float[] getRgbDataFromDatGUI()
	{
		float[] rgbData = new float[colorBtns.length*4];
		for (int i=0; i<colorBtns.length; i++)
		{
			Color btnClr = colorBtns[i].getBackground();
			rgbData[4*i] = btnClr.getRed();
			rgbData[4*i+1] = btnClr.getGreen();
			rgbData[4*i+2] = btnClr.getBlue();
			rgbData[4*i+3] = Float.parseFloat(opacityFields[i].getText());
		}
		return rgbData;
	}
	private static int[] getRgbDataFromV00GUI()
	{
		int[] rgbData = new int[colorBtns.length*4];
		for (int i=0; i<colorBtns.length; i++)
		{
			Color btnClr = colorBtns[i].getBackground();
			rgbData[4*i] = btnClr.getRed();
			rgbData[4*i+1] = btnClr.getGreen();
			rgbData[4*i+2] = btnClr.getBlue();
			rgbData[4*i+3] = Integer.parseInt(opacityFields[i].getText());
		}
		return rgbData;
	}
	private static JButton[] getColorBtnsFromRgbData(float[] rgbData)
	{
		JButton[] colorBtns = new JButton[rgbData.length/4];
		for (int i=0; i<colorBtns.length; i++)
		{
			colorBtns[i] = new JButton("Color "+(i+1));
			int[] rgb = {(int)rgbData[4*i], (int)rgbData[4*i+1], (int)rgbData[4*i+2]};
			for (int j=0; j<3; j++) //prevent color from exceeding expected range
			{
				if (rgb[j]>255) rgb[j]=255; //handle overflow
				else if (rgb[j]<0) return null; //handle underflow
			}
			Color clr = new Color(rgb[0],rgb[1],rgb[2]);
			colorBtns[i].setBackground(clr);
			if (colorBtns[i].getBackground().equals(Color.WHITE)) colorBtns[i].setForeground(Color.BLACK); 
			else colorBtns[i].setForeground(Color.WHITE);
			colorBtns[i].setBorderPainted(false);
			colorBtns[i].setMaximumSize(BUTTON_SIZE);
			colorBtns[i].setMinimumSize(BUTTON_SIZE);
			colorBtns[i].setPreferredSize(BUTTON_SIZE);
		}
		return colorBtns;
	}
	private static JButton[] getColorBtnsFromRgbData(int[] rgbData)
	{
		JButton[] colorBtns = new JButton[rgbData.length/4];
		for (int i=0; i<colorBtns.length; i++)
		{
			colorBtns[i] = new JButton("Color "+(i+1));
			int[] rgb = {(int)rgbData[4*i], (int)rgbData[4*i+1], (int)rgbData[4*i+2]};
			Color clr = new Color(rgb[0],rgb[1],rgb[2]);
			colorBtns[i].setBackground(clr);
			if (colorBtns[i].getBackground().equals(Color.WHITE)) colorBtns[i].setForeground(Color.BLACK); 
			else colorBtns[i].setForeground(Color.WHITE);
			colorBtns[i].setBorderPainted(false);
			colorBtns[i].setMaximumSize(BUTTON_SIZE);
			colorBtns[i].setMinimumSize(BUTTON_SIZE);
			colorBtns[i].setPreferredSize(BUTTON_SIZE);
		}
		return colorBtns;
	}
	private static JTextField[] getOpacityFieldsFromRgbData(float[] rgbData)
	{
		JTextField[] textFields = new JTextField[rgbData.length/4];
		for (int i=0; i<textFields.length; i++)
		{
			final int index=i;
			textFields[i] = new JTextField(rgbData[4*i+3]+"");
			textFields[i].setMaximumSize(FIELD_SIZE);
			textFields[i].setMinimumSize(FIELD_SIZE);
			textFields[i].setPreferredSize(FIELD_SIZE);
			textFields[i].addKeyListener(new KeyAdapter()
			{
				public void keyTyped(KeyEvent e)
				{
					char ch = e.getKeyChar();
					String text = textFields[index].getText();
					if (text.length()>5)
					{
						if (!(ch==KeyEvent.VK_DELETE || ch==KeyEvent.VK_BACK_SPACE))
							e.consume();
					}
					if (!(ch>='0' && ch<='9')) e.consume();
				}
			});
		}
		return textFields;
	}
	private static JTextField[] getOpacityFieldsFromRgbData(int[] rgbData)
	{
		JTextField[] textFields = new JTextField[rgbData.length/4];
		for (int i=0; i<textFields.length; i++)
		{
			final int index=i;
			textFields[i] = new JTextField(rgbData[4*i+3]+"");
			textFields[i].setMaximumSize(FIELD_SIZE);
			textFields[i].setMinimumSize(FIELD_SIZE);
			textFields[i].setPreferredSize(FIELD_SIZE);
			textFields[i].addKeyListener(new KeyAdapter()
			{
				public void keyTyped(KeyEvent e)
				{
					char ch = e.getKeyChar();
					String text = textFields[index].getText();
					if (text.length()>5)
					{
						if (!(ch==KeyEvent.VK_DELETE || ch==KeyEvent.VK_BACK_SPACE))
							e.consume();
					}
					if (!(ch>='0' && ch<='9')) e.consume();
				}
			});
		}
		return textFields;
	}
	private static Shader getShaderFromChooser() throws IOException
	{
		Shader sh=null;
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter datFilter = new FileNameExtensionFilter("BT3 Skill Shader (.DAT)", "dat");
		FileNameExtensionFilter v00Filter = new FileNameExtensionFilter("BT3 Skill Shader (.V00)", "v00");
		chooser.addChoosableFileFilter(datFilter);
		chooser.addChoosableFileFilter(v00Filter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Open Skill Shader...");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(datFilter);
		if (lastFile!=null) chooser.setCurrentDirectory(lastFile);
		while (sh==null)
		{
			int result = chooser.showOpenDialog(null);
			if (result==0)
			{
				File src = chooser.getSelectedFile();
				lastFile = src;
				Shader tmp = new Shader(src);
				shaderType = tmp.getFileType();
				if (shaderType!=-1) sh=tmp;
				else
				{
					errorBeep();
					JOptionPane.showMessageDialog(null, "Invalid skill shader!", WINDOW_TITLE, 0);
				}
			}
			else break;
		}
		return sh;
	}
	private static Shader[] getShadersFromChooser() throws IOException
	{
		Shader[] shaders=null;
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select Folder with Skill Shader Files...");
		if (lastFolder!=null) chooser.setCurrentDirectory(lastFolder);
		while (shaders==null)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				File tmpFolder = chooser.getSelectedFile();
				lastFolder=tmpFolder;
				File[] tmpFiles = tmpFolder.listFiles((dir, name) -> 
				name.toLowerCase().endsWith(".dat") || name.toLowerCase().contains(".v00"));
				if (tmpFiles.length>0)
				{
					shaders = new Shader[tmpFiles.length];
					for (int i=0; i<tmpFiles.length; i++) shaders[i] = new Shader(tmpFiles[i]);
				}
				else
				{
					errorBeep();
					JOptionPane.showMessageDialog(null, "Directory does NOT contain skill shaders!", WINDOW_TITLE, 0);
				}
			}
			else return null;
		}
		return shaders;
	}
	private static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static void setApp()
	{
		//initialize components
		GridBagConstraints gbc = new GridBagConstraints();
		JFrame frame = new JFrame();
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu folderMenu = new JMenu("Folder");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem open = new JMenuItem("Open Skill Shader...");
		JMenuItem save = new JMenuItem("Save Skill Shader...");
		JMenuItem copy = new JMenuItem("Copy Shader Color...");
		JMenuItem swap = new JMenuItem("Swap Shader Colors...");
		JMenuItem about = new JMenuItem("About");
		JPanel panel = new JPanel(new GridLayout(0,4));
		//set component properties
		frame.setLayout(new GridBagLayout());
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		//add action listeners
		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					panel.removeAll();
					shader = getShaderFromChooser();
					frame.setTitle(WINDOW_TITLE+" - "+shader.getFileName());
					if (shaderType==0)
					{
						float[] rgbData = shader.getRgbDataFromDat();
						colorBtns = getColorBtnsFromRgbData(rgbData);
						opacityFields = getOpacityFieldsFromRgbData(rgbData);
					}
					else
					{
						int[] rgbData = shader.getRgbDataFromV00();
						colorBtns = getColorBtnsFromRgbData(rgbData);
						opacityFields = getOpacityFieldsFromRgbData(rgbData);
					}
					if (colorBtns!=null)
					{
						for (int i=0; i<colorBtns.length; i++) 
						{
							final int index=i;
							//add action listeners for color chooser
							colorBtns[i].addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e) 
								{
									if (!openColorChooser) setColorDialog(colorBtns[index]);
								}
							});
							//add buttons and fields to panel
							Box box = Box.createHorizontalBox();
							box.add(colorBtns[i]);
							box.add(opacityFields[i]);
							box.add(Box.createHorizontalStrut(16));
							panel.add(box);
						}
					}
					else
					{
						errorBeep();
						JOptionPane.showMessageDialog(null, "Invalid skill shader!", WINDOW_TITLE, 0);
					}
					frame.revalidate();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		save.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					//new instance of the same shader is made so that the new stream is closed rather than the old one
					if (shaderType==0)
					{
						float[] rgbData = getRgbDataFromDatGUI();
						Shader newShader = new Shader(shader.getRefFile());
						newShader.writeRgbDataToDat(rgbData);
					}
					else
					{
						int[] rgbData = getRgbDataFromV00GUI();
						Shader newShader = new Shader(shader.getRefFile());
						newShader.writeRgbDataToV00(rgbData);
					}
					DEF_TOOLKIT.beep();
					JOptionPane.showMessageDialog(null, "Changes to skill shader have been saved!", WINDOW_TITLE, 1);
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		copy.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (!openFolderDialog) setFolderDialog(true);
			}
		});
		swap.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (!openFolderDialog) setFolderDialog(false);
			}
		});
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Box[] boxes = new Box[3];
				Box mainBox = Box.createVerticalBox();
				String[] authorLinks = {"https://github.com/ViveTheModder","https://www.youtube.com/@dbzmadeiradabr303","https://github.com/Vrass28"};
				String[] text = {"Made by: ","Greatly inspired by: ","Initial research by: "};
				JLabel[] authors = 
				{new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END), new JLabel(HTML_A_START+"Maycon"+HTML_A_END), new JLabel(HTML_A_START+"Vras"+HTML_A_END)};
			
				for (int i=0; i<authors.length; i++)
				{
					final int index=i;
					boxes[i] = Box.createHorizontalBox();
					JLabel textLabel = new JLabel(text[i]);
					textLabel.setFont(BOLD_S);
					authors[i].setFont(BOLD_S);
					boxes[i].add(textLabel);
					boxes[i].add(authors[i]);
					authors[i].addMouseListener(new MouseAdapter() 
					{
						@Override
						public void mouseClicked(MouseEvent e) 
						{
							try 
							{
								Desktop.getDesktop().browse(new URI(authorLinks[index]));
							} 
							catch (IOException | URISyntaxException e1) 
							{
								e1.printStackTrace();
							}
						}
					});
					mainBox.add(boxes[i]);
				}
				JOptionPane.showMessageDialog(null, mainBox, WINDOW_TITLE, 1);
			}
		});
		//add components
		fileMenu.add(open);
		fileMenu.add(save);
		folderMenu.add(copy);
		folderMenu.add(swap);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(folderMenu);
		menuBar.add(helpMenu);
		frame.add(panel);		
		//set frame properties
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setSize(768,512);
		frame.setTitle(WINDOW_TITLE);
		frame.setVisible(true);
	}
	private static void setColorDialog(JButton btn)
	{
		openColorChooser=true;
		//initialize components
		GridBagConstraints gbc = new GridBagConstraints();
		JColorChooser clrChooser = new JColorChooser(btn.getBackground());
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel(new GridBagLayout());
		//set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		//add listeners
		clrChooser.getSelectionModel().addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				Color newClr = clrChooser.getColor();
		        btn.setBackground(newClr);
			}
		});
		dialog.addWindowListener(new WindowAdapter()
		{
			 @Override
		     public void windowClosing(WindowEvent e) 
		     {
				 openColorChooser=false;
			 }
		});
		//add components
		panel.add(clrChooser);
		dialog.add(panel);
		//set dialog properties
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.setSize(768,512);
		dialog.setTitle("Pick a Color...");
		dialog.setVisible(true);
	}
	private static void setFolderDialog(boolean copy)
	{
		openFolderDialog=true;
		String action="swap";
		if (copy) action="copy";
		Main.inColorIdx=0; Main.outColorIdx=0;
		//initialize components
		Box inBox = Box.createHorizontalBox();
		Box outBox = Box.createHorizontalBox();
		ButtonGroup[] btnGroups = {new ButtonGroup(), new ButtonGroup()};
		GridBagConstraints gbc = new GridBagConstraints();
		JButton applyBtn = new JButton("Apply "+action.toUpperCase()+" Procedure");
		JDialog dialog = new JDialog();
		JLabel inLbl = new JLabel("Input Color:");
		JLabel outLbl = new JLabel("Output Color:");
		JPanel panel = new JPanel(new GridBagLayout());
		JRadioButton[] radioBtns = new JRadioButton[6];
		//set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		inLbl.setFont(BOLD);
		outLbl.setFont(BOLD);
		for (int i=0; i<radioBtns.length; i++)
		{
			final int index=i;
			radioBtns[i] = new JRadioButton(Main.COLORS[i%3]);
			radioBtns[i].setFont(MED);
			radioBtns[i].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if (index%2==0) Main.outColorIdx = index%3;
					else Main.inColorIdx = index%3;
				}
			});
			btnGroups[i/3].add(radioBtns[i]);
			if (i%3==0) radioBtns[i].setSelected(true);
		}
		//add listeners
		applyBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					Shader[] shaders = getShadersFromChooser();
					if (shaders!=null) 
					{
						Main.skipOdd = autoSkipOdd(shaders);
						String numType="even";
						if (!Main.skipOdd) numType="odd";
						Main.write(shaders,Main.inColorIdx,Main.outColorIdx,copy);
						DEF_TOOLKIT.beep();
						JOptionPane.showMessageDialog(null, "Changes to "+numType+"ly numbered skill shaders have been saved!", WINDOW_TITLE, 1);
					}
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		dialog.addWindowListener(new WindowAdapter()
		{
			 @Override
		     public void windowClosing(WindowEvent e) 
		     {
				 openFolderDialog=false;
			 }
		});
		//add components
		panel.add(inLbl,gbc);
		for (int i=0; i<3; i++) inBox.add(radioBtns[i]);
		panel.add(inBox,gbc);
		panel.add(outLbl,gbc);
		for (int i=3; i<6; i++) outBox.add(radioBtns[i]);
		panel.add(outBox,gbc);
		panel.add(applyBtn,gbc);
		dialog.add(panel);
		//set dialog properties
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		dialog.setSize(256,256);
		dialog.setTitle("Pick Colors to "+action+"...");
		dialog.setVisible(true);
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setApp();
		} 
		catch (Exception e) 
		{
			errorBeep();
			JOptionPane.showMessageDialog(null, e.getClass().getSimpleName()+": "+e.getMessage(), "Exception", 0);		
		}
	}
}