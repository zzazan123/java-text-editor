import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
        JTextArea fileEditTextArea = new JTextArea();
        JScrollPane fileScrollPane = new JScrollPane(fileEditTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // folder edit
        JTextArea folderEditTextArea = new JTextArea();
        JScrollPane folderScrollPane = new JScrollPane(folderEditTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JButton btn = new JButton("how");

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
        JMenuItem Save = new JMenuItem("Save");

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
        ImageIcon icon = new ImageIcon("icon.png");
        frame.setIconImage(icon.getImage());
        frame.setPreferredSize(new Dimension(1000, 600));
        open.setBackground(new Color(39, 39 ,39));

        openFile.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(frame);
            if(result == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
//                System.out.println("File Selected: " + file.getName());
                open.setVisible(false);
                frame.add(fileEdit);
                fileEdit.setVisible(true);
                frame.setTitle(file.getName());
                JsonParser parser = new JsonParser();

                try {
                    if (!parser.parse(new FileReader(home + "/imsu-perman.json")).isJsonNull()) {
                        JsonObject data = (JsonObject) parser.parse(
                                    new FileReader(home + "/imsu-perman.json"));
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonArray arr1 = (JsonArray) data.get("lists");
//                        JsonArray arr1 = data.getAsJsonArray("lists");

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", file.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", file.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", arr1.size());

                        boolean overlapCheck = false;

                        for (int i = 0; i < arr1.size(); i++) {
                            JsonObject arr3 = (JsonObject) arr1.get(i);
                            String name = arr3.get("name").toString();
                            String path = arr3.get("path").toString();
                            String fileName = '"' + file.getName() + '"';
                            String filePath = '"' + file.getPath() + '"';

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
                        //                    System.out.println(data);
                        dbFile.write(gson.toJson(data));
                        dbFile.close();
                    } else {
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject main = new JsonObject();
                        JsonArray lists = new JsonArray();
                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", file.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", file.getPath());
                        neW.addProperty("date", dtf.format(now));
                        neW.addProperty("index", 0);

                        lists.add(neW);
                        main.add("lists", lists);
                        //                    System.out.println(data);
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
                        neW.addProperty("name", file.getName());
                        neW.addProperty("type", "file");
                        neW.addProperty("path", file.getPath());
                        neW.addProperty("date", dtf.format(now));

                        data.add(Integer.toString(data.size() + 1), neW);

                        dbFile.write(gson.toJson(data));
                        dbFile.close();
//                        System.out.println("파일을 만듬");
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                } catch (JsonParseException ex) {
                    ex.printStackTrace();
                }

                //file edit
                fileEdit.setBackground(new Color(39, 39 ,39));
                fileEdit.add(fileScrollPane);
                fileScrollPane.setPreferredSize(new Dimension(frame.getBounds().width - 10, frame.getBounds().height - 35));
                fileEditTextArea.setBackground(new Color(29, 29, 29));
                fileEditTextArea.setForeground(new Color(255, 255, 255));
                fileEditTextArea.setTabSize(4);
                fileEditTextArea.setCaretColor(new Color(255, 255, 255));
                fileEditTextArea.setCaretPosition(fileEditTextArea.getDocument().getLength());

                AtomicInteger fontSize = new AtomicInteger(13);
                // View
                JMenuItem FontPlus = new JMenuItem("Font Plus");

                FontPlus.addActionListener(ea -> {
                    if (fontSize.get() <= 23) {
                        fontSize.set(fontSize.get() + 2);
                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                        fileEditTextArea.setFont(myFont);
                    }
                });
                FontPlus.setAccelerator(KeyStroke.getKeyStroke('=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                JMenuItem FontMinus = new JMenuItem("Font Minus");

                FontMinus.addActionListener(ex -> {
                    if (fontSize.get() >= 5) {
                        fontSize.set(fontSize.get() - 2);

                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
                        fileEditTextArea.setFont(myFont);
                    }
                });
                FontMinus.setAccelerator(KeyStroke.getKeyStroke('-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

                View.add(FontPlus);
                View.add(FontMinus);
                try (FileReader reader = new FileReader(file.getPath());
                     BufferedReader br = new BufferedReader(reader)) {
                    String line;
                    fileEditTextArea.setText("");
                    while ((line = br.readLine()) != null) {
                        fileEditTextArea.append(line + "\n");
                    }

                } catch (IOException ea) {
                    System.err.format("IOException: %s%n", ea);
                }

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            Scanner scanner = new Scanner(new File(file.getPath()));
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
//
                                if (result == JOptionPane.YES_OPTION) {
                                    try {
                                        FileWriter editFile = new FileWriter(file.getPath());
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
                        FileWriter editFile = new FileWriter(file.getPath());
                        editFile.write(fileEditTextArea.getText());
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

        openFolder.addActionListener(e -> {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);

            if(result == JFileChooser.APPROVE_OPTION){
                File folder = fileChooser.getSelectedFile();
//                System.out.println("File Selected: " + file.getName());
                open.setVisible(false);
                frame.add(fileEdit);
                folderEdit.setVisible(true);
                frame.setTitle(folder.getName());
                JsonParser parser = new JsonParser();

                try {
                    JsonObject data = (JsonObject) parser.parse(
                            new FileReader(home + "/imsu-perman.json"));
                    FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();

                    JsonArray arr1 = (JsonArray) data.get("lists");

                    JsonObject neW = new JsonObject();
                    neW.addProperty("name", folder.getName());
                    neW.addProperty("type", "folder");
                    neW.addProperty("path", folder.getPath());
                    neW.addProperty("date", dtf.format(now));
                    neW.addProperty("index", arr1.size());

                    boolean overlapCheck = false;

                    for (int i = 0;i < arr1.size();i++) {
                        JsonObject arr3 = (JsonObject) arr1.get(i);
                        String name = arr3.get("name").toString();
                        String path = arr3.get("path").toString();
                        String fileName =  '"' + folder.getName() + '"';
                        String filePath = '"' + folder.getPath() + '"';

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
//                    System.out.println(data);
                    dbFile.write(gson.toJson(data));
                    dbFile.close();
                } catch (IOException ex) {
                    try {
                        JsonObject data = new JsonObject();
                        FileWriter dbFile = new FileWriter(home + "/imsu-perman.json");

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();

                        JsonObject neW = new JsonObject();
                        neW.addProperty("name", folder.getName());
                        neW.addProperty("type", "folder");
                        neW.addProperty("path", folder.getPath());
                        neW.addProperty("date", dtf.format(now));

                        data.add(Integer.toString(data.size() + 1), neW);

                        dbFile.write(gson.toJson(data));
                        dbFile.close();
//                        System.out.println("파일을 만듬");
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                } catch (JsonParseException ex) {
                    ex.printStackTrace();
                }

                //file edit
                folderEdit.setBackground(new Color(39, 39 ,39));
                folderEdit.add(btn);
                btn.setPreferredSize(new Dimension(frame.getBounds().width * 33/100, frame.getBounds().height - 35));
                folderEdit.add(folderScrollPane);
                folderScrollPane.setPreferredSize(new Dimension(frame.getBounds().width * 66/100, frame.getBounds().height - 35));
                folderEditTextArea.setBackground(new Color(29, 29, 29));
                folderEditTextArea.setForeground(new Color(255, 255, 255));
                folderEditTextArea.setTabSize(4);
                folderEditTextArea.setCaretColor(new Color(255, 255, 255));
                folderEditTextArea.setCaretPosition(folderEditTextArea.getDocument().getLength());

//                AtomicInteger fontSize = new AtomicInteger(13);
                // View
//                JMenuItem FontPlus = new JMenuItem("Font Plus");
//
//                FontPlus.addActionListener(ea -> {
//                    if (fontSize.get() <= 23) {
//                        fontSize.set(fontSize.get() + 2);
//                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
//                        folderEditTextArea.setFont(myFont);
//                    }
//                });
//                FontPlus.setAccelerator(KeyStroke.getKeyStroke('=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
//
//                JMenuItem FontMinus = new JMenuItem("Font Minus");
//
//                FontMinus.addActionListener(ex -> {
//                    if (fontSize.get() >= 5) {
//                        fontSize.set(fontSize.get() - 2);
//
//                        Font myFont = new Font("Lucida Grande", Font.PLAIN, fontSize.get());
//                        folderEditTextArea.setFont(myFont);
//                    }
//                });
//                FontMinus.setAccelerator(KeyStroke.getKeyStroke('-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

//                View.add(FontPlus);
//                View.add(FontMinus);

//                try (FileReader reader = new FileReader(folder.getPath());
//                     BufferedReader br = new BufferedReader(reader)) {
//                    String line;
//                    folderEditTextArea.setText("");
//                    while ((line = br.readLine()) != null) {
//                        folderEditTextArea.append(line + "\n");
//                    }
//
//                } catch (IOException ea) {
//                    System.err.format("IOException: %s%n", ea);
//                }

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            Scanner scanner = new Scanner(new File(folder.getPath()));
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
//
                                if (result == JOptionPane.YES_OPTION) {
                                    try {
                                        FileWriter editFile = new FileWriter(folder.getPath());
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
                        FileWriter editFile = new FileWriter(folder.getPath());
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

        });

        open.add(openFile);
        open.add(openFolder);
        open.add(openRecent);

        frame.add(open);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}