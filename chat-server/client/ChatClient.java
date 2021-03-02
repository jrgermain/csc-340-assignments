/******
 * ChatClient
 * Author: Christian Duncan 
 * Updated by: ...
 *
 * This code provides a basic GUI ChatClient.
 * It is a single frame made of 3 parts:
 *    A textbox for updated messages
 *    An input textbox for entering in messages to send
 *    A "send" button to send the current textbox material.
 *
 * THIS IS JUST A FRAMEWORK so actual communication is not yet 
 * established.
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
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    // Insert code that will send the quit command to the server
                    //System.out.println("HOLA AMIGO COMO ESTA?");
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
    private String userName = "<UNDEFINED>";

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Boolean socketExists = false;
    private Input input;
    private Output output;
    
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
                    if (message != null && message != "") {
                        // There is something to transmit
                        // NOTE: You will want to fix this so it actually
                        // TRANSMITS the message to the server!
                        try  {
                            out.println("TRANSMIT " + message);

                           // output.run(message)  //Open a thread

                            //postMessage(in.readLine());

                           // inpunt.run()
                        } catch (Exception ex) {
                            
                        }
                        
                        //postMessage("DEBUG: Transmit: " + message);
                        sendTextArea.setText("");  // Clear out the field
                    }
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
                    // NOTE: This does not TRANSMIT the request to the server
                    // This is just a placeholder to display the choice.
                    //postMessage("DEBUG: User name: " + newUserName);
                    
                    if(socketExists){
                        try {
                            //out.println("EXITING " + username);
                            out.println("ENTER " + newUserName);
                            changeUserName(newUserName); // Ideally, this would be done only once the server accepts and replies back with user name
                        } catch (Exception ex) {
                            
                        }
                    }
                    
                    else {
                        changeUserName(newUserName);
                    }
                    
                    
                }
            };
        changeUserName("<UNDEFINED>");
        nameAction.putValue(Action.SHORT_DESCRIPTION, "Push this to change user name.");
        button = new JButton(nameAction);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        mainPane.add(button);

        // Set up a button to get a new room name (and transmit request to the server)
        roomAction = new AbstractAction("Set/Change Room") {
            public void actionPerformed(ActionEvent e) {
                // Get the new room and transmit to the server!
                String newRoomName = JOptionPane.showInputDialog("Please enter a room.");
                // NOTE: This does not TRANSMIT the request to the server
                // This is just a placeholder to display the choice.
                //postMessage("DEBUG: Room name: " + newRoomName);
                
                if(socketExists){
                    try {
                        //out.println("EXITING " + username);
                        out.println("JOIN " + newRoomName);
                       // changeRoom(newRoom); // Ideally, this would be done only once the server accepts and replies back with user name
                    } catch (Exception ex) {
                        
                    }
                }
                
                else {
                    
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

                        //Create a new thread 

                        establishConnection();


                    } catch(Exception ex){
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

    public void establishConnection() throws Exception {
        socket = new Socket(hostname,port);
        socketExists = true;
        System.out.println("Connected. Communicating from port " +
                socket.getLocalPort() + " to port " +
                socket.getPort() + ".");
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        out.println("ENTER " + userName);
        new Thread(new Input()).start();
        //output = new Output();
        input.run();
        
        
        
    }

    // Post a message on the main Chat Text Area (with a new line)
    public synchronized void postMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    class Output extends Thread {
        public void run(String message){

           
			out.println(message);
            System.out.println("Output reached end");
        
        }
    }

    class Input extends Thread{
        public void run(){
            try {
                while(socketExists){
                    String inputString = in.readLine();
                    switch(inputString.split(" ")[0]){
                        case "ACK" : {
                            switch(inputString.split(" ")[1]){
                                case "ENTER":{
                                    postMessage("You have entered as " + inputString.split(" ")[2] );
                                    
                                } break;
                                case "TRANSMIT":{
                                    
                                } break;
                                
                            }
                        } break;
                        case "ENTERING":{
                            postMessage((inputString.split(" ")[1]) + " has entered the chat");
                        } break;
                        case "EXITING":{
                            postMessage((inputString.split(" ")[1]) + " has left the chat");
                        } break;

                        default: {
                            postMessage(inputString);
                        } break;
                    }
                    System.out.println();
                    //postMessage(inputString);
                }
                
				
                System.out.println("Input Reached End");
			} catch (IOException e) {
				e.printStackTrace();
			}
            
        }
    }
    
}
