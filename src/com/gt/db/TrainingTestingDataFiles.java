/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

import java.io.File;
import java.util.Arrays;

/**
 * various operations relating to reading train/testing data folders<br>
 * works according to the filePath supplied in constructor arguement
 *
 * @author Ganesh Tiwari
 */
public class TrainingTestingDataFiles {

    protected String[] folderNames;
    protected File[][] dataFiles;
    protected File dataFilesPath;

    /**
     * constructor, sets the dataFile path according to the args supplied
     *
     * @param hmmOrGmm
     * @param testOrTrain
     */
    public TrainingTestingDataFiles(String testOrTrain) {
        if (testOrTrain.equalsIgnoreCase("test")) {
            setDataPath(new File("testData"));
        } else if (testOrTrain.equalsIgnoreCase("train")) {
            setDataPath(new File("trainData"));
        }

    }

    private void readFolder() {
        // only gesture folders, sorted
        File[] dirs = getDataPath().listFiles(f -> f.isDirectory() && !f.getName().startsWith("."));
        if (dirs == null) {
            dirs = new File[0];
        }
        Arrays.sort(dirs);
        folderNames = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            folderNames[i] = dirs[i].getName();
        }
    }

    public String[] readDataFolder() {
        readFolder();
        return folderNames;
    }

    public File[][] readDataFilesList() {
        readFolder();
        dataFiles = new File[folderNames.length][];
        for (int i = 0; i < folderNames.length; i++) {
            System.out.println(folderNames[i]);
            File dataFolder = new File(getDataPath(), folderNames[i]);
            dataFiles[i] = dataFolder.listFiles(File::isFile);
            Arrays.sort(dataFiles[i]);
        }
        System.out.println("++++++Folder's Content+++++");
        for (File[] dataFile : dataFiles) {
            for (int j = 0; j < dataFile.length; j++) {
                System.out.print(dataFile[j].getName() + "\t\t");
            }
            System.out.println();
        }
        return dataFiles;

    }

    public File getDataPath() {
        return dataFilesPath;
    }

    public void setDataPath(File dataFilesPath) {
        this.dataFilesPath = dataFilesPath;
        System.out.println("Current data file Path   :" + this.dataFilesPath.getName());
    }
}
