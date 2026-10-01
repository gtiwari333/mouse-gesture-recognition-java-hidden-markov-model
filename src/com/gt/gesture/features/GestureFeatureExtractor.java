/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.gesture.features;

import java.awt.*;

/**
 * Reference: http://st04110083.etu.edu.tr/makale/Hand%20gesture%20recognition%20 using%20combined%20features%20of%20location,%20angle%20and%20velocity.pdf
 */
public class GestureFeatureExtractor {

    /**
     * angles are quantized to steps of this many degrees
     */
    static final int QUANTIZE_ANGLE = 10;
    /**
     * every SAMPLE_PER_FRAME-th captured point is used
     */
    static final int SAMPLE_PER_FRAME = 3;

    private final RawFeature inputRawFeature;
    private GestureFeature[] extractedFeature;
    private double[] locationRelativeToCG;
    private double[] distanceBetweenSuccessivePts;
    private double[] timeDiffs;
    private double[] angleWithCG;
    private double[] angleWithInitialPt;
    private double[] angleWithEndPt;
    private double[] velocities;
    private double[] motionDirection;
    private double[] xMinyMinAngle;
    private double[] xMinyMaxAngle;
    private double[] xMaxyMinAngle;
    private double[] xMaxyMaxAngle;
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private double centerX;
    private double centerY;
    private double[] framed_curTime;
    private Point[] framed_drawPoint;
    /**
     * number of feature vectors: one per pair of successive framed points
     */
    private int featureCount;

    public GestureFeatureExtractor(RawFeature inputRawFeature) {
        this.inputRawFeature = inputRawFeature;
        calculateFeature();
    }

    private void calculateFeature() {
        // preprocess-- framing
        framing();
        calculateCenterAndBounds();
        calculatePositionDistanceAngle();
        normalizeFeatures();
        composeFeatureVector();
    }

    private void framing() {
        int totalSample = inputRawFeature.getCurTime().length;
        if (totalSample < 2) {
            throw new IllegalArgumentException("a gesture needs at least 2 points, got " + totalSample);
        }
        // short gestures use every point instead of every SAMPLE_PER_FRAME-th one
        int step = totalSample / SAMPLE_PER_FRAME >= 2 ? SAMPLE_PER_FRAME : 1;
        int framedSample = totalSample / step;
        framed_curTime = new double[framedSample];
        framed_drawPoint = new Point[framedSample];
        for (int i = 0; i < framedSample; i++) {
            framed_curTime[i] = inputRawFeature.getCurTime()[i * step];
            framed_drawPoint[i] = inputRawFeature.getDrawPoint()[i * step];
        }

        featureCount = framedSample - 1;
        locationRelativeToCG = new double[featureCount];
        distanceBetweenSuccessivePts = new double[featureCount];
        angleWithCG = new double[featureCount];
        angleWithInitialPt = new double[featureCount];
        angleWithEndPt = new double[featureCount];
        timeDiffs = new double[featureCount];
        velocities = new double[featureCount];
        motionDirection = new double[featureCount];
        xMinyMinAngle = new double[featureCount];
        xMinyMaxAngle = new double[featureCount];
        xMaxyMaxAngle = new double[featureCount];
        xMaxyMinAngle = new double[featureCount];
    }

    /**
     * bounding box and centre of gravity of all captured points
     */
    private void calculateCenterAndBounds() {
        Point[] points = inputRawFeature.getDrawPoint();
        xMin = xMax = points[0].getX();
        yMin = yMax = points[0].getY();
        double sX = 0, sY = 0;
        for (Point p : points) {
            xMin = Math.min(xMin, p.getX());
            xMax = Math.max(xMax, p.getX());
            yMin = Math.min(yMin, p.getY());
            yMax = Math.max(yMax, p.getY());
            sX += p.getX();
            sY += p.getY();
        }
        centerX = sX / points.length;
        centerY = sY / points.length;
    }

