/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.gesture.features;

import java.awt.Point;

import com.gt.util.ArrayWriter;

/**
 * Reference:
 * http://st04110083.etu.edu.tr/makale/Hand%20gesture%20recognition%20
 * using%20combined%20features%20of%20location,%20angle%20and%20velocity.pdf
 */
public class GestureFeatureExtractor {

	private static final int QUANTIZE_ANGLE = 10;
	private GestureFeature[] extractedFeature;
	private RawFeature inputRawFeature;
	private double[] locationRelativeToCG;
	private double[] distanceBetweenSuccessivePts;
	private double[] timeDiffs;
	private double[] angleWithCG;
	// private double[] angleWithSuccessivePts;
	private double[] angleWithInitialPt;
	private double[] angleWithEndPt;
	private double[] velocities;
	double[] xMinyMinAngle;
	double[] xMinyMaxAngle;
	double[] xMaxyMinAngle;
	double[] xMaxyMaxAngle;
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	private Point center;
	// private int[] chainCodeOfAngles;
	private int SAMPLE_PER_FRAME = 3;
	private int CHAIN_CODE_NUM_DIRECTIONS = 16;
	private double[] framed_curTime;
	private Point[] framed_drawPoint;
	// private double[] chainCodeOfAngleWithCG;
	// private double[] chainCodeOfAngleWithSuccessivePts;
	private int framedSamples;

	public GestureFeatureExtractor(RawFeature inputRawFeature) {
		this.inputRawFeature = inputRawFeature;
		calculateFeature();
	}

	private void calculateFeature() {
		// preprocess-- framing
		// new , from reference
		framing();
		calculateCenterAndBounds();
		calculatePositionDistanceAngle();
		normalizeFeatures();

		// TODO: chain code is currently not implemented
		// chainCodeOfAngleWithCG=quantizeAngle(angleWithCG);
		// chainCodeOfAngleWithSuccessivePts=quantizeAngle(angleWithCG);
		composeFeatureVector();
		// printVectors();

	}

	private void printVectors() {
		// System.out.println("angle with successive");
		// ArrayWriter.printDoubleArrayToConole(angleWithSuccessivePts);
		System.out.println("angle with cg");
		ArrayWriter.printDoubleArrayToConole(angleWithCG);
		ArrayWriter.printDoubleArrayToConole(angleWithInitialPt);

		ArrayWriter.printDoubleArrayToConole(angleWithEndPt);

	}

	private void framing() {
		//
		int totalSample = inputRawFeature.getCurTime().length;
		int framedSample = totalSample / getSAMPLE_PER_FRAME();
		framedSample = framedSample - framedSample % getSAMPLE_PER_FRAME();
		framed_curTime = new double[framedSample];
		framed_drawPoint = new Point[framedSample];
		for (int i = 0; i < framedSample; i++) {
			framed_curTime[i] = inputRawFeature.getCurTime()[i * getSAMPLE_PER_FRAME()];
			framed_drawPoint[i] = inputRawFeature.getDrawPoint()[i * getSAMPLE_PER_FRAME()];
		}
		framedSamples = totalSample / getSAMPLE_PER_FRAME();
		// define vars, with changed size after frameing
		locationRelativeToCG = new double[framedSamples];
		distanceBetweenSuccessivePts = new double[framedSamples];

		angleWithCG = new double[framedSamples];
		// angleWithSuccessivePts = new double[framedSamples];
		angleWithInitialPt = new double[framedSamples];
		angleWithEndPt = new double[framedSamples];
		timeDiffs = new double[framedSamples];
		center = new Point();
		velocities = new double[framedSamples];
		xMinyMinAngle = new double[framedSamples];
		xMinyMaxAngle = new double[framedSamples];
		xMaxyMaxAngle = new double[framedSamples];
		xMaxyMinAngle = new double[framedSamples];
	}

