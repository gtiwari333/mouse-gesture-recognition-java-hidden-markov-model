/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.gt.gesture.features.RawFeature;
import com.gt.hmm.HMMModel;
import com.gt.hmm.classify.vq.CodeBookDictionary;

/**
 * File operation as ObjectIO
 * 
 * @author Ganesh Tiwari
 * 
 *         MAKE SURE THAT Files are/will be in this folder structure the folder
 *         structure for training : <br>
 *         \train\\gestureA\\gestureA01.codebook<br>
 *         \train\\gestureA\\gestureA02.codebook<br>
 *         \test\\gestureA\\gestureA.codebook<br>
 * 
 *         \models\\HMM\\apple.hmm <br>
 *         \models\\HMM\\cat.hmm <br>
 *         \models\\codebook\\codebook.cbk<br>
 * 
 */
public class ObjectIOFileDataBase implements Database {

	/**
	 * type of current model,,gmm,hmm,cbk, which is extension ofsaved file
	 */

	DBMode mode;
	/**
     *
     */
	String[] modelFiles;
	/**
     *
     */
	String[] userNames;
	String CURRENTFOLDER;
	/**
	 * the file name to same codebook, adds .cbk extension automatically
	 */
	String CODEBOOKFILENAME = "codebook";
	String currentModelType;

	public ObjectIOFileDataBase() {
	}

	/**
     *
     */
	@Override
	public String[] getRegisteredModelNames() {
		modelFiles = readRegisteredWithExtension();
		if (modelFiles != null) {
			if (mode == DBMode.HMM_MODEL) { return removeExtension(modelFiles); }
			return modelFiles;
		}
		else {
			String[] tmp = new String[1];
			tmp[0] = "";
			return tmp;
		}

	}

	/**
     *
     */
	@Override
	public void saveModel(Model model, String name) {
		switch (mode) {
		case HMM_MODEL:
			ObjectIO<HMMModel> oio = new ObjectIO<HMMModel>();
			oio.setModel((HMMModel) model);
			oio.saveModel(CURRENTFOLDER + "\\" + name + "." + mode);
			break;
		case CODEBOOK_MODEL:
			ObjectIO<CodeBookDictionary> oio2 = new ObjectIO<CodeBookDictionary>();
			oio2.setModel((CodeBookDictionary) model);
			oio2.saveModel(CURRENTFOLDER + "\\" + CODEBOOKFILENAME + "." + mode);
			break;
		case TRAINDATA:
		case TESTDATA:
			ObjectIO<RawFeature> oio3 = new ObjectIO<RawFeature>();
			oio3.setModel((RawFeature) model);
			// trainORtestdata\\gestureA\\A1.raw
			// trainORtestdata\\gestureA\\A2.raw
			// TODO: check for existence, and generate rnd
			oio3.saveModel(CURRENTFOLDER + "\\" + name + "\\" + name + System.currentTimeMillis() + "." + mode);
			break;
		default:
		}
	}

	private String[] readRegisteredWithExtension() {
		File modelPath = new File(CURRENTFOLDER);
		// modelFiles = new String[modelPath.list().length];
		modelFiles = modelPath.list();// must return only folders
		return modelFiles;
	}

	private String[] removeExtension(String[] modelFiles) {
		// remove the ext i.e., type
		String[] noExtension = new String[modelFiles.length];
		for (int i = 0; i < modelFiles.length; i++) {
			int indexOfDot = modelFiles[i].indexOf(".");
			noExtension[i] = modelFiles[i].substring(0, indexOfDot);
			// TODO:check
			// the
			// lengths
		}
		return noExtension;
	}

	/**
	 * {@inheritDoc} <br>
	 * And set folder according to this mode
	 */
	@Override
	public void setMode(DBMode mode) {
		this.mode = mode;

		switch (mode) {
		case HMM_MODEL:
			CURRENTFOLDER = "models\\HMM";
			break;
		case CODEBOOK_MODEL:
			CURRENTFOLDER = "models\\codeBook";
			break;
		case TRAINDATA:
			CURRENTFOLDER = "trainData";
			break;
		case TESTDATA:
			CURRENTFOLDER = "testData";
			break;
		default:
		}
	}

	private int getFileCount(File filePath) {
		return filePath.list().length;
	}

	@Override
	public Model[][] readAllDataofCurrentMode() {
		Model[][] readModel = null;
		switch (mode) {
		case HMM_MODEL:
			File hmmPath = new File(CURRENTFOLDER + "\\");
			int count = getFileCount(hmmPath);
			readModel = new Model[0][count];
			String[] modelFiles = hmmPath.list();
			for (int i = 0; i < count; i++) {
				readModel[0][i] = (HMMModel) readModel(modelFiles[i]);
			}
			break;
		case CODEBOOK_MODEL:
			readModel = new Model[0][0];
			readModel[0][0] = (CodeBookDictionary) readModel("");
			break;
		case TRAINDATA:
		case TESTDATA:
			File folderPath = new File(CURRENTFOLDER + "\\");
			int dataCount = getFileCount(folderPath);
			readModel = new Model[dataCount][50];
			File[] dataTypes = folderPath.listFiles();
			for (int i = 0; i < dataTypes.length; i++) {
				File[] cur = dataTypes[i].listFiles();
				// FIXME: prob here , abs rel path.. extension 3 chars
				List<Model> lmodel = new ArrayList<Model>();
				for (int j = 0; j < cur.length; j++) {
					// TODO: check value of cur, must be valid
					lmodel.add((Model) readModel(cur[j].toString()));
				}
				readModel[i] = lmodel.toArray(new Model[0]);
			}
			break;
		default:
		}
		return readModel;
	}

	/**
    *
    */
	@Override
	public Model readModel(String name) {
		Model model = null;
		switch (mode) {
		case HMM_MODEL:
			ObjectIO<HMMModel> oio = new ObjectIO<HMMModel>();
			model = new HMMModel();
			model = oio.readModel(CURRENTFOLDER + "\\" + name + "." + mode);
			// System.out.println("Type " + type);
			// System.out.println("Read ::::: " + DBROOTFOLDER + "\\" +
			// CURRENTFOLDER + "\\" + name + "." + type);
			// System.out.println(model);
			break;
		case CODEBOOK_MODEL:
			ObjectIO<CodeBookDictionary> oio2 = new ObjectIO<CodeBookDictionary>();
			model = new CodeBookDictionary();
			model = oio2.readModel(CURRENTFOLDER + "\\" + CODEBOOKFILENAME + "." + mode);
			// System.out.println("Read ::::: " + DBROOTFOLDER + "\\" +
			// CURRENTFOLDER + "\\" + CODEBOOKFILENAME + "." + type);
			break;
		case TRAINDATA:
		case TESTDATA:
			ObjectIO<RawFeature> oio3 = new ObjectIO<RawFeature>();
			// name should be name of file with extension
			model = oio3.readModel(name);
		default:
		}
		return model;
	}
}
