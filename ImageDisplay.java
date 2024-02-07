
import sun.rmi.server.Activation$ActivationSystemImpl_Stub;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgFinal;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void showIms(String[] args) throws InterruptedException {

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgFinal = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);
		int cnt = 5;

		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		lbIm1.setIcon(new ImageIcon(imgFinal));
		frame.getContentPane().add(lbIm1, c);
		frame.revalidate();
		frame.repaint();
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		double zoom = Double.parseDouble(args[1]) - 1;
		double rotationAngle = Double.parseDouble(args[2]);
		double curZoom = 1;
		double curRotationAngle = 0;
		int fps = Integer.parseInt(args[3]);
		double zoomIncrementFactor = zoom/fps;
		double rotationIncrementFactor = rotationAngle/fps;
		long startTime = System.currentTimeMillis();
		while (curZoom > 0 && curZoom < 50) {

			curZoom += zoomIncrementFactor;
			// Check if curZoom is very close to zero and consider it as zero
			if (curZoom < 0) {
				curZoom = 0;
			}
			curRotationAngle += rotationIncrementFactor;
			double angleInRadians = Math.toRadians(curRotationAngle);
			performImageAction(curZoom, angleInRadians, imgOne);

			// Update the image in the existing JLabel
			lbIm1.setIcon(new ImageIcon(imgFinal));
			frame.getContentPane().add(lbIm1, c);
			frame.revalidate();
			frame.repaint();
			frame.pack();
			frame.setVisible(true);

			// Pause for 2 seconds
			Thread.sleep((long)Math.max(0,(1000/fps)-33.33));
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("Time taken: " + elapsedTime + " milliseconds");

// Ensure the program exits when the window is closed

	}

	private void performImageAction(double zoom, double angleInRadians, BufferedImage img) {
		double cosValue = Math.cos(angleInRadians);
		double sinValue = Math.sin(angleInRadians);
		double invZoom = 1 / zoom;
		int widthDiv2 = width / 2;
		int heightDiv2 = height / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int xt = x - widthDiv2;
				int yt = y - heightDiv2;
				double xOld = invZoom * cosValue * xt + invZoom * sinValue * yt + widthDiv2;
				double yOld = -invZoom * sinValue * xt + invZoom * cosValue * yt + heightDiv2;
				int xOldInt = (int) Math.floor(xOld);
				int yOldInt = (int) Math.floor(yOld);

				if (zoom != 0 && xOldInt >= 0 && xOldInt < 512 && yOldInt >= 0 && yOldInt < 512) {
					setRGBValueFromOld(img, y, x, xOldInt, yOldInt, zoom);
				} else {
					imgFinal.setRGB(x, y, 0);
				}
			}
		}
	}

	private void setRGBValueFromOld(BufferedImage img, int y, int x, int xOldInt, int yOldInt, double zoom) {
		if (zoom > 1) {
			int rgb = img.getRGB(xOldInt, yOldInt);
			imgFinal.setRGB(x, y, rgb);
		} else {
			int filterFactor = Math.max((int) (1 / zoom), 1);
			int red = 0, blue = 0, green = 0;
			int cnt = 0;

			for (int i = xOldInt - filterFactor; i <= xOldInt + filterFactor; i++) {
				for (int j = yOldInt - filterFactor; j <= yOldInt + filterFactor; j++) {
					if (i >= 0 && i < 512 && j >= 0 && j < 512) {
						cnt++;
						int rgb = img.getRGB(i, j);
						red += (rgb >> 16) & 0xFF;
						green += (rgb >> 8) & 0xFF;
						blue += rgb & 0xFF;
					}
				}
			}

			int avgRGB = 0xff000000 | ((red / cnt) & 0xFF) << 16 | ((green / cnt) & 0xFF) << 8 | (blue / cnt) & 0xFF;
			imgFinal.setRGB(x, y, avgRGB);
		}
	}


	public static void main(String[] args) throws InterruptedException {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
