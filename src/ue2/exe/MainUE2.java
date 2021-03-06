package ue2.exe;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;

import javax.media.jai.PlanarImage;

import interfaces.Readable;
import ue2.filters.BallFilter;
import ue2.filters.CalcCentroidsFilter;
import ue2.filters.MedianFilter;
import ue2.filters.RegionOfInterestFilter;
import ue2.filters.ThresholdFilter;
import ue2.helpers.Coordinate;
import ue2.helpers.ImageSaver;
import ue2.helpers.ImageViewer;
import ue2.pipes.BufferedSyncPipe;
import ue2.pipes.ImageStreamSupplierPipe;
import ue2.pipes.OutputFileSink;

public class MainUE2 {

	public static void main(String[] args) {
		
		long timeTaskAPush = System.currentTimeMillis();
		runTaskAPush();
		System.out.println("Zeit Task a (push): " + (System.currentTimeMillis()-timeTaskAPush)+"ms");
		
		long timeTaskAPull = System.currentTimeMillis();	
//		runTaskAPull();
		System.out.println("Zeit Task a (pull): " + (System.currentTimeMillis()-timeTaskAPull)+"ms");

		long timeTaskB = System.currentTimeMillis();
//		 runTaskB();
		System.out.println("Zeit Task b: " + (System.currentTimeMillis()-timeTaskB)+"ms");

	}

	private static void runTaskAPush() {

		PlanarImage image = null;

		/* Startpunkt der ROI */
		Coordinate roiOrigin = new Coordinate(40, 50);
		
		LinkedList<Coordinate> coordinates = new LinkedList<>();
		/** Fill list of Coordinates **/
//		coordinates.add(new Coordinate(7,77));
		coordinates.add(new Coordinate(72, 77));
		coordinates.add(new Coordinate(137,81));
		coordinates.add(new Coordinate(202, 81));
		coordinates.add(new Coordinate(266, 80));
		coordinates.add(new Coordinate(330, 82));
		coordinates.add(new Coordinate(396, 81));

		/*
		 * Rectangle, das relevanten Bereich umschliesst: x= 40, y= 50, width=
		 * 390, height= 60
		 */
		Rectangle roiRectangle = new Rectangle(40, 50, 390, 60);

		/** source: image supplier pipe **/
		ImageStreamSupplierPipe imageStreamSupplierPipe = new ImageStreamSupplierPipe("loetstellen.jpg");
		BufferedSyncPipe<PlanarImage> endOfViewPipe = new BufferedSyncPipe<>(1);
//		BufferedSyncPipe<PlanarImage> thresholdPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> searchMedianPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> ballPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> centroidsPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<LinkedList<Coordinate>> coordinatesPipe = new BufferedSyncPipe<>(1);

		/*********** 1. das Bild laden und visualisieren */
//		try {
//			image = imageStreamSupplierPipe.read();
//		} catch (StreamCorruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		ImageSaver.save(image, "Original");
//		ImageViewer.show(image, "Original");

		/*********** 2. eine ROI (region of interest1) definieren */
		/** get ROI **/
		RegionOfInterestFilter roi = new RegionOfInterestFilter(imageStreamSupplierPipe, roiRectangle);
		

		/***********
		 * 3. einen Operator zur Bildsegmentierung ausw�hlen: Threshold Operator
		 * 3a. Parameterwerte des Operators w�hlen
		 */
		/* color values: lower range, upper range, end result */
		double[][] thresholdParameters = { new double[] { 0 }, new double[] { 28 }, new double[] { 255 } };

		ThresholdFilter thresholdFilter = new ThresholdFilter((Readable<PlanarImage>) roi, thresholdParameters);
//		PlanarImage thImage = thresholdFilter.getThImage(image);
//		ImageViewer.show(thImage, "ThresholdFilter");

		/***********
		 * 4. beseitige lokale St�rungen (z.B. schwarzer Fleck im 2. Anschluss
		 * von rechts) 
		 * 4a.w�hle Parameter des Filters: Gr��e der Maske zur
		 * Medianberechnung
		 */
		Integer maskSize = 6;
		
		MedianFilter medianFilter = new MedianFilter((Readable<PlanarImage>)thresholdFilter, maskSize);
//		PlanarImage medianImage = medianFilter.getMedianImage(thImage);
//		ImageViewer.show(medianImage, "MedianFilter");

		/***********
		 * 5. nun bleiben noch die Kabelanschl�sse der �balls�; man nutzt die
		 * Kreisform der Balls aus und benutzt einen Opening-Operator mit
		 * kreisf�rmiger Maske (in JAI: "erode" und �dilate�):
		 *
		 * 5a. w�hle Parameter des Operators: Gr��e der Maske (Alternative:
		 * laufe mehrmals mit dem Operator �ber das Bild)
		 *
		 * 6.Resultatbild (ein Bild, in dem nur die �balls� als Scheiben zu
		 * sehen sind.)in einer Datei abspeichern, aber nicht als Sink
		 * realisieren, sondern nach der Abspeicherung das unver�nderte Bild
		 * weiterleiten.
		 */
		
		BallFilter ballFilter = new BallFilter((Readable<PlanarImage>)medianFilter);
//		PlanarImage ballImage = ballFilter.getBallImage(medianImage);
//		ImageViewer.show(ballImage, "BallFilter");
		
		/**********
		 * 7.Scheiben z�hlen, ihre Zentren bestimmen,
		 * und pr�fen, ob sie im Toleranzbereich der Qualit�tskontrolle liegen.
		 * Letztere Information wird bei Erzeugung des Filters im "main" als
		 * Initialisierungsdaten an das Filterobjekt �bergeben. Resultat in eine
		 * txt Datei schreiben.
		 */
		
		//LinkedList<Coordinate> results = new LinkedList<>();
		CalcCentroidsFilter calcFilter = new CalcCentroidsFilter(ballFilter);
		
		// TODO check how list can be saved with sink
		new OutputFileSink(calcFilter, "results.txt").run();
	}

