
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
		double zoom = Double.parseDouble(args[1]);
		double rotationAngle = Double.parseDouble(args[2]);
		double curZoom = 1;
		double curRotationAngle = 0;

//		while(cnt-- > 0) {
//			curZoom  *= zoom;
//			curRotationAngle += rotationAngle;
//			double angleInRadians = Math.toRadians(curRotationAngle);
//			performImageAction(curZoom, angleInRadians, imgOne);
//			// Use label to display the image
//			frame = new JFrame();
//			GridBagLayout gLayout = new GridBagLayout();
//			frame.getContentPane().setLayout(gLayout);
//
//			lbIm1 = new JLabel(new ImageIcon(imgFinal));
//
//			GridBagConstraints c = new GridBagConstraints();
//			c.fill = GridBagConstraints.HORIZONTAL;
//			c.anchor = GridBagConstraints.CENTER;
//			c.weightx = 0.5;
//			c.gridx = 0;
//			c.gridy = 0;
//
//			c.fill = GridBagConstraints.HORIZONTAL;
//			c.gridx = 0;
//			c.gridy = 1;
//			frame.getContentPane().add(lbIm1, c);
//
//			frame.pack();
//			frame.setVisible(true);
//			Thread.sleep(2000);
//		}

// Create the frame once
		JFrame frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

// Assuming lbIm1 is declared outside the loop
		JLabel lbIm1 = new JLabel();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
// Inside the loop
		while (cnt-- > 0) {
			curZoom *= zoom;
			curRotationAngle += rotationAngle;
			double angleInRadians = Math.toRadians(curRotationAngle);
			performImageAction(curZoom, angleInRadians, imgOne);

			// Update the image in the existing JLabel
			lbIm1.setIcon(new ImageIcon(imgFinal));

			// Pause for 2 seconds
			Thread.sleep(2000);
		}

// Ensure the program exits when the window is closed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void performImageAction(double zoom, double angleInRadians, BufferedImage img) {

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int xt = x - width/2;
				int yt = y - height/2;
				double xOld = (1/zoom) * Math.cos(angleInRadians) * xt + (1/zoom) * Math.sin(angleInRadians) * yt + (width/2);
				double yOld = -1 * (1/zoom) * Math.sin(angleInRadians) * xt + (1/zoom) * Math.cos(angleInRadians) * yt + (height/2);
				int xOldInt = (int) Math.floor(xOld);
				int yOldInt = (int) Math.floor(yOld);
				if (xOldInt >= 0 && xOldInt < 512 && yOldInt >= 0 && yOldInt < 512) {
					setRGBValueFromOld(img, y, x, xOldInt, yOldInt, zoom);

				}
				else {
					imgFinal.setRGB(x, y, 0);
				}
//				int xo = x - (width / 2);
//				int yo = y - (height / 2);
//				double zoom = Double.parseDouble(z);
//				double angleInRadians = Math.toRadians(Double.parseDouble(r));
//				double xn = zoom * Math.cos(angleInRadians) * xo - zoom * Math.sin(angleInRadians) * yo + (width / 2);
//				double yn = zoom * Math.sin(angleInRadians) * xo + zoom * Math.cos(angleInRadians) * yo + (height / 2);
//				System.out.println(xo + " " + yo + "    " + xn + " " + yn + "   " + (int) xn + " " + (int) yn);
//				int rgb = img.getRGB(x, y);
//				int ceil_x = (int) Math.ceil(xn);
//				int ceil_y = (int) Math.ceil(yn);
//				int floor_x = (int) Math.floor(xn);
//				int floor_y = (int) Math.floor(yn);
//				int[] xs = new int[]{ceil_x, floor_x};
//				int[] ys = new int[]{ceil_y, floor_y};
//				for (int i = 0; i < 2; i++) {
//					for (int j = 0; j < 2; j++) {
//						if (xs[i] >= 0 && xs[i] < 512 && ys[j] >= 0 && ys[j] < 512) {
//							imgFinal.setRGB(xs[i], ys[j], rgb);
//						}
//					}
//				}
			}
		}

//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				int rgb = bilinearInterpolation(imgFinal, x, y, 512, 512);
//				imgFinal.setRGB(x, y, rgb);
//			}
//		}
	}

	private void setRGBValueFromOld(BufferedImage img, int y, int x, int xOldInt, int yOldInt, double zoom) {
		if (zoom < 1) {
			int rgb = img.getRGB(xOldInt, yOldInt);
			imgFinal.setRGB(x, y, rgb);
		}
		else {
			int filterFactor = (int)(1/zoom);
			int red = 0, blue = 0, green = 0;
			int cnt = 0;
			for(int i=xOldInt - filterFactor; i<=xOldInt+filterFactor; i++) {
				for (int j = yOldInt - filterFactor; j <= yOldInt + filterFactor; j++) {
					if (i >= 0 && i < 512 && j >= 0 && j < 512) {
						cnt++;
						int rgb = img.getRGB(i, j);
						red = red + ((rgb >> 16) & 0xFF);
						green = green + ((rgb >> 8) & 0xFF);
						blue = blue + (rgb & 0xFF);
					}
				}
			}
			float avgRed = red/cnt;
			float avgBlue = blue/cnt;
			float avgGreen = green/cnt;
			red = (int) (avgRed * 255);
			green = (int) (avgGreen * 255);
			blue = (int) (avgBlue * 255);

			int avgRGB = 0xff000000 | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
			imgFinal.setRGB(x, y, avgRGB);
		}
	}


	// Bilinear interpolation function
	public int bilinearInterpolation(BufferedImage image, double x, double y, int imageWidth, int imageHeight) {
		int x0 = (int) Math.floor(x);
		int x1 = x0 + 1;
		int y0 = (int) Math.floor(y);
		int y1 = y0 + 1;

		if (x1 >= imageWidth) x1 = x0;
		if (y1 >= imageHeight) y1 = y0;

		double xFraction = x - x0;
		double yFraction = y - y0;

		int rgb00 = image.getRGB(x0, y0);
		int rgb01 = image.getRGB(x0, y1);
		int rgb10 = image.getRGB(x1, y0);
		int rgb11 = image.getRGB(x1, y1);

		int red = (int) ((1 - xFraction) * (1 - yFraction) * ((rgb00 >> 16) & 0xFF) +
				xFraction * (1 - yFraction) * ((rgb10 >> 16) & 0xFF) +
				(1 - xFraction) * yFraction * ((rgb01 >> 16) & 0xFF) +
				xFraction * yFraction * ((rgb11 >> 16) & 0xFF));

		int green = (int) ((1 - xFraction) * (1 - yFraction) * ((rgb00 >> 8) & 0xFF) +
				xFraction * (1 - yFraction) * ((rgb10 >> 8) & 0xFF) +
				(1 - xFraction) * yFraction * ((rgb01 >> 8) & 0xFF) +
				xFraction * yFraction * ((rgb11 >> 8) & 0xFF));

		int blue = (int) ((1 - xFraction) * (1 - yFraction) * (rgb00 & 0xFF) +
				xFraction * (1 - yFraction) * (rgb10 & 0xFF) +
				(1 - xFraction) * yFraction * (rgb01 & 0xFF) +
				xFraction * yFraction * (rgb11 & 0xFF));

		return (red << 16) | (green << 8) | blue;
	}

	public static void main(String[] args) throws InterruptedException {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
