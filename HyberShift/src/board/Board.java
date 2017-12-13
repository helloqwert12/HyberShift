package board;

import javafx.scene.canvas.GraphicsContext;

public class Board {
	GraphicsContext gc;
	PenDrawing penDrawing;
	
	public Board(){
		penDrawing = new PenDrawing();
	}
	
	public Board(GraphicsContext gc){
		this.penDrawing = new PenDrawing();
		this.gc = gc;
	}
	
	
}