	private void calculateCenterAndBounds() {
		int sX = 0, sY = 0;
		int n = inputRawFeature.getDrawPoint().length;
		xMin = 0;
		yMin = 0;
		xMax = 0;
		yMax = 0;
		for (int i = 0; i < n; i++) {
			double curX = inputRawFeature.getDrawPoint()[i].getX();
			double curY = inputRawFeature.getDrawPoint()[i].getY();
			if (curX < xMin) {
				xMin = curX;
			}
			else if (curX > xMax) {
				xMax = curX;
			}
			if (curY < yMin) {
				yMin = curY;
			}
			else if (curY > yMax) {
				yMax = curY;
			}
			sX += curX;
			sY += curY;
		}
		center.x = sX / n;
		center.y = sY / n;
	}

	private void calculatePositionDistanceAngle() {
		double initXo = framed_drawPoint[0].getX();
		double initYo = framed_drawPoint[0].getY();
		double initXn = framed_drawPoint[framed_drawPoint.length - 1].getX();
		double initYn = framed_drawPoint[framed_drawPoint.length - 1].getY();
		for (int i = 0; i < framed_curTime.length - 1; i++) {
			/** Geometry **/
			// get values
			Point curPt = framed_drawPoint[i];
			double dxC = (curPt.getX() - center.getX());
			double dyC = (curPt.getY() - center.getY());

			double sqSum = Math.pow(dxC, 2) + Math.pow(dyC, 2);
			locationRelativeToCG[i] = Math.sqrt(sqSum);
			angleWithCG[i] = getAngleYbyX(dyC, dxC);
			// with successive
			Point p2 = framed_drawPoint[i + 1];
			double dxSu = (curPt.getX() - p2.getX());
			double dySu = (curPt.getY() - p2.getY());
			// angleWithSuccessivePts[i] = getAngleYbyX(dySu, dxSu);
			distanceBetweenSuccessivePts[i] = Math.sqrt(Math.pow(dxSu, 2) + Math.pow(dySu, 2));
			angleWithInitialPt[i] = getAngleYbyX(curPt.getY() - initYo, curPt.getX() - initXo);
			angleWithEndPt[i] = getAngleYbyX(curPt.getY() - initYn, curPt.getX() - initXn);
			/** Kinematics **/
			double r1 = framed_curTime[i];
			double r2 = framed_curTime[i + 1];
			// time diff in two readings
			timeDiffs[i] = r2 - r1;
			velocities[i] = divide(distanceBetweenSuccessivePts[i], timeDiffs[i]);
			// angle with corners
			xMinyMinAngle[i] = getAngleYbyX(curPt.getY() - yMin, curPt.getX() - xMin);
			xMinyMaxAngle[i] = getAngleYbyX(curPt.getY() - yMax, curPt.getX() - xMin);
			xMaxyMinAngle[i] = getAngleYbyX(curPt.getY() - yMax, curPt.getX() - xMin);
			xMaxyMaxAngle[i] = getAngleYbyX(curPt.getY() - yMax, curPt.getX() - xMax);
		}

	}

	private double divide(double num, double denom) {
		if (denom == 0) {
			return 0.0;
		}
		else {
			return num / denom;
		}
	}

	/**
	 * also quantizes the angle
	 * 
	 * @param dy
	 * @param dx
	 * @return
	 */
	private double getAngleYbyX(double dy, double dx) {
		// quantize too
		double angleD = (Math.toDegrees(Math.atan(divide(dy, dx))) / QUANTIZE_ANGLE);
		System.out.println(angleD + "         " + Math.floor(angleD));
		return Math.ceil(angleD);
	}

	/** post process **/
	private void normalizeFeatures() {
		// location and distances
		// time
		// velocity
		double maxLoc = findMax(locationRelativeToCG);
		double maxDist = findMax(distanceBetweenSuccessivePts);
		double minLoc = findMin(locationRelativeToCG);
		double minDist = findMin(distanceBetweenSuccessivePts);
		double maxTimDiff = findMax(timeDiffs);
		double maxVelocity = findMax(velocities);
		for (int i = 0; i < velocities.length; i++) {
			// normalize
			locationRelativeToCG[i] = divide(locationRelativeToCG[i] - minLoc, maxLoc - minLoc);
			distanceBetweenSuccessivePts[i] = divide(distanceBetweenSuccessivePts[i] - minDist, maxDist - minDist);
			// simple div
			timeDiffs[i] = divide(timeDiffs[i], maxTimDiff);
			velocities[i] = divide(velocities[i], maxVelocity);

		}
	}

