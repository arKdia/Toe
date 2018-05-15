/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.ImageIcon;

/**
 *
 * @author Mario
 */
public class TicTacClient extends Application{
    private Label messagelabel;
    GridPane pane;
    Button[] buttonList;
    Button b;
    Scene scene;
    
    private Socket socket;
    private static int PORT = 8901;
    private BufferedReader in;
    private PrintWriter out;
    
    char mark = ' ';
    char opp  = ' ';
    char myMark = ' ';
    
    String response;
    String serverAddress;
    
 
    public void startNetBoard(String serverAddress) throws Exception {
       // --Network--
       //System.out.println("Where would you like to play?[localhost or IP]");
       socket = new Socket(serverAddress, PORT);
       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       out = new PrintWriter(socket.getOutputStream(), true);
       
       // --GUI--
       startGUI();

    } 
    
    public void startGUI(){
       messagelabel = new Label();
       buttonList = new Button[9];
       
       for (int i=0; i < buttonList.length; i++){
           buttonList[i] = new Button();
           buttonList[i].setId(Integer.toString(i));
           buttonList[i].setText("");
           buttonList[i].setOnAction(myHandler);
       }
       
       //column |, row --
       pane = new GridPane();

       pane.add(buttonList[0], 0, 0);
       pane.add(buttonList[1], 1, 0);
       pane.add(buttonList[2], 2, 0);
       pane.add(buttonList[3], 0, 1);
       pane.add(buttonList[4], 1, 1);
       pane.add(buttonList[5], 2, 1);
       pane.add(buttonList[6], 0, 2);
       pane.add(buttonList[7], 1, 2);
       pane.add(buttonList[8], 2, 2);
       pane.add(messagelabel,0,3,3,3);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent e) {
            Platform.exit();
            System.exit(0);
        }
        });
        
        try {
            startNetBoard(serverAddress);
        } catch (Exception ex) {
            System.err.println("Err creating network-board: " + ex);
        }
       
        new Thread(new Runnable() {
            public void run() {
                try {
                    response = in.readLine();
                    if (response.startsWith("WELCOME")) {
                        myMark = mark = response.charAt(8);
                        opp = (mark == 'X' ? 'O' : 'X');
                        System.out.println("this user is: " + mark);
                        //messagelabel.setText(response);
                    }
                    while (true) {
                            response = in.readLine();
                            if (response.startsWith("VALID_MOVE")){
                                updateLabel(response);
                                updateButton(Character.toString(mark));
                                System.out.printf("\nResponse: %s", response);
                            }
                            else if (response.startsWith("OPPONENT_MOVED")){
                                int loc = Integer.parseInt(response.substring(15));
                                b = (Button) buttonList[loc];
                                updateButton(Character.toString(opp));
                                System.out.printf("\nResponse: %s", response);
                                updateLabel("Opponent moved, your turn");
                            }
                            else if (response.startsWith("VICTORY")){
                                updateLabel("You Win!");
                                break;
                            }
                            else if (response.startsWith("DEFEAT")){
                                updateLabel("you Lose");
                                break;
                            }
                            else if (response.startsWith("TIE")){
                                updateLabel("Tie");
                                break;
                            }
                            else if (response.startsWith("MESSAGE")){
                                updateLabel(response.substring(8));
                                //System.out.printf("\nResponse: %s", response);
                            }
                    }
                    out.println("QUIT");
                } catch (IOException ex) {
                    System.err.println("Error reading response " + ex);
                } finally {
                    try {socket.close();} catch (IOException e) {}
                }
            }
        }).start();

        primaryStage.setTitle("-> Player - Tic Tac Toe ");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(mark == 'X' ? "x.png" : "o.png")));
        primaryStage.setResizable(false);
        scene = new Scene(pane, 290, 310);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(TicTacClient.class.getResource("toe.css").toExternalForm());
        primaryStage.show();
    }
    
    protected void updateLabel(String message) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                messagelabel.setText(" "+message);
            }
        };
        if (Platform.isFxApplicationThread()) {
            command.run();
        } else {
            Platform.runLater(command);
        }

    }
    
    protected void updateButton(String message) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                b.setText(message);
            }
        };
        if (Platform.isFxApplicationThread()) {
            command.run();
        } else {
            Platform.runLater(command);
        }
    }

    public boolean playAgain(){
        Alert alert = new Alert(AlertType.CONFIRMATION, "Another match?",
                      ButtonType.YES, ButtonType.NO);
        alert.setTitle("Tic Tac Toe: " + mark);
        alert.setHeaderText(null);
        //alert.setContentText("another match?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES)
            return true;
        return false;
    }
    
    
    
    final EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent event) {
            b = (Button) event.getSource();
            //System.out.printf("\nMove: %d\n", Integer.parseInt(b.getId()));
            out.println("MOVE "+ Integer.parseInt(b.getId()));
        }
    };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TicTacClient tic = new TicTacClient();
        tic.serverAddress = (args.length == 0) ? "localhost" : args[1];

        launch(args);
    }
}
