import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainClass
{
	
	static JLabel currentTimeLabel;
	
	public static void main(String[] args)
	{
		
		String backupLogs = "C:\\Program Files\\CZM\\SQATestApp\\Logs\\backup-restore.log";
		
		String defaultSettings = "C:\\Program Files\\CZM\\SQATestApp\\Configuration\\default.settings.xml";
		String defaultSettingsBefore = "<setting name=\"CompressFullBackup\"   type=\"System.Boolean\"   encrypted=\"false\" readonly=\"false\" scope=\"application\" description=\"Compress full backup\" >";
		String defaultSettingsAfter = "</setting>";
		String defaultSettingsLine = "";
		String compressionSettings = "";
		
		defaultSettingsLine = searchString(defaultSettings, defaultSettingsBefore);
		compressionSettings = getStringBetween(defaultSettingsLine, defaultSettingsBefore, defaultSettingsAfter);
		
		String filePhrase = "Backup Type will be :";
		String line = "";
		line = searchString(backupLogs, filePhrase);
		
		JFrame frame = new JFrame("Backup Type Reader");
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400,400);
		
		try 
		{
			frame.setIconImage(ImageIO.read(new File("res/zeiss-logo.png")));
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu saveMenu = new JMenu("Save");
		JMenu clearMenu = new JMenu("Clear");
		JMenu helpMenu = new JMenu("Help");
		
		menuBar.add(saveMenu);
		menuBar.add(clearMenu);
		menuBar.add(helpMenu);
		
		JMenuItem clearLogs = new JMenuItem("Clear Logs");
		JMenuItem clearDbFiles = new JMenuItem("Clear DB Files");
		JMenuItem clearBackup = new JMenuItem("Clear Backup Files");
		JMenuItem zipLogs = new JMenuItem("Save Logs");
		JMenuItem about = new JMenuItem("About");
		
		saveMenu.add(zipLogs);
		clearMenu.add(clearLogs);
		clearMenu.add(clearDbFiles);
		clearMenu.add(clearBackup);
		helpMenu.add(about);		

		zipLogs.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed (ActionEvent e) 
			{
				File f = new File("C:\\Program Files\\CZM\\SQATestApp\\Logs");
				if (f.exists())
				{
					new ZipUtils().run();
					alert("Logs Saved to Desktop");
				}
				else
				{
					alert("Logs Not Found");
				}
			}
		});
		
		clearLogs.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed (ActionEvent e) 
			{
				File f = new File("C:\\Program Files\\CZM\\SQATestApp\\Logs");
				if (f.exists())
				{
					deleteDir(f);
					alert("Logs Deleted");
				}
				else
				{
					alert("Logs Not Found");
				}
			}
		});
		
		clearDbFiles.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed (ActionEvent e) 
			{
				deleteDir(new File("C:\\db.default"));
				deleteDir(new File("C:\\db.usermanagement"));
				alert("DB Files Deleted");
			}

		});

		clearBackup.addActionListener(new ActionListener() 
		{

			@Override
			public void actionPerformed (ActionEvent e) 
			{
				deleteDir(new File(getBackupTargetPath() + "\\133221"));
				deleteDir(new File(getBackupTargetPath() + "\\Copy_133221"));
				alert("Backup Deleted");
			}
		    
			public String getBackupTargetPath() 
			{
				String backupTargetPath = "";

				String applicationSettings = "C:\\Program Files\\CZM\\SQATestApp\\Settings\\application.settings.xml";
				String applicationSettingsBefore = "<setting name=\"BackupTargetPath\" type=\"System.String\" encrypted=\"false\" readonly=\"false\" scope=\"application\">";
				String applicationSettingsAfter = "</setting>";
				String applicationSettingsLine = "";

				applicationSettingsLine = searchString(applicationSettings, applicationSettingsBefore);
				backupTargetPath = getStringBetween(applicationSettingsLine, applicationSettingsBefore, applicationSettingsAfter);

				if (backupTargetPath.isEmpty()) 
				{
					String defaultTargetPath = "C:\\Program Files\\CZM\\SQATestApp\\Configuration\\default.settings.xml";
					String defaultTargetPathBefore = "<setting name=\"BackupTargetPath\" type=\"System.String\" encrypted=\"false\" readonly=\"false\" scope=\"application\" description=\"Target drive for the backup\">";
					String defaultTargetPathAfter = "</setting>";

					applicationSettingsLine = searchString(defaultTargetPath, defaultTargetPathBefore);
					backupTargetPath = getStringBetween(applicationSettingsLine, defaultTargetPathBefore, defaultTargetPathAfter);
				}

				if (backupTargetPath.endsWith("\\")) 
				{
					backupTargetPath = backupTargetPath.substring(0, backupTargetPath.length() - 1);
				}

				return backupTargetPath;

			}
		});
		
		about.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed (ActionEvent e) {
				alert("BackupTypeReader\nVersion 1.0\n8/1/2019\nBy Mark Mandocdoc");
			}
		});
		
		JButton button1 = new JButton("Refresh");
		button1.setFocusPainted(false);
		
		JPanel mainPanel = new JPanel(new GridLayout(0,1));
		
		JPanel compressionPanel = new JPanel();
		compressionPanel.setBorder(
				new CompoundBorder(
						new EmptyBorder(10, 10, 10, 10), 
						BorderFactory.createTitledBorder("Compression")
						)
				);

		JLabel compressionLabel = new JLabel(compressionSettings, SwingConstants.CENTER);
		compressionPanel.add(compressionLabel);
		
		JPanel currentTimePanel = new JPanel(new GridLayout(0,3));
		currentTimePanel.setBorder(
				new CompoundBorder(
						new EmptyBorder(10, 10, 10, 10), 
						BorderFactory.createTitledBorder("Current Time")
						)
				);
		
		JLabel decreaseDateLabel = new JLabel("<", SwingConstants.CENTER);
		decreaseDateLabel.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				addToDate(-1);
			}
		});

		currentTimePanel.add(decreaseDateLabel);
		
		currentTimeLabel = new JLabel("Loading", SwingConstants.CENTER);
		currentTimePanel.add(currentTimeLabel);
		
		JLabel increaseDateLabel = new JLabel(">", SwingConstants.CENTER);
		increaseDateLabel.addMouseListener(new MouseAdapter() 
		{
	        	public void mouseClicked(MouseEvent e) 
			{
				addToDate(1);
			}
		});

		currentTimePanel.add(increaseDateLabel);
		
		JPanel backupTimePanel = new JPanel();
		backupTimePanel.setBorder(
				new CompoundBorder(
						new EmptyBorder(10, 10, 10, 10), 
						BorderFactory.createTitledBorder("Recent Backup Time")
						)
				);
		
		JLabel backupTimeLabel = new JLabel(getBackupTime(line), SwingConstants.CENTER);
		backupTimePanel.add(backupTimeLabel);
		
		JPanel backupTypePanel = new JPanel();
		backupTypePanel.setBorder(
				new CompoundBorder(
						new EmptyBorder(10, 10, 10, 10), 
						BorderFactory.createTitledBorder("Recent Backup Type")
						)
				);
		
		JLabel backupTypeLabel = new JLabel(getBackupType(line,filePhrase), SwingConstants.CENTER);
		backupTypePanel.add(backupTypeLabel);
		
		mainPanel.add(compressionPanel);
		mainPanel.add(currentTimePanel);
		mainPanel.add(backupTimePanel);
		mainPanel.add(backupTypePanel);
		
		frame.getContentPane().add(BorderLayout.NORTH, menuBar);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setVisible(true);
		
		Runnable helloRunnable = new Runnable() 
		{
			public void run() 
			{
	
				updateCurrentTime();
				String line = searchString(backupLogs, filePhrase);
				String defaultSettingsLine = searchString(defaultSettings, defaultSettingsBefore);
				
				backupTimeLabel.setText(getBackupTime(line));
				backupTypeLabel.setText(getBackupType(line, filePhrase));
				compressionLabel.setText(getStringBetween(defaultSettingsLine, defaultSettingsBefore, defaultSettingsAfter));
		        
			}
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);
		
	}
	
	public static void alert(String str)
	{
		JOptionPane.showMessageDialog(null, str);
	}
    
	public static boolean deleteDir(File dir) 
	{

		if (dir.isDirectory()) 
		{
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) 
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) 
				{
					return false;
				}

			}

		}

		return dir.delete();
	
	}
	
	public static void updateCurrentTime()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss YY.MM.dd");
		currentTimeLabel.setText(LocalDateTime.now().format(formatter));
	}
	
	public static String getStringBetween(String line, String before, String after)
	{
		if (line.isEmpty() || line.isBlank()) return "";
		line = line.substring(line.indexOf(before) + 1);
		line = line.substring(0, line.indexOf(after));
		line = line.substring(line.lastIndexOf(before) + before.length());
		return line;
	}
	
	public static String getBackupTime(String line)
	{
		if (line.isEmpty()) return "";
		String before = "Date:";
		String after = "INFORMATION";

		line = line.substring(line.indexOf(before) + 1);
		line = line.substring(0, line.indexOf(after));
		line = line.substring(line.lastIndexOf(before) + before.length());
		return line;
	}
	
	public static String getBackupType(String line, String filePhrase)
	{
		if (line.isEmpty()) return "";
		line = line.substring(line.lastIndexOf(filePhrase) + filePhrase.length());
		return line;
	}

	public static String searchString(String fileName, String phrase)
	{
		
		if (new File(fileName).exists() == false) return "";
		
		String foundText = "";
		
		BufferedReader Reader;
		try {

			if (fileName.indexOf(".log") >= 0) Reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-16"));
			else Reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line = null;
			Pattern pattern =  Pattern.compile(phrase);
			Matcher matcher = null;
				
			while ((line = Reader.readLine()) != null)
			{
				matcher = pattern.matcher(line);
				if (matcher.find())
				{
					foundText = line;
				}
			}

			Reader.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return foundText;
		
	}
	
	public static void addToDate(int val)
	{
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date currentDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, val);
		Date currentDatePlusOne = c.getTime();
		changeDateTo(dateFormat.format(currentDatePlusOne));
		updateCurrentTime();
	}
	
	public static void changeDateTo(String date)
	{
		try 
		{
			Runtime.getRuntime().exec("cmd /C date " + date);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
}