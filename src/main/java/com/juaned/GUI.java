package com.juaned;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public static GUI app;
	public static ProcessingSketch myProcessingSketch;
	public static TaskInfo task_info;
	public static SunInfo sun_info;
	public static JFrame processingFrame;

	/* MENU */
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem saveMenuItem;
	private JFileChooser fileChooser;
	private JMenu viewMenu;
	private JMenuItem viewMenu_ShowSettings;
	private boolean settingsPanelVisible;

	/* User Task Settings */
	private static String taskID;
	private static JPanel settingsPanel;
	private static JPanel savedTaskPanel;
	private JLabel settingsPanelTitleLabel;
	private JLabel addTaskNameLabel;
	private JButton startStopTaskButton;
	private JButton addNewSavedTask;
	private Boolean taskStarted;
	private static JTextField currentTaskTextField;

	public GUI() {
		setTitle("Task Visualizer");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		/* Initialize Processing Sketch */
		myProcessingSketch = new ProcessingSketch();
		myProcessingSketch.init();

		// needed on mac os x
		// System.setProperty("apple.laf.useScreenMenuBar", "true");

		menuBar = new JMenuBar();
		/* build the File menu */
		fileMenu = new JMenu("File");
		saveMenuItem = new JMenuItem("Save Image..");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame parentFrame = new JFrame();
				fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Specify a file to save");
				fileChooser.setSelectedFile(new File(String.format("%s.png", TaskInfo.taskDay)));
				int userSelection = fileChooser.showSaveDialog(parentFrame);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File fileToSave = fileChooser.getSelectedFile();
					System.out.println("Save as file: " + fileToSave.getAbsolutePath());
					myProcessingSketch.savePNG(fileToSave.getAbsolutePath());
				}
			}
		});
		fileMenu.add(saveMenuItem);

		/* build the View menu */
		viewMenu = new JMenu("View");
		viewMenu_ShowSettings = new JMenuItem("Show Task Settings");
		viewMenu_ShowSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (settingsPanelVisible == false) {
					settingsPanel.setVisible(true);
					settingsPanelVisible = true;
					viewMenu_ShowSettings.setText("Hide Task Settings");
				} else {
					settingsPanel.setVisible(false);
					settingsPanelVisible = false;
					viewMenu_ShowSettings.setText("Show Task Settings");
				}
				pack();
			}
		});
		viewMenu.add(viewMenu_ShowSettings);

		// add menus to menubar
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);

		// put the menubar on the frame
		setJMenuBar(menuBar);

		/* build the settings panel */
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS)); // TODO: Layout
		settingsPanel.setBackground(Color.GRAY);
		settingsPanel.setPreferredSize(new Dimension(400, 465));
		// settings panel labels //
		settingsPanelTitleLabel = new JLabel();
		settingsPanelTitleLabel.setText("Task Settings");
		settingsPanelTitleLabel.setForeground(Color.WHITE);
		settingsPanel.add(settingsPanelTitleLabel, BorderLayout.NORTH);

		addTaskNameLabel = new JLabel();
		addTaskNameLabel.setText("Add Task Name:");
		addTaskNameLabel.setForeground(Color.WHITE);
		currentTaskTextField = new JTextField(20);
		taskStarted = false;
		startStopTaskButton = new JButton("Start Task");
		startStopTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("adding task..");
				String getTaskText = currentTaskTextField.getText();
				if (!getTaskText.equals("") && !taskStarted) {
					String addTaskCommand = String.format("/usr/local/bin/task add %s", getTaskText.toUpperCase());
					Process proc = null;
					try {
						// add the task
						proc = Runtime.getRuntime().exec(addTaskCommand);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					// wait for the process to finish
					try {
						proc.waitFor();
					} catch (InterruptedException exception) {
						exception.printStackTrace();
					}
					// grab the ID of last task created
					String getLastTaskCommand = String.format("/usr/local/bin/task newest rc.verbose=nothing limit:1");
					proc = null;
					try {
						proc = Runtime.getRuntime().exec(getLastTaskCommand);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					// wait for the process to finish
					try {
						proc.waitFor();
					} catch (InterruptedException exception) {
						exception.printStackTrace();
					}
					// read the output, assign task ID to variable
					BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String taskOutput = "";
					try {
						taskOutput = reader.readLine();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.out.println(taskOutput);
					Pattern p = Pattern.compile("(^|\\s)([0-9]+)($|\\s)");
					Matcher m = p.matcher(taskOutput);
					if (m.find()) {
						System.out.println(m.group(2));
						taskID = m.group(2);
						// start task
						String startTaskCommand = String.format("/usr/local/bin/task %s start", taskID);
						System.out.println(startTaskCommand);
						proc = null;
						try {
							proc = Runtime.getRuntime().exec(startTaskCommand);
						} catch (IOException exception) {
							exception.printStackTrace();
						}
						// wait for the process to finish
						try {
							proc.waitFor();
						} catch (InterruptedException exception) {
							exception.printStackTrace();
						}
						// update boolean clear text field and change button text to Stop Task
						taskStarted = true;
						currentTaskTextField.setText("");
						currentTaskTextField.setEnabled(false);
						startStopTaskButton.setText("Pause Task");
					}
				} else if (taskStarted) {
					String stopTaskCommand = String.format("/usr/local/bin/task stop %s", taskID);
					Process proc = null;
					try {
						// start the task
						proc = Runtime.getRuntime().exec(stopTaskCommand);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					// wait for the process to finish
					try {
						proc.waitFor();
					} catch (InterruptedException exception) {
						exception.printStackTrace();
					}
					// update boolean and change button text to Start Task
					taskStarted = false;
					currentTaskTextField.setEnabled(true);
					startStopTaskButton.setText("Start Task");
				}
			}
		});

		savedTaskPanel = new JPanel();
		savedTaskPanel.setLayout(new GridLayout(3, 3));
		addNewSavedTask = new JButton("Add To Task List");
		addNewSavedTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userHomeDir = System.getProperty("user.home");
				File f = new File(userHomeDir + "/.task/");
				if (f.exists() && f.isDirectory()) {
					System.out.println("do stuff here");
				} else {
					JOptionPane.showMessageDialog(null, "TaskWarrior is not installed! Please read the README.md for full installation process");
				}


				// JPanel newPanel = new JPanel();
				// JLabel savedTaskName = new JLabel("Guitar");
				// JRadioButton j1 = new JRadioButton();
				// newPanel.add(savedTaskName);
				// newPanel.add(j1);
				// savedTaskPanel.add(newPanel);
				// settingsPanel.revalidate();
				// validate();
			}
		});

		settingsPanel.add(addTaskNameLabel);
		settingsPanel.add(savedTaskPanel);
		settingsPanel.add(addNewSavedTask);
		settingsPanel.add(currentTaskTextField);
		settingsPanel.add(startStopTaskButton);

		// make settingsPanel invisible at start
		settingsPanel.setVisible(false);

		add(GUI.myProcessingSketch, BorderLayout.WEST);
		add(settingsPanel, BorderLayout.EAST);

		pack();
		setVisible(true);

	}

	/*
	 * Main function.
	 */
	public static void main(String[] args) {
		task_info = new TaskInfo();
		sun_info = new SunInfo();
		app = new GUI();
		systemTrayMenu();
	}

	public static void systemTrayMenu() {
		InputStream is = app.getFileFromResourceAsStream("icon.png");
		BufferedImage imBuff = null;
		try {
			imBuff = ImageIO.read(is);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final PopupMenu popup = new PopupMenu();

		final TrayIcon trayIcon = new TrayIcon(imBuff);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		// Menu displayMenu = new Menu("Display");
		MenuItem showItem = new MenuItem("Show");
		showItem.setActionCommand("Show");
		MenuItem hideItem = new MenuItem("Hide");
		hideItem.setActionCommand("Hide");
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setActionCommand("Exit");

		MenuItemListener menuItemListener = new MenuItemListener();

		showItem.addActionListener(menuItemListener);
		hideItem.addActionListener(menuItemListener);
		exitItem.addActionListener(menuItemListener);

		// Add components to pop-up menu
		popup.add(showItem);
		popup.add(hideItem);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}
	}

	// get a file from the resources folder
	// works everywhere, IDEA, unit test and JAR file.
	private InputStream getFileFromResourceAsStream(String fileName) {

		// The class loader that loaded the class
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return inputStream;
		}

	}

	static class MenuItemListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "Show":
					System.out.println(e.getActionCommand());
					app.setVisible(true);
					break;
				case "Hide":
					System.out.println(e.getActionCommand());
					app.setVisible(false);
					break;
				case "Exit":
					System.out.println(e.getActionCommand());
					System.exit(0);
					break;
				default:
					System.out.println(e.getActionCommand());
					break;
			}
		}
	}

}
