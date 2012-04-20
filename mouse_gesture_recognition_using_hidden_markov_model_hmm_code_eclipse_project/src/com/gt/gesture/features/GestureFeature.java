/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.gesture.features;

import java.io.Serializable;

/**
 * a single feature vector is a point in K-dim space, here K=6 later it should
 * be conveted to Point
 * 
 * @author Ganesh
 * 
 */
public class GestureFeature implements Serializable {

	private static final long serialVersionUID = 8399123588511856655L;
	double locationRelativeToCG;
	// double distanceBetweenSuccessivePts;
	double angleWithCG;
	// double angleWithSuccessivePts;
	double angleWithInitialPt;
	double angleWithEndPt;
	double velocity;
	double xMinyMinAngle;
	double xMinyMaxAngle;
	double xMaxyMinAngle;
	double xMaxyMaxAngle;

	public GestureFeature() {
		super();
	}

	public double getxMinyMinAngle() {
		return xMinyMinAngle;
	}

	public void setxMinyMinAngle(double xMinyMinAngle) {
		this.xMinyMinAngle = xMinyMinAngle;
	}

	public double getxMinyMaxAngle() {
		return xMinyMaxAngle;
	}

	public void setxMinyMaxAngle(double xMinyMaxAngle) {
		this.xMinyMaxAngle = xMinyMaxAngle;
	}

	public double getxMaxyMinAngle() {
		return xMaxyMinAngle;
	}

	public void setxMaxyMinAngle(double xMaxyMinAngle) {
		this.xMaxyMinAngle = xMaxyMinAngle;
	}

	public double getxMaxyMaxAngle() {
		return xMaxyMaxAngle;
	}

	public void setxMaxyMaxAngle(double xMaxyMaxAngle) {
		this.xMaxyMaxAngle = xMaxyMaxAngle;
	}

	/**
	 * 
	 * @return sequence of feature coefficients of current vector
	 */
	public double[] getFeatureVector() {
		return new double[] { getLocationRelativeToCG(),
				// getDistanceBetweenSuccessivePts(),
				getAngleWithCG(),
				// getAngleWithSuccessivePts(),
				getAngleWithInitialPt(), getAngleWithEndPt(), getVelocity(), getxMaxyMaxAngle(), getxMaxyMinAngle(), getxMinyMaxAngle(),
				getxMinyMinAngle() };
	}

	public double getLocationRelativeToCG() {
		return locationRelativeToCG;
	}

	public void setLocationRelativeToCG(double locationRelativeToCG) {
		this.locationRelativeToCG = locationRelativeToCG;
	}

	// public double getDistanceBetweenSuccessivePts() {
	// return distanceBetweenSuccessivePts;
	// }
	//
	// public void setDistanceBetweenSuccessivePts(double
	// distanceBetweenSuccessivePts) {
	// this.distanceBetweenSuccessivePts = distanceBetweenSuccessivePts;
	// }

	public double getAngleWithCG() {
		return angleWithCG;
	}

	public void setAngleWithCG(double angleWithCG) {
		this.angleWithCG = angleWithCG;
	}

	// public double getAngleWithSuccessivePts() {
	// return angleWithSuccessivePts;
	// }
	//
	// public void setAngleWithSuccessivePts(double angleWithSuccessivePts) {
	// this.angleWithSuccessivePts = angleWithSuccessivePts;
	// }

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getAngleWithInitialPt() {
		return angleWithInitialPt;
	}

	public void setAngleWithInitialPt(double angleWithInitialPt) {
		this.angleWithInitialPt = angleWithInitialPt;
	}

	public double getAngleWithEndPt() {
		return angleWithEndPt;
	}

	public void setAngleWithEndPt(double angleWithEndPt) {
		this.angleWithEndPt = angleWithEndPt;
	}

	@Override
	public String toString() {
		return "GestureFeature [Loc_rel2CG=" + locationRelativeToCG + ", Dist_betnSuccPts="
		// + distanceBetweenSuccessivePts
				+ ", Angle_withCG=" + angleWithCG + ", AngleWithSuccPts="
				// + angleWithSuccessivePts
				+ ", AngleWithInitPt=" + angleWithInitialPt + ", AngleWithEndPt=" + angleWithEndPt + ", Velocity=" + velocity + "]";
	}
}
