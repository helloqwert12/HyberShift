package chat;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PlanItem {
	@FXML
    private VBox vbox;

    @FXML
    private Label lblWork;

    @FXML
    private CheckBox cbDone;

    @FXML
    private Text lblPerformers;
    
    @FXML
    private Text lblStartDay;

    @FXML
    private Text lblEndDay;
    
    public PlanItem(){
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat/PlanItem.fxml"));
        fxmlLoader.setController(this);
        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setInfo(String work, String perfomers, boolean isDone, String start, String end){
    	lblWork.setText(work);
    	lblPerformers.setText(perfomers);
    	cbDone.setSelected(isDone);
    	lblStartDay.setText(start);
    	if (end == null || end == "0" || String.valueOf(end) == "0")
    		lblEndDay.setText("The task is on progress");
    	else
    		lblEndDay.setText(end);
    	
    }
    
    public VBox getVBox(){
    	return vbox;
    }

}
