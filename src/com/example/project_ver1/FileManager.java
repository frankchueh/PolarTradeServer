package com.example.project_ver1;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;


public class FileManager {

	private FileReader reader;
	private FileWriter writer;
	private File f;
	private FileOutputStream fos;
	public String savePath;
	private BufferedReader br;
	FileManager(String filename)
	{
		try{
		savePath = "C:/DataBase"+filename;
		f = new File(savePath);
			if(!f.exists())
			{
				//f.mkdir();
				f.createNewFile();
			}
		}
		catch(Exception e)
		{
		e.printStackTrace();
		}
	}
	

	
	public String[] readAllLine()
	{	
		String temp,line="";
		try{
			
			reader = new FileReader(savePath);
			br = new BufferedReader(reader);
			
			while((temp=br.readLine())!=null)
			{
				line+=temp+"\n";
			}
			br.close();
			reader.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return line.split("\n");
	}
	
	public void writeAllLine(String[] data)
	{	
		String line="";
		for(int i=0;i<data.length;i++)
		{
			line+=data[i]+"\r\n";
		}
		try{
			writer = new FileWriter(savePath);
			writer.write(line);
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeLine(String data)
	{
		String[] origin = readAllLine();
		String line="";
		for(int i=0;i<origin.length;i++)
		{
			line+=origin[i]+"\r\n";
		}
		line+=data+"\r\n";
		try{
			writer = new FileWriter(savePath);
			writer.write(line);
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeObject(byte[] buffer) throws IOException
	{
		try {
			fos = new FileOutputStream(savePath);
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writePhoto(byte[] buffer, float compress_rate) throws IOException
	{
		try {
			fos = new FileOutputStream(savePath);
			buffer = ImageCompress(buffer, compress_rate);
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Clear()
	{
		try {
			writer = new FileWriter(savePath);
			writer.write("");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("restriction")
	public byte[] ImageCompress(byte[] photo, float CompressRate)
	{
		
		if(CompressRate > 1.0f)
			CompressRate = 1.0f;
		else if (CompressRate < 0.0f)
			CompressRate = 0.0f;
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();

		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(CompressRate); // Change this, float between 0.0 and 1.0

		try {
			InputStream in = new ByteArrayInputStream(photo);
			BufferedImage image = ImageIO.read(in);
			image = resizeImage(image, image.getType(), image.getWidth()/10, image.getHeight()/10);
			writer.setOutput(ImageIO.createImageOutputStream(os));
			writer.write(null, new IIOImage(image, null, null), param);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.dispose();
		return os.toByteArray();
	}
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height){
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	    }
	
	
//	public static void main(String[] args) {
//			FileManager f = new FileManager();
//			String[] s = f.readAllLine();
//			f.writeLine("いゅ代刚");
//			f.writeLine("いゅ代刚");
//			f.writeLine("いゅ代刚");
//			f.writeLine("いゅ代刚");
//			for(int i=0;i<s.length;i++)
//			{
//				System.out.println(s[i]);
//			}
//			
//	}
	
	
}
