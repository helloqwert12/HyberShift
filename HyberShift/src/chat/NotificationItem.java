package chat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import Tools.ImageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class NotificationItem {
	@FXML
	private VBox vbox;
	
	@FXML
    private Circle cimgPic;

    @FXML
    private Label lblType;

    @FXML
    private Label lblTimestamp;

    @FXML
    private Label lblContent;
	
	public NotificationItem(){
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat/NotificationItem.fxml"));
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
	
	public void setInfo(String name, String content, String type, String imgstring, int timestamp) throws IOException{
		lblType.setText(type);
		lblContent.setText(name + " " + content);
		if (!imgstring.equals("null")){
			cimgPic.setFill(new ImagePattern(ImageUtils.decodeBase64BinaryToImage(imgstring)));
		}
		if (timestamp != 0){
			//Formmated timestamp
	    	Date date = new Date(timestamp);
	    	DateFormat formatter = new SimpleDateFormat("EE h:mm a");
	    	String dateFormatted = formatter.format(date);
	    	
			lblTimestamp.setText(String.valueOf(dateFormatted));
		}
		else{
			lblTimestamp.setVisible(false);
		}
	}
	
	public VBox getVBox(){
		return vbox;
	}
	
}
