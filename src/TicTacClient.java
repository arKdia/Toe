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
import javax.swing.ImageIcon;

/**
 *
 * @author Mario
 */
public class TicTacClient extends Application {
    private Label messagelabel;
    GridPane pane;
    Button[] buttonList;
    Button b;
    
    private Socket socket;
    private static int PORT = 8901;
    private BufferedReader in;
    private PrintWriter out;
    
    char mark = ' ';
    char opp  = ' ';
    
    String response;
    
 
    public void startNetBoard(String serverAddress) throws Exception {
       // --Network--
       //String serverAddress;
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
        
        String serverAddress=" ";
        
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Welcome to TicTacToe");
        dialog.setHeaderText(null);
        dialog.setContentText("How would you like to play? <localhost or IP>");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            serverAddress = result.get();
        }
        
        try {
            startNetBoard(serverAddress);
        } catch (Exception ex) {
            System.err.println("Err creating network-board: " + ex);
        }
       
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                mark = response.charAt(8);
                opp = (mark == 'X' ? 'O' : 'X');
                System.out.println("this user is: " + mark);
            }
        } catch (Exception ex) {
            System.err.println("Err communication play()" + ex);
        }
        
        primaryStage.setTitle("Tic Tac Toe - Player: " + mark);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(mark == 'X' ? "x.png" : "o.png")));
        Scene scene = new Scene(pane, 300, 320);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(TicTacClient.class.getResource("toe.css").toExternalForm());
        primaryStage.show();
   
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
        
        //response = 
    }
    
    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
             
            
            
        }
    });
    
    final EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>() {
        //char token = ' ';
        //boolean winner = false;

        @Override
        public void handle(ActionEvent event) {
            try {
                b = (Button) event.getSource();
                //b1 = (Button) event.getSource();
                
                //b.setDisable(true);
                System.out.printf("\nMove: %d\n", Integer.parseInt(b.getId()));
                out.println("MOVE "+ Integer.parseInt(b.getId()));
                
                try {
                    response = in.readLine();
                } catch (IOException ex) {
                    System.err.println("Error reading response " + ex);
                }
                System.out.printf("\nResponse: %s", response);
                //messagelabel.setText(response);
                
                if (response.startsWith("VALID_MOVE")){
                    messagelabel.setText("Valid move, please wait");
                    //System.out.println("Valid move, please wait");
                    b.setText(Character.toString(mark));
                } else if (response.startsWith("OPPONENT_MOVED")){
                    int loc = Integer.parseInt(response.substring(15));
                    buttonList[loc].setText(Character.toString(opp));
                    messagelabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")){
                    messagelabel.setText("You win");
                    /*if(playAgain())
                       startGUI();
                    else
                        out.println("QUIT");*/
                    return;
                } /*else if(response.startsWith("DEFEAT")){
                    messagelabel.setText("You lose");
                    return;
                } else if (response.startsWith("TIE")) {
                    messagelabel.setText("You tied");
                    return;
                } else if (response.startsWith("MESSAGE")) {
                    messagelabel.setText(response.substring(8));
                }
                out.println("QUIT");*/
            } finally {
                /*try {
                    //socket.close();
                } catch (IOException ex) {
                    System.err.println("err closing socket: " +  ex);
                }*/
            }
            
        }
    };
    

     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String serverAddress = (args.length == 0) ? "localhost" : args[1];
        TicTacClient tic = new TicTacClient();
        tic.t.start();
        launch(args);
    }
    
}
