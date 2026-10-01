/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

import com.gt.gesture.features.RawFeature;
import com.gt.hmm.HMMModel;
import com.gt.hmm.classify.vq.CodeBookDictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File operation as ObjectIO
 *
 * @author Ganesh Tiwari MAKE SURE THAT Files are/will be in this folder structure the folder structure for training : <br>
 * \train\\gestureA\\gestureA01.codebook<br>
 * \train\\gestureA\\gestureA02.codebook<br>
 * \test\\gestureA\\gestureA.codebook<br>
 * \models\\HMM\\apple.hmm <br>
 * \models\\HMM\\cat.hmm <br>
 * \models\\codebook\\codebook.cbk<br>
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
        File[] entries = listEntries();
        String[] names = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            names[i] = isFolderPerName() ? entries[i].getName() : removeExtension(entries[i].getName());
        }
        return names;
    }

    /**
     *
     */
    @Override
    public void saveModel(Model model, String name) {
        switch (mode) {
            case HMM_MODEL:
                ObjectIO<HMMModel> oio = new ObjectIO<>();
                oio.setModel((HMMModel) model);
                oio.saveModel(CURRENTFOLDER + File.separator + name + "." + mode);
                break;
            case CODEBOOK_MODEL:
                ObjectIO<CodeBookDictionary> oio2 = new ObjectIO<>();
                oio2.setModel((CodeBookDictionary) model);
                oio2.saveModel(CURRENTFOLDER + File.separator + CODEBOOKFILENAME + "." + mode);
                break;
            case TRAINDATA:
            case TESTDATA:
                ObjectIO<RawFeature> oio3 = new ObjectIO<>();
                oio3.setModel((RawFeature) model);
                // trainORtestdata"+ File.separator +"gestureA"+ File.separator +"A1.raw
                // trainORtestdata"+ File.separator +"gestureA"+ File.separator +"A2.raw
                // TODO: check for existence, and generate rnd
                oio3.saveModel(CURRENTFOLDER + File.separator + name + File.separator + name + System.currentTimeMillis() + "." + mode);
                break;
            default:
        }
    }

    /**
     * train/test data has one sub folder per gesture; models are one file per gesture
     */
    private boolean isFolderPerName() {
        return mode == DBMode.TRAINDATA || mode == DBMode.TESTDATA;
    }

    /**
     * sorted gesture folders (train/test data) or model files (HMM/codebook) of the current mode,
     * skipping anything else, e.g. .DS_Store
     */
    private File[] listEntries() {
        File[] entries = new File(CURRENTFOLDER).listFiles(f -> !f.getName().startsWith(".")
                && (isFolderPerName() ? f.isDirectory() : f.isFile() && f.getName().endsWith("." + mode)));
        if (entries == null) {
            return new File[0];
        }
        Arrays.sort(entries);
        return entries;
    }

    private String removeExtension(String fileName) {
        return fileName.substring(0, fileName.length() - ("." + mode).length());
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
                CURRENTFOLDER = "models" + File.separator + "HMM";
                break;
            case CODEBOOK_MODEL:
                CURRENTFOLDER = "models" + File.separator + "codeBook";
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

    @Override
    public Model[][] readAllDataofCurrentMode() {
        Model[][] readModel = null;
        switch (mode) {
            case HMM_MODEL:
                String[] names = getRegisteredModelNames();
                readModel = new Model[1][names.length];
                for (int i = 0; i < names.length; i++) {
                    readModel[0][i] = readModel(names[i]);
                }
                break;
            case CODEBOOK_MODEL:
                readModel = new Model[][]{{readModel("")}};
                break;
            case TRAINDATA:
            case TESTDATA:
                // same order as getRegisteredModelNames()
                File[] dataTypes = listEntries();
                readModel = new Model[dataTypes.length][];
                for (int i = 0; i < dataTypes.length; i++) {
                    File[] cur = dataTypes[i].listFiles(f -> f.isFile() && f.getName().endsWith("." + mode));
                    Arrays.sort(cur);
                    List<Model> lmodel = new ArrayList<>();
                    for (File aCur : cur) {
                        lmodel.add(readModel(aCur.toString()));
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
                ObjectIO<HMMModel> oio = new ObjectIO<>();
                model = new HMMModel();
                model = oio.readModel(CURRENTFOLDER + File.separator + name + "." + mode);
                // System.out.println("Type " + type);
                // System.out.println("Read ::::: " + DBROOTFOLDER + File.separator +
                // CURRENTFOLDER + File.separator + name + "." + type);
                // System.out.println(model);
                break;
            case CODEBOOK_MODEL:
                ObjectIO<CodeBookDictionary> oio2 = new ObjectIO<>();
                model = new CodeBookDictionary();
                model = oio2.readModel(CURRENTFOLDER + File.separator + CODEBOOKFILENAME + "." + mode);
                // System.out.println("Read ::::: " + DBROOTFOLDER + File.separator +
                // CURRENTFOLDER + File.separator + CODEBOOKFILENAME + "." + type);
                break;
            case TRAINDATA:
            case TESTDATA:
                ObjectIO<RawFeature> oio3 = new ObjectIO<>();
                // name should be name of file with extension
                model = oio3.readModel(name);
                break;
            default:
        }
        return model;
    }
}
