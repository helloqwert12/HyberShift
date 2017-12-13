package board;

import java.awt.Point;
import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

public class PenDrawing {
	ArrayList<Point> listPoints;
	
	public PenDrawing(){
		listPoints = new ArrayList<>();
	}

	
	public PenDrawing(ArrayList<Point> sourcePoints){
		this.listPoints = sourcePoints;
	}
	
	public void addPoint(Point point){
		listPoints.add(point);
	}
	
	public void setListPoints(ArrayList<Point> sourcePoints){
		this.listPoints.clear();
		this.listPoints = sourcePoints;
	}
	
	public ArrayList<Point> getListPoints(){
		return this.listPoints;
	}
	
	public void draw(GraphicsContext gc){
		for(int i=0; i<listPoints.size() - 1; i++){
			gc.strokeLine(listPoints.get(i).x, listPoints.get(i).y, listPoints.get(i+1).x, listPoints.get(i+1).y);
		}
	}
	
	public void clear(){
		listPoints.clear();
	}
}
