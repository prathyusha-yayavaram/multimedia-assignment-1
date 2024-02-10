import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgPerFrame;

	short avgRGB[][][];

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
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					// Setting initial value for image in first frame
					imgPerFrame.setRGB(x,y,pix);
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

		// Original Image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Transformed image per frame
		imgPerFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		readImageRGB(width, height, args[0], imgOne);

		// Precompute 3x3 avg RGB values
		avgRGB = new short[width][height][3];
		calculateAverages();

		// Initialize JFrame to display images
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

		lbIm1.setIcon(new ImageIcon(imgPerFrame));
		frame.getContentPane().add(lbIm1, c);
		frame.revalidate();
		frame.repaint();
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Get input values from args
		double zoom = Double.parseDouble(args[1]) - 1;
		double rotationAngle = Double.parseDouble(args[2]);
		int fps = Integer.parseInt(args[3]);

		double curZoom = 1;
		double curRotationAngle = 0;

		//Calculate zoom and rotation increment factor based on fps
		double zoomIncrementFactor = zoom/fps;
		double rotationIncrementFactor = rotationAngle/fps;

		long startTime = System.currentTimeMillis();
		while (curZoom > 0) {
			curZoom += zoomIncrementFactor;
			if (curZoom < 0) {
				curZoom = 0;
			}
			curRotationAngle += rotationIncrementFactor;
			double angleInRadians = Math.toRadians(curRotationAngle);

			//Set transformed image
			performImageTransformation(curZoom, angleInRadians);

			//Repaint the image in JFrame
			frame.repaint();
			frame.pack();

			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime - startTime;

			// Calculate sleep time to achieve given fps. 4 ms correction to fix time taken to calculate the time
			Thread.sleep(Math.max(0,(1000/fps) - elapsedTime - 4));

			frame.setVisible(true);

			startTime = System.currentTimeMillis();
		}
	}

	//Precompute 3x3 avg RGB values for each pixel in the original image
	private void calculateAverages() {
		int filterFactor = 1;
		for (int xOldInt = 0; xOldInt < width; xOldInt++) {
			for (int yOldInt = 0; yOldInt < height; yOldInt++) {
				int red = 0, blue = 0, green = 0;
				int cnt = 0;

				for (int i = xOldInt - filterFactor; i <= xOldInt + filterFactor; i++) {
					for (int j = yOldInt - filterFactor; j <= yOldInt + filterFactor; j++) {
						if (i >= 0 && i < width && j >= 0 && j < height) {
							cnt++;
							int rgb = imgOne.getRGB(i, j);
							red += (rgb >> 16) & 0xFF;
							green += (rgb >> 8) & 0xFF;
							blue += rgb & 0xFF;
						}
					}
				}
				avgRGB[xOldInt][yOldInt][0] = (short) (red/cnt);
				avgRGB[xOldInt][yOldInt][1] = (short) (green/cnt);
				avgRGB[xOldInt][yOldInt][2] = (short) (blue/cnt);
			}
		}
	}

	// Perform the image transformation using inverse mapping for given zoom and rotation step values
	private void performImageTransformation(double zoom, double angleInRadians) {
		double cosValue = Math.cos(angleInRadians);
		double sinValue = Math.sin(angleInRadians);
		double invZoom = 1 / zoom;
		int widthDiv2 = width / 2;
		int heightDiv2 = height / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int xt = x - widthDiv2;
				int yt = y - heightDiv2;

				//Inverse transformation and rounding to map new pixel to old pixel
				double xOld = invZoom * cosValue * xt + invZoom * sinValue * yt + widthDiv2;
				double yOld = -invZoom * sinValue * xt + invZoom * cosValue * yt + heightDiv2;
				int xOldInt = (int) Math.round(xOld);
				int yOldInt = (int) Math.round(yOld);

				if (zoom != 0 && xOldInt >= 0 && xOldInt < width && yOldInt >= 0 && yOldInt < height) {
					setRGBValueFromOld(y, x, xOldInt, yOldInt, zoom);
				} else {
					imgPerFrame.setRGB(x, y, 0);
				}
			}
		}
	}

	//Set RGB values based on zoom factor. Apply filter only when zooming out
	private void setRGBValueFromOld(int y, int x, int xOldInt, int yOldInt, double zoom) {
		if (zoom >= 1) {
			int rgb = imgOne.getRGB(xOldInt, yOldInt);
			imgPerFrame.setRGB(x, y, rgb);
		} else {
			int red = avgRGB[xOldInt][yOldInt][0], green = avgRGB[xOldInt][yOldInt][1], blue = avgRGB[xOldInt][yOldInt][2];
			int avgRGB = 0xff000000 | ((red) & 0xFF) << 16 | ((green) & 0xFF) << 8 | (blue) & 0xFF;
			imgPerFrame.setRGB(x, y, avgRGB);
		}
	}


	public static void main(String[] args) throws InterruptedException {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