    private void calculatePositionDistanceAngle() {
        Point initPt = framed_drawPoint[0];
        Point endPt = framed_drawPoint[framed_drawPoint.length - 1];
        for (int i = 0; i < featureCount; i++) {
            /** Geometry **/
            Point curPt = framed_drawPoint[i];
            // location relative to CG
            double dxC = curPt.getX() - centerX;
            double dyC = curPt.getY() - centerY;
            locationRelativeToCG[i] = Math.sqrt(dxC * dxC + dyC * dyC);
            angleWithCG[i] = getAngleYbyX(dyC, dxC);

            // distance between successive points
            Point p2 = framed_drawPoint[i + 1];
            distanceBetweenSuccessivePts[i] = curPt.distance(p2);
            // which way the stroke moves, e.g. tells a left stroke from a right one
            motionDirection[i] = getDirection(p2.getY() - curPt.getY(), p2.getX() - curPt.getX());

            angleWithInitialPt[i] = getAngleYbyX(curPt.getY() - initPt.getY(), curPt.getX() - initPt.getX());
            angleWithEndPt[i] = getAngleYbyX(curPt.getY() - endPt.getY(), curPt.getX() - endPt.getX());

            /** Kinematics **/
            timeDiffs[i] = framed_curTime[i + 1] - framed_curTime[i];
            velocities[i] = divide(distanceBetweenSuccessivePts[i], timeDiffs[i]);

            // angles with the corners of the bounding box
            xMinyMinAngle[i] = getAngleYbyX(curPt.getY() - yMin, curPt.getX() - xMin);
            xMinyMaxAngle[i] = getAngleYbyX(curPt.getY() - yMax, curPt.getX() - xMin);
            xMaxyMinAngle[i] = getAngleYbyX(curPt.getY() - yMin, curPt.getX() - xMax);
            xMaxyMaxAngle[i] = getAngleYbyX(curPt.getY() - yMax, curPt.getX() - xMax);
        }
    }

    private static double divide(double num, double denom) {
        if (denom == 0) {
            return 0.0;
        } else {
            return num / denom;
        }
    }

    /**
     * orientation of the vector (dx, dy), i.e. atan(dy/dx), quantized to QUANTIZE_ANGLE degree steps.<br>
     * Opposite directions share an orientation on purpose; the direction of motion is a separate feature
     * ({@link #getDirection}). Using full-circle atan2 angles for every feature instead lowered the 3-fold cross
     * validation accuracy (~91.8% vs ~96.2%).
     *
     * @return quantized angle in [-90, 90] / QUANTIZE_ANGLE
     */
    static double getAngleYbyX(double dy, double dx) {
        // a vertical vector is +-90 degrees, not 0 (dy / dx would divide by zero)
        double angle = dx == 0 ? Math.signum(dy) * Math.PI / 2 : Math.atan(dy / dx);
        // + 0.0 turns -0.0 into 0.0
        return Math.ceil(Math.toDegrees(angle) / QUANTIZE_ANGLE) + 0.0;
    }

    /**
     * direction of the vector (dx, dy) over the full circle, quantized to QUANTIZE_ANGLE degree steps
     *
     * @return quantized angle in (-180, 180] / QUANTIZE_ANGLE
     */
    static double getDirection(double dy, double dx) {
        return Math.ceil(Math.toDegrees(Math.atan2(dy, dx)) / QUANTIZE_ANGLE) + 0.0;
    }

    /**
     * post process
     **/
    private void normalizeFeatures() {
        double maxLoc = findMax(locationRelativeToCG);
        double maxDist = findMax(distanceBetweenSuccessivePts);
        double minLoc = findMin(locationRelativeToCG);
        double minDist = findMin(distanceBetweenSuccessivePts);
        double maxTimDiff = findMax(timeDiffs);
        double maxVelocity = findMax(velocities);

        for (int i = 0; i < featureCount; i++) {
            locationRelativeToCG[i] = divide(locationRelativeToCG[i] - minLoc, maxLoc - minLoc);
            distanceBetweenSuccessivePts[i] = divide(distanceBetweenSuccessivePts[i] - minDist, maxDist - minDist);
            timeDiffs[i] = divide(timeDiffs[i], maxTimDiff);
            velocities[i] = divide(velocities[i], maxVelocity);
        }
    }

    private void composeFeatureVector() {
        extractedFeature = new GestureFeature[featureCount];
        for (int i = 0; i < featureCount; i++) {
            extractedFeature[i] = new GestureFeature();
            extractedFeature[i].setAngleWithCG(angleWithCG[i]);
            extractedFeature[i].setAngleWithInitialPt(angleWithInitialPt[i]);
            extractedFeature[i].setLocationRelativeToCG(locationRelativeToCG[i]);
            extractedFeature[i].setVelocity(velocities[i]);
            extractedFeature[i].setMotionDirection(motionDirection[i]);
            extractedFeature[i].setAngleWithEndPt(angleWithEndPt[i]);
            extractedFeature[i].setxMaxyMaxAngle(xMaxyMaxAngle[i]);
            extractedFeature[i].setxMaxyMinAngle(xMaxyMinAngle[i]);
            extractedFeature[i].setxMinyMaxAngle(xMinyMaxAngle[i]);
            extractedFeature[i].setxMinyMinAngle(xMinyMinAngle[i]);
        }
    }

    private static double findMax(double[] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    private static double findMin(double[] arr) {
        double min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }

    public GestureFeature[] getExtractedFeature() {
        return extractedFeature;
    }
}
