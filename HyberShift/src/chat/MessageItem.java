package chat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dataobject.Message;
import Tools.ImageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class MessageItem {
	@FXML
	private VBox vbox;
	
    @FXML
    private Circle cimgPic;

    @FXML
    private Label lblMessage;
    
    @FXML
    private Label lblTimestamp;
    
    @FXML private Label lblSender;
    
    public MessageItem(){
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat/MessageItem.fxml"));
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
    
    public void setInfo(String imgstring, String message, int timestamp) throws IOException{
    	if (!imgstring.equals("null"))
    		cimgPic.setFill(new ImagePattern(ImageUtils.decodeBase64BinaryToImage(imgstring)));
    	lblMessage.setText(message);
    	
    	//Formmated timestamp
    	Date date = new Date(timestamp);
    	DateFormat formatter = new SimpleDateFormat("EE h:mm a");
    	String dateFormatted = formatter.format(date);
    	
    	lblTimestamp.setText(dateFormatted);
    }
    
    public void setInfo(Message message) throws IOException{
    	if (!message.getImgString().equals("null"))
    		cimgPic.setFill(new ImagePattern(ImageUtils.decodeBase64BinaryToImage(message.getImgString())));
    	
    	lblSender.setText(message.getSender());
    	lblMessage.setText(message.getMessage());
    	
    	//Formmated timestamp
    	Date date = new Date(message.getTimestamp());
    	DateFormat formatter = new SimpleDateFormat("EE h:mm a");
    	String dateFormatted = formatter.format(date);
    	
    	lblTimestamp.setText(dateFormatted);
    }
    
    public VBox getVBox(){
    	return vbox;
    }
}