	// TODO: chain code is currently not used
	// simple quantization is done in
	private double[] quantizeAngle(double[] theta) {
		double phi = 360 / getCHAIN_CODE_NUM_DIRECTIONS();
		double[] code = new double[theta.length];
		for (int j = 0; j < theta.length; j++) {
			for (int i = 0; i < getCHAIN_CODE_NUM_DIRECTIONS(); i++) {
				// lower bound
				double shiL = 360 / getCHAIN_CODE_NUM_DIRECTIONS() * (i - getCHAIN_CODE_NUM_DIRECTIONS() / 2);
				// upper bound
				double shiU = 360 / getCHAIN_CODE_NUM_DIRECTIONS() * (i + 1 - getCHAIN_CODE_NUM_DIRECTIONS() / 2);
				if (shiU >= theta[i] && theta[i] > shiL) {
					double delta = Math.abs(shiU - theta[i]);
					if (delta < phi / 2) {
						code[j] = i;
					}
					else {
						code[j] = i + 1;
					}
					System.out.println(code[j]);
					// go for next angle
					break;// code found break inner loop
				}
			}
		}

		return code;
	}

	private void composeFeatureVector() {
		// location, distance,angleCG,angleSucc,Velocity.....
		System.out.println("Composing...");
		extractedFeature = new GestureFeature[framedSamples - 1];
		for (int i = 0; i < extractedFeature.length; i++) {
			extractedFeature[i] = new GestureFeature();
			extractedFeature[i].setAngleWithCG(angleWithCG[i]);
			extractedFeature[i].setAngleWithInitialPt(angleWithInitialPt[i]);
			// extractedFeature[i].setAngleWithSuccessivePts(angleWithSuccessivePts[i]);
			// extractedFeature[i].setDistanceBetweenSuccessivePts(distanceBetweenSuccessivePts[i]);
			extractedFeature[i].setLocationRelativeToCG(locationRelativeToCG[i]);
			extractedFeature[i].setVelocity(velocities[i]);
			extractedFeature[i].setAngleWithEndPt(angleWithEndPt[i]);
			extractedFeature[i].setxMaxyMaxAngle(xMaxyMaxAngle[i]);
			extractedFeature[i].setxMaxyMinAngle(xMaxyMinAngle[i]);
			extractedFeature[i].setxMinyMaxAngle(xMinyMaxAngle[i]);
			extractedFeature[i].setxMinyMinAngle(xMinyMinAngle[i]);
		}
	}

	// **MATH UTILS**//
	private double findMax(double[] arr) {
		double max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		return max;
	}

	private double findMin(double[] arr) {
		double max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < max) {
				max = arr[i];
			}
		}
		return max;
	}

	// ** GETTER AND SETTER **//

	public void setInputRawFeature(RawFeature inputRawFeature) {
		this.inputRawFeature = inputRawFeature;
	}

	public GestureFeature[] getExtractedFeature() {
		return extractedFeature;
	}

	public int getSAMPLE_PER_FRAME() {
		return SAMPLE_PER_FRAME;
	}

	public void setSAMPLE_PER_FRAME(int sAMPLE_PER_FRAME) {
		SAMPLE_PER_FRAME = sAMPLE_PER_FRAME;
	}

	public int getCHAIN_CODE_NUM_DIRECTIONS() {
		return CHAIN_CODE_NUM_DIRECTIONS;
	}

	public void setCHAIN_CODE_NUM_DIRECTIONS(int cHAIN_CODE_NUM_DIRECTIONS) {
		CHAIN_CODE_NUM_DIRECTIONS = cHAIN_CODE_NUM_DIRECTIONS;
	}

}
