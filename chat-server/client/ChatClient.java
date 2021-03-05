/******
 * ChatClient
 * Author: Christian Duncan 
 * Updated by: Kevin Sangurima, Brian Carballo, James Jacobson
 *
 * This code provides a basic GUI ChatClient.
 * It is a single frame made of 3 parts:
 *    A textbox for updated messages
 *    An input textbox for entering in messages to send
 *    A "send" button to send the current textbox material.
 *
 ******/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame implements Runnable {

    public void run() {
        // Create and start up the ChatClient Frame
        ChatClient frame = new ChatClient();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            //Overrides close button to send an exit message to server when client is closed
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    //Send exit message to server
                    frame.out.println("EXIT");
                    System.exit(0);
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String args[]) {
        (new Thread(new ChatClient())).start();
    }

    private JTextArea chatTextArea;
    private JTextArea sendTextArea;
    private Action nameAction;
    private Action roomAction;
    private String hostname = "127.0.0.1";  // Default is local host
    private int port = 1518;                // Default port is 1518
    private String userName = "Default";    // Default username is "Default" to prevent sending invalid name

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Boolean socketExists = false;
    private Input input;

    /* Constructor: Sets up the initial look-and-feel */
    public ChatClient() {
        JLabel label;  // Temporary variable for a label
        JButton button; // Temporary variable for a button

        // Set up the initial size and layout of the frame
        // For this we will keep it to a simple BoxLayout
        setLocation(100, 100);
        setPreferredSize(new Dimension(1000, 500));
        setTitle("CSC340 Chat Client");
        Container mainPane = getContentPane();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.setPreferredSize(new Dimension(1000, 500));

        // Set up the text area for receiving chat messages
        chatTextArea = new JTextArea(30, 80);
        chatTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        label = new JLabel("Chat Messages", JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPane.add(label);
        mainPane.add(scrollPane);

        // Set up the text area for entering chat messages (to send)
        sendTextArea = new JTextArea(3, 80);
        sendTextArea.setEditable(true);
        scrollPane = new JScrollPane(sendTextArea);
        label = new JLabel("Message to Transmit", JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPane.add(label);
        mainPane.add(scrollPane);

        // Set up a button to "send" the chat message
        Action sendAction = new AbstractAction("Send") {
            public void actionPerformed(ActionEvent e) {
                // Send the message in the text area (if anything)
                // and clear the text area
                String message = sendTextArea.getText();
                if (message != null && !message.isEmpty()) {
                    // There is something to transmit
                    String[] messageLines = message.split("\n");
                    //Splits the lines into separate strings
                    try {
                        //Sends each string in the array to the server as a separate message
                        for (String string : messageLines) {
                            out.println("TRANSMIT " + string);
                            //Pauses execution to prevent server from being overloaded with requests
                            Thread.sleep(5);
                        }
                    } catch (Exception ex) {

                    }
                }
                //postMessage("DEBUG: Transmit: " + message);
                sendTextArea.setText("");  // Clear out the field
                sendTextArea.requestFocus();  // Focus back on box
            }
        };

        sendAction.putValue(Action.SHORT_DESCRIPTION, "Push this to transmit message to server.");

        // ALT+ENTER will automatically trigger this button
        sendAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ENTER);

        button = new JButton(sendAction);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        mainPane.add(button);

        // Set up Ctrl-Enter in JTextArea as a send option as well
        setupTextAreaSend(sendAction);

        // Set up a button to get a new user name (and transmit request to the server)
        nameAction = new AbstractAction("Set/Change User Name") {
            public void actionPerformed(ActionEvent e) {
                // Get the new user name and transmit to the server!
                String newUserName = JOptionPane.showInputDialog("Please enter a user name.  Current user name: " + userName);
                if (socketExists) {
                    try {
                        //Sends request to server set username as the string entered
                        out.println("ENTER " + newUserName);
                    } catch (Exception ex) {

                    }
                } else {
                    //Allows user to change the username to be sent to server before attempting to connect
                    changeUserName(newUserName);
                }
            }
        };

        // changeUserName("Default"); //For debugging purposes set automatically to default
        nameAction.putValue(Action.SHORT_DESCRIPTION, "Push this to change user name.");
        button = new JButton(nameAction);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        mainPane.add(button);

        // Set up a button to get a new room name (and transmit request to the server)
        roomAction = new AbstractAction("Set/Change Room") {
            public void actionPerformed(ActionEvent e) {
                // Get the new room and transmit to the server!
                String newRoomName = JOptionPane.showInputDialog("Please enter a room.");
                //postMessage("DEBUG: Room name: " + newRoomName);
                if (socketExists) { 
                    try {
                        //Sends room request to server
                        out.println("JOIN " + newRoomName);
                    } catch (Exception ex) {

                    }
                }
            }
        };

        roomAction.putValue(Action.SHORT_DESCRIPTION, "Push this to change room.");
        button = new JButton(roomAction);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        mainPane.add(button);

        // Setup the menubar
        setupMenuBar();
    }

    private void setupTextAreaSend(Action sendAction) {
        //System.err.println("DEBUG: Setting up TextAreaSend");
        // Get InputMap and ActionMap for the sendTextArea
        InputMap inputMap = sendTextArea.getInputMap();
        ActionMap actionMap = sendTextArea.getActionMap();

        // Get the key used to send a message (for us, CTRL+ENTER)
        KeyStroke sendKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(sendKeyStroke, "SendText");

        // Add the send action for this key to the Text Area's ActionMap
        actionMap.put("SendText", sendAction);
    }

    private void setupMenuBar() {
        JMenuBar mbar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        Action menuAction;
        menu = new JMenu("Connection");

        // Menu item to change server IP address (or hostname really)
        menuAction = new AbstractAction("Change Server IP") {
            public void actionPerformed(ActionEvent e) {
                String newHostName = JOptionPane.showInputDialog("Please enter a server IP/Hostname.\nThis only takes effect after the next connection attempt.\nCurrent server address: " + hostname);
                if (newHostName != null && newHostName.length() > 0)
                    hostname = newHostName;
            }
        };
        menuAction.putValue(Action.SHORT_DESCRIPTION, "Change server IP address.");
        menuItem = new JMenuItem(menuAction);
        menu.add(menuItem);

        // Menu item to change the port to use
        menuAction = new AbstractAction("Change Server PORT") {
            public void actionPerformed(ActionEvent e) {
                String portName = JOptionPane.showInputDialog("Please enter a server PORT.\nThis only takes effect after the next connection attempt.\nCurrent port: " + port);
                if (portName != null && portName.length() > 0) {
                    try {
                        int p = Integer.parseInt(portName);
                        if (p < 0 || p > 65535) {
                            JOptionPane.showMessageDialog(null, "The port [" + portName + "] must be in the range 0 to 65535.", "Invalid Port Number", JOptionPane.ERROR_MESSAGE);
                        } else {
                            port = p;  // Valid.  Update the port
                        }
                    } catch (NumberFormatException ignore) {
                        JOptionPane.showMessageDialog(null, "The port [" + portName + "] must be an integer.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        menuAction.putValue(Action.SHORT_DESCRIPTION, "Change server PORT.");
        menuItem = new JMenuItem(menuAction);
        menu.add(menuItem);

        // Menu item to create a connection
        menuAction = new AbstractAction("Connect to Server") {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Attempts to establish a conection with the server
                    establishConnection();
                } catch (Exception ex) {
                    System.out.print(ex);
                }
            }
        };
        menuAction.putValue(Action.SHORT_DESCRIPTION, "Change server PORT.");
        menuItem = new JMenuItem(menuAction);
        menu.add(menuItem);

        mbar.add(menu);
        setJMenuBar(mbar);
    }

    // Changes the user name on the nameAction
    public void changeUserName(String newName) {
        userName = newName;
        nameAction.putValue(Action.NAME, "User Name: " + userName);
    }

    // Creates a new socket and connects the client to the port (1518 in our case)
    public void establishConnection() throws Exception {
        socket = new Socket(hostname, port);
        //Used to check if connection exists by other functions
        socketExists = true;
        System.out.println("Connected. Communicating from port " +
                socket.getLocalPort() + " to port " +
                socket.getPort() + ".");
        out = new PrintWriter(socket.getOutputStream(), true); // Passes information to the server
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Information from the server

        out.println("ENTER " + userName);
        new Thread(new Input()).start(); // The thread listens for incoming commands
    }

    // Post a message on the main Chat Text Area (with a new line)
    public synchronized void postMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    /**
    * This class is responsible for creating a new thread that listens for input
    * A thread was used to prevent blocking operation from blocking UI functions
    **/
    class Input extends Thread {
        public void run() {
            try {
                while (socketExists) {
                    String inputString = in.readLine(); // Listens for input coming from the server (Line 277)
                    String[] inputStringArray = inputString.split(" "); //Splits string into array for parsing
                    switch (inputStringArray[0]) {
                        case "ACK": {
                            // The split string is parsed based on the command from the client
                            switch (inputString.split(" ")[1]) {
                                // Client receives an acknowledgement from the server in response to entrance / name change
                                case "ENTER": {
                                    changeUserName(inputString.split(" ")[2]); 
                                    postMessage("You have entered as " + inputString.split(" ")[2]);
                                }
                                break;
                                // Client receives an acknowledgement from the server in response to sending a message
                                case "TRANSMIT": {
                                }
                                break;
                                // Client recieves an acknowlegement from the server in response to a room change
                                case "JOIN": {
                                    postMessage("You have entered room: " + inputString.split(" ")[2]);
                                }
                            }
                        }
                        break;
                        //Responds to a new user entering the room 
                        case "ENTERING": {
                            postMessage((inputString.split(" ")[1]) + " has entered the chat");
                        }
                        break;
                        //Responds to a user exiting the room
                        case "EXITING": {
                            postMessage((inputString.split(" ")[1]) + " has left the chat");
                        }
                        break;
                        //Responds to a message sent from another user
                        case "NEWMESSAGE": {
                            String name = inputStringArray[1];
                            String message = name + ": ";
                            // Puts string back together for displaying without the server command text
                            for (int i = 2; i < inputStringArray.length; i++) {
                                message += inputStringArray[i] + " ";
                            }
                            postMessage(message);
                        }
                        break;
                        //Post any unrecognized server command as text in the chat window
                        default: {
                            postMessage(inputString);
                        }
                        break;
                    }
                    System.out.println();
                    //postMessage(inputString);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}