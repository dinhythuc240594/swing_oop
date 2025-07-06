package com.project01;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class HandlerImage {
	
	public byte[] exportImage(JLabel labelImage, File selectedFile) {
		// read and resize image
		BufferedImage originalImage;
		try {
			originalImage = ImageIO.read(selectedFile);
			BufferedImage resizedImage = resizeImage(originalImage, 200, 200);
			
			// update preview
			ImageIcon icon = new ImageIcon(resizedImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
			labelImage.setIcon(icon);
			labelImage.setText("");
			
			// convert to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(resizedImage, "PNG", baos);
			byte[] avatarImage = baos.toByteArray();
			return avatarImage;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public BufferedImage resizeImage(BufferedImage origImage, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = resizedImage.createGraphics();
		
		graphics2D.drawImage(origImage, 0, 0, width, height, null);
		graphics2D.dispose();
		return resizedImage;
	}
	
	public byte[] fileImage(String strPath, int width, int height) {
		try {

			//// 2025-05-31 - get file from resource project ////
			File file = new File(this.getClass().getResource(strPath).getFile());
			if(file.exists()) {
				
				// read and resize image
				BufferedImage originalImage = ImageIO.read(file);
				BufferedImage resizedImage = resizeImage(originalImage, width, height);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(resizedImage, "PNG", baos);
				byte[] avatarImage = baos.toByteArray();
				return avatarImage;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
