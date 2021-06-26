package com.juaned;

import java.awt.AWTException;
import java.awt.BorderLayout;
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

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
	private static JPanel settingsPanel;
	private JLabel settingsPanelTitleLabel;
	private JLabel addTaskNameLabel;
	private JButton startTaskButton;
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
		startTaskButton = new JButton("Start Task");
		startTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("starting..");
				String getTaskText = currentTaskTextField.getText();
				if (!getTaskText.equals("")) {
					String command = String.format("/usr/local/bin/task add %s", getTaskText);
					Process proc = null;
					try {
						proc = Runtime.getRuntime().exec(command);
					} catch (IOException exception) {
						exception.printStackTrace();
					}

					// read the output, get task ID
					BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String taskOutput = reader.readLine();
					System.out.println(taskOutput);
				}

				// int exitVal = 0;
				// try {
				// exitVal = proc.waitFor();
				// } catch (InterruptedException e1) {
				// e1.printStackTrace();
				// }
				// if (exitVal == 0) {
				// System.out.println("Success!");
				// } else {
				// System.out.println("Failure");
				// }
				// System.out.println(command);
				// System.out.println(reader);
			}
		});

		settingsPanel.add(addTaskNameLabel);
		settingsPanel.add(currentTaskTextField);
		settingsPanel.add(startTaskButton);

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
