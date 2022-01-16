package com.juaned;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.json.JSONException;

public class GUI extends JFrame {

  private static final long serialVersionUID = 1L;

  public static GUI app;
  public static ProcessingSketch myProcessingSketch;
  public static TaskInfo task_info;
  public static SunInfo sun_info;

  // Menu Bar
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenuItem saveMenuItem;
  private JMenuItem showSettingsItem;
  private JFileChooser fileChooser;
  private JMenu viewMenu;
  // private JMenuItem viewMenu_ShowSettings;
  private boolean settingsPanelVisible;

  // User Task Settings
  private static String taskID;
  private static String taskName;
  private static JPanel settingsPanel;
  private static JPanel savedTaskPanel;
  private static JPanel taskButtonsPanel;
  private static JPanel addNewSavedTaskPanel;
  private static JPanel currentTaskLabelPanel;
  private static JPanel currentTaskPanel;
  private static JPanel savedTasksLabelPanel;
  private static JScrollPane savedTaskScrollPane;
  private JLabel savedTasksLabel;
  private JLabel currentTaskLabel;
  private JLabel currentTaskDisplayLabel;
  private JButton startStopTaskButton;
  private JButton finishTaskButton;
  private JButton addNewSavedTaskButton;
  private Boolean taskStarted;
  private static JTextField currentTaskTextField;
  public static ArrayList<SavedTask> savedTasksArray;

  public GUI() {
    setTitle("Task Visualizer");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);

