import com.google.gson.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class imsuperman extends JFrame {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JFrame frame = new JFrame("imsu-perman");
        JPanel open = new JPanel();
        JPanel fileEdit = new JPanel();
        JPanel folderEdit = new JPanel();
        JPanel recent = new JPanel();
        JButton openFile = new JButton("open file");
        JButton openFolder = new JButton("open folder");
        JButton openRecent = new JButton("open recent");
        JFileChooser fileChooser = new JFileChooser();
        JMenuBar menuBar = new JMenuBar();
        JMenu File = new JMenu("File");
        JMenu Window = new JMenu("Window");
        JMenu View = new JMenu("View");
        // file edit
        UndoManager undoMan = new UndoManager();
        JTextArea fileEditTextArea = new JTextArea();
        fileEditTextArea.getDocument().addUndoableEditListener(undoMan);
        JScrollPane fileScrollPane = new JScrollPane(fileEditTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JTextArea lines = new JTextArea("1");
        // folder edit
        Box folderInFileList = Box.createVerticalBox();
        JTextArea folderEditTextArea = new JTextArea();
        JScrollPane folderScrollPane = new JScrollPane(folderEditTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // open recent
        Box recentList = Box.createVerticalBox();

        fileEditTextArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        fileEditTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int caretPosition = fileEditTextArea.getDocument().getLength();
                Element root = fileEditTextArea.getDocument().getDefaultRootElement();
                String text = "1 " + System.getProperty("line.separator");
                for(int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
                    text += i + System.getProperty("line.separator");
                }
                return text;
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                lines.setText(getText());
            }
            @Override
            public void insertUpdate(DocumentEvent de) {
                lines.setText(getText());
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                lines.setText(getText());
            }
        });
        fileScrollPane.getViewport().add(fileEditTextArea);
        fileScrollPane.setRowHeaderView(lines);
        lines.setEditable(false);

        // Window
        JMenuItem Minimize = new JMenuItem("Minimize");
        Minimize.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        Minimize.setAccelerator(KeyStroke.getKeyStroke('M', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        JMenuItem Zoom = new JMenuItem("Zoom");
        Zoom.addActionListener(e -> {
            if (frame.getExtendedState() != 6) {
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            } else {
                frame.setExtendedState(Frame.NORMAL);
            }
        });

        // File
        JMenuItem Undo = new JMenuItem("Undo");
        JMenuItem Redo = new JMenuItem("Redo");
        JMenuItem Save = new JMenuItem("Save");

        Undo.addActionListener(e -> {
            if (undoMan.canUndo()) {
                undoMan.undo();
            }
        });
        Undo.setAccelerator(KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        Redo.addActionListener(e -> {
            if (undoMan.canRedo()) {
                undoMan.redo();
            }
        });
        Redo.setAccelerator(KeyStroke.getKeyStroke('Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        File.add(Undo);
        File.add(Redo);
        File.addSeparator();
        Window.add(Minimize);
        Window.add(Zoom);
        menuBar.add(File);
        menuBar.add(Window);
        menuBar.add(View);

        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");

        File home = new File(System.getProperty("user.home"));
        fileChooser.setCurrentDirectory(home);
        frame.setJMenuBar(menuBar);
        frame.setPreferredSize(new Dimension(1000, 600));
        open.setBackground(new Color(39, 39 ,39));

        openFile.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(frame);
            if(result == JFileChooser.APPROVE_OPTION){
                File selectFile = fileChooser.getSelectedFile();
                open.setVisible(false);
                frame.add(fileEdit);
                fileEdit.setVisible(true);
                frame.setTitle(selectFile.getName());
                JsonParser parser = new JsonParser();

                try {
                    if (!parser.parse(new FileReader(home + "/imsu-perman.json")).isJsonNull()) {
                        JsonObject data = (JsonObject) parser.parse(
                                    new FileReader(home + "/imsu-perman.json"));
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonArray arr1 = (JsonArray) data.get("lists");

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFile.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", selectFile.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", arr1.size());

                        boolean overlapCheck = false;

                        for (int i = 0; i < arr1.size(); i++) {
                            JsonObject arr3 = (JsonObject) arr1.get(i);
                            String name = arr3.get("name").toString();
                            String path = arr3.get("path").toString();
                            String fileName = '"' + selectFile.getName() + '"';
                            String filePath = '"' + selectFile.getPath() + '"';

                            if (name.equals(fileName) && path.equals(filePath)) {
                                arr1.remove(i);
                                arr1.add(neW);
                                overlapCheck = true;
                            }
                        }

                        if (!overlapCheck) {
                            arr1.add(neW);
                        }

                        data.add("lists", arr1);
                        dbFile.write(gson.toJson(data));
                        dbFile.close();
                    } else {
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject main = new JsonObject();
                        JsonArray lists = new JsonArray();
                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFile.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", selectFile.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", 0);

                        lists.add(neW);
                        main.add("lists", lists);
                        dbFile.write(gson.toJson(main));
                        dbFile.close();
                    }
                } catch (IOException ex) {
                    try {
                        JsonObject data = new JsonObject();
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFile.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", selectFile.getPath());
                        neW.addProperty("date", dtf.format(now));

                        data.add(Integer.toString(data.size() + 1), neW);

                        dbFile.write(gson.toJson(data));
                        dbFile.close();
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                } catch (JsonParseException ex) {
                    ex.printStackTrace();
                }

                //file edit
                fileEdit.setBackground(new Color(39, 39 ,39));
                fileEdit.add(fileScrollPane);
                fileScrollPane.setPreferredSize(new Dimension(frame.getBounds().width - 25, frame.getBounds().height - 5));
                fileEditTextArea.setBackground(new Color(29, 29, 29));
                fileEditTextArea.setForeground(new Color(255, 255, 255));
                fileEditTextArea.setCaretColor(new Color(255, 255, 255));
                fileEditTextArea.setCaretPosition(fileEditTextArea.getDocument().getLength());
                lines.setBackground(new Color(38, 39, 40));
                lines.setForeground(new Color(147, 146, 145));
                fileScrollPane.setBorder(null);
                fileEditTextArea.setTabSize(2);

                frame.addComponentListener(new ComponentAdapter(){
                    public void componentResized(ComponentEvent e){
                        fileScrollPane.setPreferredSize(new Dimension(frame.getBounds().width - 25, frame.getBounds().height - 5));
                    }
                });

                AtomicInteger fontSize = new AtomicInteger(13);
                // View
                JMenuItem FontPlus = new JMenuItem("Font Plus");

                FontPlus.addActionListener(ea -> {
                    if (fontSize.get() <= 23) {
                        fontSize.set(fontSize.get() + 2);
                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                        fileEditTextArea.setFont(myFont);
                        lines.setFont(myFont);
                    }
                });
                FontPlus.setAccelerator(KeyStroke.getKeyStroke('=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                JMenuItem FontMinus = new JMenuItem("Font Minus");

                FontMinus.addActionListener(ex -> {
                    if (fontSize.get() >= 5) {
                        fontSize.set(fontSize.get() - 2);
                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                        fileEditTextArea.setFont(myFont);
                        lines.setFont(myFont);
                    }
                });
                FontMinus.setAccelerator(KeyStroke.getKeyStroke('-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                View.add(FontPlus);
                View.add(FontMinus);
                try (FileReader reader = new FileReader(selectFile.getPath());
                     BufferedReader br = new BufferedReader(reader)) {
                    String line;
                    fileEditTextArea.setText("");
                    while ((line = br.readLine()) != null) {
                        fileEditTextArea.append(line + "\n");
                    }

                } catch (IOException ea) {
                    System.err.format("IOException: %s%n", ea);
                }

                undoMan.die();

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            Scanner scanner = new Scanner(new File(selectFile.getPath()));
                            String[] line = fileEditTextArea.getText().split("\n");
                            ArrayList<String> fileLine = new ArrayList<>();
                            ArrayList<String> textareaLine = new ArrayList<>(Arrays.asList(line));

                            while (scanner.hasNext()) {
                                String str = scanner.nextLine();

                                fileLine.add(str);
                            }

                            if (fileLine.equals(textareaLine)) {
                                System.exit(0);
                            } else {
                                int result = JOptionPane.showConfirmDialog(null, "저장하시겠습니다?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
                                if (result == JOptionPane.YES_OPTION) {
                                    try {
                                        FileWriter editFile = new FileWriter(selectFile.getPath());
                                        editFile.write(fileEditTextArea.getText());
                                        editFile.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }

                                    System.exit(0);
                                } else if (result == JOptionPane.NO_OPTION) {
                                    System.exit(0);
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                File.add(Save);
                Save.addActionListener(ea -> {
                    try {
                        FileWriter editFile = new FileWriter(selectFile.getPath());
                        editFile.write(fileEditTextArea.getText());
                        editFile.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                Save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            }
        });

        openFolder.addActionListener(e -> {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);

            if(result == JFileChooser.APPROVE_OPTION){
                File selectFolder = fileChooser.getSelectedFile();
                File folderList[] = selectFolder.listFiles();
                open.setVisible(false);
                frame.add(folderEdit);
                folderEdit.setVisible(true);
                frame.setTitle(selectFolder.getName());
                JsonParser parser = new JsonParser();

                for (int i = 0;i < folderList.length; i++) {
                    JButton file = new JButton(folderList[i].getName());
                    String filePath = folderList[i].getPath();
                    String fileName = folderList[i].getName();
                    var ref = new Object() {
                        String currentFile = null;
                    };
                    folderInFileList.add(file);
                    file.addActionListener(ea -> {
                        System.out.println(file.getText());

                        try {
                            Scanner scanner = new Scanner(new File(filePath));
                            String[] line = folderEditTextArea.getText().split("\n");
                            ArrayList<String> fileLine1 = new ArrayList<>();
                            ArrayList<String> textareaLine = new ArrayList<>(Arrays.asList(line));

                            while (scanner.hasNext()) {
                                String str = scanner.nextLine();

                                fileLine1.add(str);
                            }

                            if (ref.currentFile == null) {
                                try (FileReader reader = new FileReader(filePath);
                                     BufferedReader br = new BufferedReader(reader)) {
                                    String fileLine2;
                                    folderEditTextArea.setText("");
                                    while ((fileLine2 = br.readLine()) != null) {
                                        folderEditTextArea.append(fileLine2 + "\n");
                                    }
                                    frame.setTitle(selectFolder.getName() + "/" + fileName);

                                } catch (IOException eac) {
                                    System.err.format("IOException: %s%n", eac);
                                }
                            } else {
                                if (fileLine1.equals(textareaLine)) {
                                    try (FileReader reader = new FileReader(filePath);
                                         BufferedReader br = new BufferedReader(reader)) {
                                        String fileLine2;
                                        folderEditTextArea.setText("");
                                        while ((fileLine2 = br.readLine()) != null) {
                                            folderEditTextArea.append(fileLine2 + "\n");
                                        }
                                        frame.setTitle(selectFolder.getName() + "/" + fileName);

                                    } catch (IOException eac) {
                                        System.err.format("IOException: %s%n", eac);
                                    }
                                } else {
                                    int reasult = JOptionPane.showConfirmDialog(null, "저장하시겠습니다?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);

                                    if (reasult == JOptionPane.YES_OPTION) {
                                        try {
                                            FileWriter editFile = new FileWriter(filePath);
                                            editFile.write(folderEditTextArea.getText());
                                            editFile.close();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }

                                        try (FileReader reader = new FileReader(filePath);
                                             BufferedReader br = new BufferedReader(reader)) {
                                            String fileLine2;
                                            folderEditTextArea.setText("");
                                            while ((fileLine2 = br.readLine()) != null) {
                                                folderEditTextArea.append(fileLine2 + "\n");
                                            }
                                            frame.setTitle(selectFolder.getName() + "/" + fileName);

                                        } catch (IOException eac) {
                                            System.err.format("IOException: %s%n", eac);
                                        }
                                    } else if (reasult == JOptionPane.NO_OPTION) {
                                        try (FileReader reader = new FileReader(filePath);
                                             BufferedReader br = new BufferedReader(reader)) {
                                            String fileLine2;
                                            folderEditTextArea.setText("");
                                            while ((fileLine2 = br.readLine()) != null) {
                                                folderEditTextArea.append(fileLine2 + "\n");
                                            }

                                        } catch (IOException eac) {
                                            System.err.format("IOException: %s%n", eac);
                                        }
                                    }
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                        ref.currentFile = fileName;
                    });
                    folderEdit.add(folderInFileList);
                }

                try {
                    if (!parser.parse(new FileReader(home + "/imsu-perman.json")).isJsonNull()) {
                        JsonObject data = (JsonObject) parser.parse(
                                new FileReader(home + "/imsu-perman.json"));
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonArray arr1 = (JsonArray) data.get("lists");

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFolder.getName());
                        neW.addProperty("type", "folder");
                        neW.addProperty("path", selectFolder.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", arr1.size());

                        boolean overlapCheck = false;

                        for (int i = 0; i < arr1.size(); i++) {
                            JsonObject arr3 = (JsonObject) arr1.get(i);
                            String name = arr3.get("name").toString();
                            String path = arr3.get("path").toString();
                            String fileName = '"' + selectFolder.getName() + '"';
                            String filePath = '"' + selectFolder.getPath() + '"';

                            if (name.equals(fileName) && path.equals(filePath)) {
                                arr1.remove(i);
                                arr1.add(neW);
                                overlapCheck = true;
                            }
                        }

                        if (!overlapCheck) {
                            arr1.add(neW);
                        }

                        data.add("lists", arr1);
                        dbFile.write(gson.toJson(data));
                        dbFile.close();
                    } else {
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject main = new JsonObject();
                        JsonArray lists = new JsonArray();
                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFolder.getName());
                        neW.addProperty("type", "folder");
                        neW.addProperty("path", selectFolder.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", 0);

                        lists.add(neW);
                        main.add("lists", lists);
                        dbFile.write(gson.toJson(main));
                        dbFile.close();
                    }
                } catch (IOException ex) {
                    try {
                        JsonObject data = new JsonObject();
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", selectFolder.getName());
                        neW.addProperty("type", "folder");
                        neW.addProperty("path", selectFolder.getPath());
                        neW.addProperty("date", dtf.format(now));

                        data.add(Integer.toString(data.size() + 1), neW);

                        dbFile.write(gson.toJson(data));
                        dbFile.close();
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                } catch (JsonParseException ex) {
                    ex.printStackTrace();
                }

                //file edit
                folderEdit.setBackground(new Color(39, 39 ,39));
                folderEdit.add(folderScrollPane);
                folderInFileList.setPreferredSize(new Dimension(frame.getBounds().width * 33/100, frame.getBounds().height - 35));
                folderScrollPane.setPreferredSize(new Dimension(frame.getBounds().width * 66/100, frame.getBounds().height - 35));
                folderEditTextArea.setBackground(new Color(29, 29, 29));
                folderEditTextArea.setForeground(new Color(255, 255, 255));
                folderEditTextArea.setTabSize(4);
                folderEditTextArea.setCaretColor(new Color(255, 255, 255));
                folderEditTextArea.setCaretPosition(folderEditTextArea.getDocument().getLength());

                // View

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            Scanner scanner = new Scanner(new File(selectFolder.getPath()));
                            String[] line = folderEditTextArea.getText().split("\n");
                            ArrayList<String> fileLine = new ArrayList<>();
                            ArrayList<String> textareaLine = new ArrayList<>(Arrays.asList(line));

                            while (scanner.hasNext()) {
                                String str = scanner.nextLine();

                                fileLine.add(str);
                            }

                            if (fileLine.equals(textareaLine)) {
                                System.exit(0);
                            } else {
                                int result = JOptionPane.showConfirmDialog(null, "저장하시겠습니다?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);

                                if (result == JOptionPane.YES_OPTION) {
                                    try {
                                        FileWriter editFile = new FileWriter(selectFolder.getPath());
                                        editFile.write(folderEditTextArea.getText());
                                        editFile.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }

                                    System.exit(0);
                                } else if (result == JOptionPane.NO_OPTION) {
                                    System.exit(0);
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                File.add(Save);
                Save.addActionListener(ea -> {
                    try {
                        FileWriter editFile = new FileWriter(selectFolder.getPath());
                        editFile.write(folderEditTextArea.getText());
                        editFile.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                Save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            }/*else{
                System.out.println("Open command canceled");
            }*/
        });

        openRecent.addActionListener(e -> {
            open.setVisible(false);
            frame.add(recent);
            recent.setVisible(true);
            frame.setTitle("recent");
            recent.setBackground(new Color(39, 39 ,39));

            JsonParser parser = new JsonParser();
            try {
                JsonObject data = (JsonObject) parser.parse(
                        new FileReader(home + "/imsu-perman.json"));
                JsonArray arr1 = (JsonArray) data.get("lists");

                for (int i = arr1.size() - 1; i >= 0; i--) {
                    JsonObject arr1ToObject = (JsonObject) arr1.get(i);
                    String recentName = arr1ToObject.get("name").toString().replaceAll("\"", "");
                    String recentPath = arr1ToObject.get("path").toString().replaceAll("\"", "");
                    String sum = String.format("<html><h1>%s</h1><h3>%s</h3></html>",
                            recentName, recentPath);
                    JButton recentButton = new JButton(sum);
                    recentButton.addActionListener(es -> {
                        recent.setVisible(false);
                        frame.add(fileEdit);
                        fileEdit.setVisible(true);
                        frame.setTitle(recentName);

                        try {
                            if (!parser.parse(new FileReader(home + "/imsu-perman.json")).isJsonNull()) {
                                FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDateTime now = LocalDateTime.now();

                                JsonArray arr = (JsonArray) data.get("lists");

                                JsonObject neW = new JsonObject();
                                neW.addProperty("name", recentName);
                                neW.addProperty("type", "file");
                                neW.addProperty("path", recentPath);
                                neW.addProperty("date", dtf.format(now));
                                neW.addProperty("index", arr.size());

                                boolean overlapCheck = false;

                                for (int ia = 0; ia < arr.size(); ia++) {
                                    JsonObject arr3 = (JsonObject) arr.get(ia);
                                    String name = arr3.get("name").toString();
                                    String path = arr3.get("path").toString();
                                    String fileName = '"' + recentName + '"';
                                    String filePath = '"' + recentPath + '"';

                                    if (name.equals(fileName) && path.equals(filePath)) {
                                        arr.remove(ia);
                                        arr.add(neW);
                                        overlapCheck = true;
                                    }
                                }

                                if (!overlapCheck) {
                                    arr.add(neW);
                                }

                                data.add("lists", arr);

                                dbFile.write(gson.toJson(data));
                                dbFile.close();
                            } else {
                                FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDateTime now = LocalDateTime.now();

                                JsonObject main = new JsonObject();
                                JsonArray lists = new JsonArray();
                                JsonObject neW = new JsonObject();
                                neW.addProperty("name", recentName);
                                neW.addProperty("type", "file");
                                neW.addProperty("path", recentPath);
                                neW.addProperty("date", dtf.format(now));
                                neW.addProperty("index", 0);

                                lists.add(neW);
                                main.add("lists", lists);
                                dbFile.write(gson.toJson(main));
                                dbFile.close();
                            }
                        } catch (IOException ex) {
                            try {
                                FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDateTime now = LocalDateTime.now();

                                JsonObject neW = new JsonObject();
                                neW.addProperty("name", recentName);
                                neW.addProperty("type", "file");
                                neW.addProperty("path", recentPath);
                                neW.addProperty("date", dtf.format(now));

                                data.add(Integer.toString(data.size() + 1), neW);

                                dbFile.write(gson.toJson(data));
                                dbFile.close();
                            } catch (IOException a) {
                                a.printStackTrace();
                            }
                        } catch (JsonParseException ex) {
                            ex.printStackTrace();
                        }

                        //file edit
                        fileEdit.setBackground(new Color(39, 39 ,39));
                        fileEdit.add(fileScrollPane);
                        fileScrollPane.setPreferredSize(new Dimension(frame.getBounds().width - 25, frame.getBounds().height - 5));
                        fileEditTextArea.setBackground(new Color(29, 29, 29));
                        fileEditTextArea.setForeground(new Color(255, 255, 255));
                        fileEditTextArea.setCaretColor(new Color(255, 255, 255));
                        fileEditTextArea.setCaretPosition(fileEditTextArea.getDocument().getLength());
                        lines.setBackground(new Color(38, 39, 40));
                        lines.setForeground(new Color(147, 146, 145));
                        fileScrollPane.setBorder(null);
                        fileEditTextArea.setTabSize(2);

                        frame.addComponentListener(new ComponentAdapter(){
                            public void componentResized(ComponentEvent e){
                                fileScrollPane.setPreferredSize(new Dimension(frame.getBounds().width - 25, frame.getBounds().height - 5));
                            }
                        });

                        AtomicInteger fontSize = new AtomicInteger(13);
                        // View
                        JMenuItem FontPlus = new JMenuItem("Font Plus");

                        FontPlus.addActionListener(ea -> {
                            if (fontSize.get() <= 23) {
                                fontSize.set(fontSize.get() + 2);
                                Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                                fileEditTextArea.setFont(myFont);
                                lines.setFont(myFont);
                            }
                        });
                        FontPlus.setAccelerator(KeyStroke.getKeyStroke('=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                        JMenuItem FontMinus = new JMenuItem("Font Minus");

                        FontMinus.addActionListener(ex -> {
                            if (fontSize.get() >= 5) {
                                fontSize.set(fontSize.get() - 2);
                                Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                                fileEditTextArea.setFont(myFont);
                                lines.setFont(myFont);
                            }
                        });
                        FontMinus.setAccelerator(KeyStroke.getKeyStroke('-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                        View.add(FontPlus);
                        View.add(FontMinus);
                        try (FileReader reader = new FileReader(recentPath);
                             BufferedReader br = new BufferedReader(reader)) {
                            String line;
                            fileEditTextArea.setText("");
                            while ((line = br.readLine()) != null) {
                                fileEditTextArea.append(line + "\n");
                            }

                        } catch (IOException ea) {
                            System.err.format("IOException: %s%n", ea);
                        }

                        undoMan.die();

                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                try {
                                    Scanner scanner = new Scanner(new File(recentPath));
                                    String[] line = fileEditTextArea.getText().split("\n");
                                    ArrayList<String> fileLine = new ArrayList<>();
                                    ArrayList<String> textareaLine = new ArrayList<>(Arrays.asList(line));

                                    while (scanner.hasNext()) {
                                        String str = scanner.nextLine();

                                        fileLine.add(str);
                                    }

                                    if (fileLine.equals(textareaLine)) {
                                        System.exit(0);
                                    } else {
                                        int result = JOptionPane.showConfirmDialog(null, "저장하시겠습니다?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);

                                        if (result == JOptionPane.YES_OPTION) {
                                            try {
                                                FileWriter editFile = new FileWriter(recentPath);
                                                editFile.write(fileEditTextArea.getText());
                                                editFile.close();
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }

                                            System.exit(0);
                                        } else if (result == JOptionPane.NO_OPTION) {
                                            System.exit(0);
                                        }
                                    }
                                } catch (FileNotFoundException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        });

                        File.add(Save);
                        Save.addActionListener(ea -> {
                            try {
                                FileWriter editFile = new FileWriter(recentPath);
                                editFile.write(fileEditTextArea.getText());
                                editFile.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                        Save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                    });
                    recentList.add(recentButton);
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

            JButton back = new JButton("back");

            back.addActionListener(r -> {
                recentList.removeAll();
                open.setVisible(true);
                recent.setVisible(false);
                frame.setTitle("imsu-perman");
            });
            recentList.add(back);
            recent.add(recentList);
        });

        open.add(openFile);
//        open.add(openFolder);
        open.add(openRecent);

        frame.add(open);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}