	private static void runTaskAPull() {

		// TODO:
		PlanarImage thImage = null;
		PlanarImage medianImage = null;
		PlanarImage image = null;
		
		/* Startpunkt der ROI */
		Coordinate roiOrigin = new Coordinate(40, 50);
		
		List<Coordinate> coordinates = new LinkedList<>();
		/** Fill list of Coordinates **/
		coordinates.add(new Coordinate(7,77));
		coordinates.add(new Coordinate(72, 77));
		coordinates.add(new Coordinate(137,81));
		coordinates.add(new Coordinate(202, 81));
		coordinates.add(new Coordinate(266, 80));
		coordinates.add(new Coordinate(330, 82));
		coordinates.add(new Coordinate(396, 81));
		
		/** source: image supplier pipe **/
		ImageStreamSupplierPipe imageStreamSupplierPipe = new ImageStreamSupplierPipe("loetstellen.jpg");
		BufferedSyncPipe<PlanarImage> endOfViewPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> thresholdPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> searchMedianPipe = new BufferedSyncPipe<>(1);
		
		/*
		 * Rectangle, das relevanten Bereich umschliesst: x= 40, y= 50, width=
		 * 390, height= 60
		 */
		Rectangle roiRectangle = new Rectangle(40, 50, 390, 60);
		
		/** Write result to file **/
		
		
		/** Calculate Centeroids **/
		
		
		/** Balls **/
		BallFilter ballFilter = new BallFilter(searchMedianPipe, endOfViewPipe);
		PlanarImage ballImage = ballFilter.getBallImage(medianImage);
		ImageSaver.save(ballImage, "BallFilter");
		ImageViewer.show(ballImage, "BallFilter");
		
		/** Median Filter **/
		Integer maskSize = 6;
		
		MedianFilter medianFilter = new MedianFilter(searchMedianPipe, new BufferedSyncPipe<PlanarImage>(1), maskSize);
		medianImage = medianFilter.getMedianImage(thImage);
		ImageSaver.save(medianImage, "MedianFilter");
		ImageViewer.show(medianImage, "MedianFilter");

		
		/** Threshold Filter **/
		/* color values: lower range, upper range, end result */
		double[][] thresholdParameters = { new double[] { 0 }, new double[] { 28 }, new double[] { 255 } };

		ThresholdFilter thresholdFilter = new ThresholdFilter(thresholdPipe, searchMedianPipe, thresholdParameters);
		thImage = thresholdFilter.getThImage(image);
		ImageViewer.show(thImage, "ThresholdFilter");
		
		/** ROI **/
		String roiFilter = "RegionOfInterestFilter";
		image = PlanarImage
				.wrapRenderedImage((RenderedImage) image.getAsBufferedImage(roiRectangle, image.getColorModel()));
		ImageSaver.save(image, roiFilter);
		ImageViewer.show(image, roiFilter);

		/** load Data **/
		try {
			image = imageStreamSupplierPipe.read();
		} catch (StreamCorruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		ImageSaver.save(image, "Original");
		ImageViewer.show(image, "Original");
	}

	/**
	 * Threaded Task
	 */
	private static void runTaskB() {
		
		PlanarImage image = null;
		
		/* Startpunkt der ROI */
		Coordinate roiOrigin = new Coordinate(40, 50);
		
		List<Coordinate> coordinates = new LinkedList<>();
		/** Fill list of Coordinates **/
		coordinates.add(new Coordinate(7,77));
		coordinates.add(new Coordinate(72, 77));
		coordinates.add(new Coordinate(137,81));
		coordinates.add(new Coordinate(202, 81));
		coordinates.add(new Coordinate(266, 80));
		coordinates.add(new Coordinate(330, 82));
		coordinates.add(new Coordinate(396, 81));
		
		/** Pipes **/
		/** source: image supplier pipe **/
		ImageStreamSupplierPipe imageStreamSupplierPipe = new ImageStreamSupplierPipe("loetstellen.jpg");
		BufferedSyncPipe<PlanarImage> endOfViewPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> thresholdPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> searchMedianPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> ballPipe = new BufferedSyncPipe<>(1);
		BufferedSyncPipe<PlanarImage> resultPipe = new BufferedSyncPipe<>(1);
		
		/*********** 1. das Bild laden und visualisieren */
		try {
			image = imageStreamSupplierPipe.read();
		} catch (StreamCorruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		ImageSaver.save(image, "Original");
		ImageViewer.show(image, "Original");

		/*********** 2. eine ROI (region of interest) definieren */
		/*
		 * Rectangle, das relevanten Bereich umschliesst: x= 40, y= 50, width=
		 * 390, height= 60
		 */
		Rectangle roiRectangle = new Rectangle(40, 50, 390, 60);

		new Thread(
				new RegionOfInterestFilter(imageStreamSupplierPipe, thresholdPipe, roiRectangle)
		).start();

		/************* 3. Bildsegmentierung Threshold Operator ************/
		double[][] thresholdParameters = { new double[] { 0 }, new double[] { 28 }, new double[] { 255 } };
		new Thread(
				new ThresholdFilter(thresholdPipe, searchMedianPipe, thresholdParameters)
		).start();
		
		/***********
		 * 4. beseitige lokale St�rungen (z.B. schwarzer Fleck im 2. Anschluss
		 * von rechts) 
		 * 4a.w�hle Parameter des Filters: Gr��e der Maske zur
		 * Medianberechnung
		 */
		Integer maskSize = 6;
		
		new Thread(
				new MedianFilter(searchMedianPipe, ballPipe, maskSize)
		).start();
		
		/***********
		 * 5. nun bleiben noch die Kabelanschl�sse der �balls�; man nutzt die
		 * Kreisform der Balls aus und benutzt einen Opening-Operator mit
		 * kreisf�rmiger Maske (in JAI: "erode" und �dilate�):
		 *
		 * 5a. w�hle Parameter des Operators: Gr��e der Maske (Alternative:
		 * laufe mehrmals mit dem Operator �ber das Bild)
		 */
		new Thread(
				new BallFilter(ballPipe, resultPipe)
		).start();
		
		
		/**********
		 * 6.Resultatbild (ein Bild, in dem nur die �balls� als Scheiben zu
		 * sehen sind.)in einer Datei abspeichern, aber nicht als Sink
		 * realisieren, sondern nach der Abspeicherung das unver�nderte Bild
		 * weiterleiten.
		 */
		
		

		/**********
		 * 7.Scheiben z�hlen, ihre Zentren (Centroid, siehe unten) bestimmen,
		 * und pr�fen, ob sie im Toleranzbereich der Qualit�tskontrolle liegen.
		 * Letztere Information wird bei Erzeugung des Filters im "main" als
		 * Initialisierungsdaten an das Filterobjekt �bergeben. Resultat in eine
		 * txt Datei schreiben.
		 */
		
		 
	}
}
