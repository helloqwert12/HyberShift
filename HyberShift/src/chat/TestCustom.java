package chat;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class TestCustom implements Initializable{
	@FXML
    private Button button;

    @FXML
    private Label label;
    
    @FXML
    private AnchorPane pnlTest;
    
    public TestCustom(){
//    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat/TestCustom.fxml"));
//    	fxmlLoader.setRoot(this);
//        fxmlLoader.setController(this);
//        try
//        {
//            fxmlLoader.load();
//        }
//        catch (IOException e)
//        {
//            throw new RuntimeException(e);
//        }
    }

    @FXML
    void onActionButton(ActionEvent event) {
    	label.setText("You click on the button");
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}

}
