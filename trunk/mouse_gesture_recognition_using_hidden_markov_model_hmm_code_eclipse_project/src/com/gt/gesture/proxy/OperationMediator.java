/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.gesture.proxy;

import java.util.ArrayList;
import java.util.List;

import com.gt.db.DBMode;
import com.gt.db.Database;
import com.gt.db.Model;
import com.gt.db.ObjectIOFileDataBase;
import com.gt.gesture.features.GestureFeature;
import com.gt.gesture.features.GestureFeatureExtractor;
import com.gt.gesture.features.RawFeature;
import com.gt.hmm.HiddenMarkov;
import com.gt.hmm.classify.vq.Codebook;
import com.gt.hmm.classify.vq.Points;

/**
 * A mediator class to access all HMM/VQ algorithms<br>
 * 
 * @author Ganesh
 * 
 */
public class OperationMediator {
	private GestureFeatureExtractor featureExtractor;
	private Database database;
	private List<GestureFeature> allFeaturesList;
	private Codebook codebook;
	// FIXME: what is appropriate value for these
	private final int NUM_STATES = 4;
	// FIXME: same in Codebook.java class
	private final int NUM_SYMBOLS = 64;

	public OperationMediator() {
		database = new ObjectIOFileDataBase();
		allFeaturesList = new ArrayList<GestureFeature>();

	}

	public boolean saveCaptured(RawFeature rf, String gestName) {
		boolean operationSuccess = false;
		database.setMode(DBMode.TRAINDATA);
		database.saveModel(rf, gestName);
		operationSuccess = true;
		return operationSuccess;
	}

	public boolean verify(String gesture, RawFeature rf) {
		String bestMatched = recognizeGesture(rf);
		return (gesture.equalsIgnoreCase(bestMatched));
	}

	public String[] readRegGestureModels() {
		database.setMode(DBMode.HMM_MODEL);
		return (database.getRegisteredModelNames());
	}

	public String[] readTrainData1D() {
		database.setMode(DBMode.TRAINDATA);
		return (database.getRegisteredModelNames());

	}

	/**
	 * First quantizes the feature vector against codebook, and then runs
	 * viterbi decoding
	 * 
	 * @param rf
	 * @return
	 */
	public String recognizeGesture(RawFeature rf) {
		codebook = new Codebook();
		GestureFeature[] gestureFeatures = getFeature(rf);
		Points[] pts = getPointsFromFeatureVector(gestureFeatures);
		int[] quantized = codebook.quantize(pts);
		String[] regGestures = readRegGestureModels();
		HiddenMarkov[] hmms = new HiddenMarkov[regGestures.length];
		// read hmms
		for (int i = 0; i < hmms.length; i++) {
			hmms[i] = new HiddenMarkov(regGestures[i]);
		}
		// find likelihood by viterbi decoding of quantized seq
		double[] likelihoods = new double[regGestures.length];
		for (int j = 0; j < likelihoods.length; j++) {
			likelihoods[j] = hmms[j].viterbi(quantized);
		}
		// find the largest likelihood
		double highest = Double.NEGATIVE_INFINITY;
		int wordIndex = -1;
		for (int j = 0; j < regGestures.length; j++) {
			if (likelihoods[j] > highest) {
				highest = likelihoods[j];
				wordIndex = j;
			}
		}
		// best matched
		return regGestures[wordIndex];
	}

	private GestureFeature[] getFeature(RawFeature rf) {
		featureExtractor = new GestureFeatureExtractor(rf);
		return featureExtractor.getExtractedFeature();
	}

	private Points[] getPointsFromFeatureVector(GestureFeature[] gestureFeatures) {
		// get Points object from all feature vector
		Points pts[] = new Points[gestureFeatures.length];
		for (int j = 0; j < gestureFeatures.length; j++) {
			pts[j] = new Points(gestureFeatures[j].getFeatureVector());
		}
		return pts;
	}

	/**
	 * Generates codebook by clustering all features in training set raw data
	 * 
	 * @return
	 */
	public boolean generateCodeBook() {
		boolean operationSuccess = false;
		database.setMode(DBMode.TRAINDATA);
		Model[][] regModels = database.readAllDataofCurrentMode();
		int totalFrames = 0;
		allFeaturesList.clear();
		// extract single list of all features
		for (int i = 0; i < regModels.length; i++) {
			for (int j = 0; j < regModels[i].length; j++) {
				GestureFeature[] gf = getFeature((RawFeature) regModels[i][j]);
				for (int k = 0; k < gf.length; k++) {
					allFeaturesList.add(gf[k]);
					totalFrames++;
				}
			}

		}//
			// single array from list
		GestureFeature[] allFeaturesArr = new GestureFeature[totalFrames];
		allFeaturesArr = allFeaturesList.toArray(new GestureFeature[0]);
		// clustering is done automatically after callng constructor
		Codebook cbk = new Codebook(getPointsFromFeatureVector(allFeaturesArr));
		cbk.saveToFile();
		operationSuccess = true;
		return operationSuccess;

	}

	/**
	 * Performs hmm train for all training data set.<br>
	 * First quantizes the feature vector against codebook, and then train
	 * baum-welch
	 * 
	 * @return
	 */
	public boolean trainHMM() {
		boolean operationSuccess = false;
		codebook = new Codebook();
		database.setMode(DBMode.TRAINDATA);
		Model[][] regModels = database.readAllDataofCurrentMode();
		String[] gestName = database.getRegisteredModelNames();
		int quantizedSeq[][];
		HiddenMarkov mkv = new HiddenMarkov(NUM_STATES, NUM_SYMBOLS);
		// for each gesture
		for (int i = 0; i < regModels.length; i++) {
			operationSuccess = false;
			// for each train sample of current gesture
			quantizedSeq = new int[regModels[i].length][];
			for (int j = 0; j < regModels[i].length; j++) {
				GestureFeature[] gf = getFeature((RawFeature) regModels[i][j]);
				Points[] pts = getPointsFromFeatureVector(gf);
				quantizedSeq[j] = codebook.quantize(pts);
			}
			mkv.setTrainSeq(quantizedSeq);
			mkv.train();
			mkv.save(gestName[i]);
			operationSuccess = true;
		}
		return operationSuccess;
	}
}
