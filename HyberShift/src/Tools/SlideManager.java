package Tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;


public class SlideManager {
//	private static SlideManager instance = null;
//	
//	public SlideManager(){
//		
//	}
//	
//	public static SlideManager getInstance(){
//		if (instance == null)
//			instance = new SlideManager();
//		return instance;
//	}
//	
	public void convertSlideToImg(String inputPath) throws IOException{
		//creating an empty presentation
	      File file = new File(inputPath);
	      XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file));
	      	
	      //getting the dimensions and size of the slide 
	      Dimension pgsize = ppt.getPageSize();
	      List<XSLFSlide> slide = ppt.getSlides();
	      
	      for (int i = 0; i < slide.size(); i++) {
	         BufferedImage img = new BufferedImage(
	            pgsize.width, pgsize.height,BufferedImage.TYPE_INT_RGB);
	         Graphics2D graphics = img.createGraphics();

	         //clear the drawing area
	         graphics.setPaint(Color.white);
	         graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

	         //render
	         slide.get(i).draw(graphics);
	         
	       //creating an image file as output
		      FileOutputStream out = new FileOutputStream("ppt_image_" + i + ".png");
		      javax.imageio.ImageIO.write(img, "png", out);
		      ppt.write(out);
		      out.close();
	      }
	      
	      System.out.println("Image successfully created");
	}
	
	public static ArrayList<Image> convertSlideToImage(String inputPath) throws FileNotFoundException, IOException{
		ArrayList<Image> list = new ArrayList<>();
		
		//creating an empty presentation
	      File file = new File(inputPath);
	      XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file));
	      	
	      //getting the dimensions and size of the slide 
	      Dimension pgsize = ppt.getPageSize();
	      List<XSLFSlide> slide = ppt.getSlides();
	      
	      for (int i = 0; i < slide.size(); i++) {
	         BufferedImage img = new BufferedImage(
	            pgsize.width, pgsize.height,BufferedImage.TYPE_INT_RGB);
	         Graphics2D graphics = img.createGraphics();

	         //clear the drawing area
	         graphics.setPaint(Color.white);
	         graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

	         //render
	         slide.get(i).draw(graphics);
	         
	         //add to list
	         list.add(SwingFXUtils.toFXImage(img, null));
	      }
	      
	      ppt.close();
	      System.out.println("Convert slide to image successfully created");
		
		return list;
	}
}