    // Menu bar
    menuBar = new JMenuBar();
    // File menu
    fileMenu = new JMenu("File");
    saveMenuItem = new JMenuItem("Save Image..");
    saveMenuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFrame fileChooserFrame = new JFrame();
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setSelectedFile(new File(String.format("%s.png", TaskInfo.taskDay)));
            int userSelection = fileChooser.showSaveDialog(fileChooserFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
              File fileToSave = fileChooser.getSelectedFile();
              System.out.println("Saved as file: " + fileToSave.getAbsolutePath());
              myProcessingSketch.savePNG(fileToSave.getAbsolutePath());
            }
          }
        });
    fileMenu.add(saveMenuItem);

    // View menu
    viewMenu = new JMenu("View");
    showSettingsItem = new JMenuItem("Show Task Settings");
    settingsPanelVisible = false;
    showSettingsItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (settingsPanelVisible == false) {
              settingsPanel.setVisible(true);
              settingsPanelVisible = true;
              showSettingsItem.setText("Hide Task Settings");
            } else {
              settingsPanel.setVisible(false);
              settingsPanelVisible = false;
              showSettingsItem.setText("Show Task Settings");
            }
            pack();
          }
        });
    viewMenu.add(showSettingsItem);

    // add menus to menubar
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    // put the menubar on the frame
    setJMenuBar(menuBar);

    /* build the settings panel */
    settingsPanel = new JPanel();
    settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
    settingsPanel.setPreferredSize(new Dimension(400, 465));

    /* saved tasks label */
    savedTasksLabelPanel = new JPanel();
    savedTasksLabelPanel.setBackground(Color.GRAY);
    savedTasksLabel = new JLabel();
    savedTasksLabel.setText("Saved Tasks");
    savedTasksLabel.setForeground(Color.WHITE);
    savedTasksLabelPanel.add(savedTasksLabel);

    /*
     * current task label
     */
    currentTaskLabelPanel = new JPanel();
    currentTaskLabelPanel.setBackground(Color.GRAY);
    currentTaskLabel = new JLabel();
    currentTaskLabel.setText("Current Task");
    currentTaskLabel.setForeground(Color.WHITE);
    currentTaskLabelPanel.add(currentTaskLabel);

    /* current task panel */
    currentTaskPanel = new JPanel();
    currentTaskPanel.setVisible(false);
    currentTaskPanel.setLayout(new BorderLayout());
    currentTaskDisplayLabel = new JLabel("", SwingConstants.CENTER);
    currentTaskPanel.add(currentTaskDisplayLabel, BorderLayout.CENTER);

    /* create, start, pause, finish task section */
    taskButtonsPanel = new JPanel();
    taskButtonsPanel.setLayout(new BorderLayout());
    startStopTaskButton = new JButton("Start Task");
    startStopTaskButton.setEnabled(false);
    finishTaskButton = new JButton("Finish Task");
    finishTaskButton.setEnabled(false);
    taskStarted = false;
    taskID = "";
    startStopTaskButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String getTaskText = currentTaskTextField.getText();
            if (!taskStarted && taskID.equals("")) {
              taskName = getTaskText.toUpperCase();
              System.out.println("taskName: " + taskName);
              String addTaskCommand = String.format("task add %s", taskName);
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
              String getLastTaskCommand = String.format("task newest rc.verbose=nothing limit:1");
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
              BufferedReader reader =
                  new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
                String startTaskCommand = String.format("task %s start", taskID);
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
                currentTaskDisplayLabel.setText(getTaskText);
                currentTaskTextField.setText("");
                currentTaskTextField.setVisible(false);
                currentTaskPanel.setVisible(true);
                startStopTaskButton.setText("Pause Task");
                startStopTaskButton.setEnabled(true);
                finishTaskButton.setEnabled(true);
              }
            } else if (!taskStarted && startStopTaskButton.getText().equals("Resume Task")) {
              String startTaskCommand = String.format("task %s start", taskID);
              System.out.println(startTaskCommand);
              Process proc = null;
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
              startStopTaskButton.setText("Pause Task");
            } else if (taskStarted && startStopTaskButton.getText().equals("Pause Task")) {
              String stopTaskCommand = String.format("task stop %s", taskID);
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
              startStopTaskButton.setText("Resume Task");
            }
          }
        });
    finishTaskButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.out.println("finishing task..");
            System.out.println("taskID: " + taskID);
            String finishTaskCommand = String.format("task %s done", taskID);
            Process proc = null;
            try {
              // finish the task
              proc = Runtime.getRuntime().exec(finishTaskCommand);
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
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String taskOutput = "";
            try {
              taskOutput = reader.readLine();
            } catch (IOException e1) {
              e1.printStackTrace();
            }
            System.out.println(taskOutput);
            taskStarted = false;
            startStopTaskButton.setText("Start Task");
            currentTaskTextField.setVisible(true);
            currentTaskPanel.setVisible(false);
            startStopTaskButton.setEnabled(false);
            finishTaskButton.setEnabled(false);
            taskID = "";
          }
        });

    taskButtonsPanel.add(startStopTaskButton, BorderLayout.CENTER);
    taskButtonsPanel.add(finishTaskButton, BorderLayout.EAST);

    /* new task textfield */
    currentTaskTextField = new JTextField();
    currentTaskTextField.setHorizontalAlignment(JTextField.CENTER);
    currentTaskTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void changedUpdate(DocumentEvent e) {
                changed();
              }

              public void removeUpdate(DocumentEvent e) {
                changed();
              }

              public void insertUpdate(DocumentEvent e) {
                changed();
              }

              public void changed() {
                if (currentTaskTextField.getText().equals("")) {
                  addNewSavedTaskButton.setEnabled(false);
                  startStopTaskButton.setEnabled(false);
                  finishTaskButton.setEnabled(false);
                } else {
                  taskName = currentTaskTextField.getText();
                  addNewSavedTaskButton.setEnabled(true);
                  startStopTaskButton.setEnabled(true);
                  finishTaskButton.setEnabled(true);
                }
              }
            });

    /* saved task panels */
    savedTaskPanel = new JPanel();
    savedTaskPanel.setLayout(new GridLayout(0, 1));
    savedTaskScrollPane = new JScrollPane(savedTaskPanel);
    addNewSavedTaskPanel = new JPanel();
    addNewSavedTaskPanel.setLayout(new BorderLayout());
    addNewSavedTaskButton = new JButton("+");
    addNewSavedTaskButton.setEnabled(false);
    addNewSavedTaskButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // get task name and add new random color
            String name = currentTaskTextField.getText();
            Random rand = new Random();
            Integer r = Math.round(rand.nextFloat() * 255);
            Integer g = Math.round(rand.nextFloat() * 255);
            Integer b = Math.round(rand.nextFloat() * 255);
            Color color = new Color(r, g, b);
            // disable button because we don't want to accidentally save it twice
            addNewSavedTaskButton.setEnabled(false);
            // create a new task profile and add to saved tasks grid
            addNewButton(taskName, color);
            // save task to text file
            String stringToWrite = name + "," + r + "," + g + "," + b;
            try {
              writeToFile(stringToWrite);
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        });
    savedTaskPanel.add(addNewSavedTaskButton);

    // load the saved tasks. create file if it does not exist.
    try {
      readFile();
    } catch (IOException e2) {
      e2.printStackTrace();
    }

    // add to settings panel
    settingsPanel.add(savedTasksLabelPanel);
    settingsPanel.add(savedTaskScrollPane);

    settingsPanel.add(addNewSavedTaskPanel);

    settingsPanel.add(currentTaskLabelPanel);
    settingsPanel.add(currentTaskPanel);
    settingsPanel.add(currentTaskTextField);

    settingsPanel.add(taskButtonsPanel);

    // make settingsPanel invisible at start
    settingsPanel.setVisible(true);

    // Initialize processing sketch
    myProcessingSketch = new ProcessingSketch();
    myProcessingSketch.init();

    add(app.myProcessingSketch, BorderLayout.WEST);
    add(settingsPanel, BorderLayout.EAST);

    pack();
    setVisible(true);
  }

  public static Boolean checkIfPathExists() {
    String userHomeDir = System.getProperty("user.home");
    File d = new File(userHomeDir + "/.task/");
    File f = new File(userHomeDir + "/.task/savedTasks.txt");
    Boolean exists = false;
    if (d.exists() && d.isDirectory()) {
      System.out.println("found: " + d);
      if (!f.exists()) {
        try {
          f.createNewFile();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        System.out.println("created savedTasks.txt");
      }
      System.out.println("found savedTasks.txt");
      exists = true;
    } else {
      JOptionPane.showMessageDialog(
          null,
          "TaskWarrior is not installed! Please read the README.md for full installation process");
    }
    return exists;
  }

  public static void writeToFile(String content) throws IOException {
    String userHomeDir = System.getProperty("user.home");
    File f = new File(userHomeDir + "/.task/savedTasks.txt");
    if (checkIfPathExists()) {
      FileWriter fw = new FileWriter(f, true);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(content);
      bw.newLine();
      bw.close();
    }
  }

  public static void readFile() throws IOException {
    String userHomeDir = System.getProperty("user.home");
    File f = new File(userHomeDir + "/.task/savedTasks.txt");
    Integer savedTasksLength = 0;
    if (checkIfPathExists()) {
      // the file exists. Read contents. get total
      Scanner myReader = new Scanner(f);
      // get the length
      while (myReader.hasNextLine()) {
        myReader.nextLine();
        savedTasksLength++;
      }
      savedTasksArray = new ArrayList<SavedTask>(savedTasksLength);
      Scanner newReader = new Scanner(f);
      Integer i = 0;
      while (newReader.hasNextLine()) {
        String data = newReader.nextLine();
        String[] dataSplit = data.split(",");
        String name = dataSplit[0];
        Integer r = Integer.parseInt(dataSplit[1]);
        Integer g = Integer.parseInt(dataSplit[2]);
        Integer b = Integer.parseInt(dataSplit[3]);
        Color color = new Color(r, g, b);

        addNewButton(name, color);
        i++;
      }
      myReader.close();
      newReader.close();
    }
  }

  public static void editFile(String targetTaskName, Color newColor) throws IOException {
    String userHomeDir = System.getProperty("user.home");
    File f = new File(userHomeDir + "/.task/savedTasks.txt");
    if (checkIfPathExists()) {
      Scanner myReader = new Scanner(f);
      StringBuffer buffer = new StringBuffer();
      String oldLine = "";
      while (myReader.hasNextLine()) {
        String line = myReader.nextLine();
        buffer.append(line + System.lineSeparator());
        if (line.matches("^" + targetTaskName + "(.*)")) {
          oldLine = line;
        }
      }
      String fileContents = buffer.toString();
      System.out.println("Contents of the file: " + fileContents);
      myReader.close();

      String newLine =
          targetTaskName
              + ","
              + newColor.getRed()
              + ","
              + newColor.getGreen()
              + ","
              + newColor.getBlue();

      fileContents = fileContents.replaceAll(oldLine, newLine);
      FileWriter writer = new FileWriter(f);
      System.out.println("");
      System.out.println("new data: " + fileContents);
      writer.append(fileContents);
      writer.flush();
      writer.close();
    }
  }

  public static void deleteLineFromFile(String targetTaskName) throws IOException {
    String userHomeDir = System.getProperty("user.home");
    File f = new File(userHomeDir + "/.task/savedTasks.txt");
    if (checkIfPathExists()) {
      Scanner myReader = new Scanner(f);
      StringBuffer buffer = new StringBuffer();

      while (myReader.hasNextLine()) {
        String line = myReader.nextLine();
        if (!line.matches("^" + targetTaskName + "(.*)")) {
          buffer.append(line + System.lineSeparator());
        }
      }
      String fileContents = buffer.toString();
      System.out.println("After Deleting: " + fileContents);
      myReader.close();

      FileWriter writer = new FileWriter(f);
      writer.append(fileContents);
      writer.flush();
      writer.close();
    }
  }

  public static JButton addNewButton(String title, Color col) {
    JPanel newTaskPanel = new JPanel();
    newTaskPanel.setLayout(new BorderLayout());
    JButton savedTaskNameButton = new JButton(title);
    savedTaskNameButton.setBackground(col);
    savedTaskNameButton.setOpaque(true);
    savedTaskNameButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            for (int i = 0; i < savedTasksArray.size(); i++) {
              if (savedTasksArray.get(i).name.equals(actionCommand)) {
                // set textfield text to the selected task
                currentTaskTextField.setText(actionCommand);
              }
            }
          }
        });

    JPanel savedTaskEditPanel = new JPanel();
    savedTaskEditPanel.setLayout(new BorderLayout());
    JButton savedTaskDeleteButton = new JButton("x");
    savedTaskDeleteButton.setForeground(Color.RED);
    savedTaskDeleteButton.setActionCommand(title);
    savedTaskDeleteButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            for (int i = 0; i < savedTasksArray.size(); i++) {
              System.out.println(actionCommand);
              if (savedTasksArray.get(i).name.equals(actionCommand)) {
                System.out.println("found: " + actionCommand);
                savedTaskPanel.remove(savedTasksArray.get(i).panel);
                savedTaskPanel.revalidate();
                savedTaskPanel.repaint();
                // delete entry from file
                try {
                  deleteLineFromFile(actionCommand);
                } catch (IOException e1) {
                  e1.printStackTrace();
                }
                // remove object from array altogether
                savedTasksArray.remove(i);
              }
            }
          }
        });
    JButton savedTaskColorChooserButton = new JButton(">");
    savedTaskColorChooserButton.setActionCommand(title);
    savedTaskColorChooserButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            for (int i = 0; i < savedTasksArray.size(); i++) {
              System.out.println(actionCommand);
              if (savedTasksArray.get(i).name.equals(actionCommand)) {
                System.out.println("found: " + actionCommand);
                Color newColor =
                    JColorChooser.showDialog(
                        app, "Choose Background Color", savedTasksArray.get(i).color); // final
                if (newColor != null) {
                  savedTasksArray.get(i).button.setBackground(newColor);
                  // change color in file
                  try {
                    editFile(actionCommand, newColor);
                  } catch (IOException e1) {
                    e1.printStackTrace();
                  }
                  // update SavedTask object color
                  savedTasksArray.get(i).color = newColor;
                }
              }
            }
          }
        });
    savedTaskEditPanel.add(savedTaskColorChooserButton, BorderLayout.WEST);
    savedTaskEditPanel.add(savedTaskDeleteButton, BorderLayout.EAST);

    // create new SavedTask and add to SavedTasks array
    SavedTask savedTask = new SavedTask(title, col, newTaskPanel, savedTaskNameButton);
    savedTasksArray.add(savedTask);

    newTaskPanel.add(savedTaskNameButton, BorderLayout.CENTER);
    newTaskPanel.add(savedTaskEditPanel, BorderLayout.EAST);

    savedTaskPanel.add(newTaskPanel);
    settingsPanel.revalidate();
    return savedTaskNameButton;
  }

  /*
   * Main function.
   */
  public static void main(String[] args) throws JSONException, IOException {
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

  /*
   * Get a file from the resource folder.
   */
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

  /*
   * Show a warning if app couldn't retrieve sunrise or sunset data from the
   * internet..
   */
  public static void showNoConnectionWarning() {
    JOptionPane.showMessageDialog(
        GUI.app, "Couldn't retrieve sunrise and sunset data. No internet connection?");
  }

  /*
   * SavedTask class
   */
  public static class SavedTask {
    public String name;
    public Color color;
    public JPanel panel;
    public JButton button;

    public SavedTask(String name, Color color, JPanel panel, JButton button) {
      this.name = name;
      this.color = color;
      this.panel = panel;
      this.button = button;
    }
  }
}
