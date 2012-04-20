/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

import java.io.File;

/**
 * various operations relating to reading train/testing data folders<br>
 * works according to the filePath supplied in constructor arguement
 * 
 * @author Ganesh Tiwari
 * 
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
			setDataPath(new File("TestData"));
		}
		else if (testOrTrain.equalsIgnoreCase("train")) {
			setDataPath(new File("TrainData"));
		}

	}

	private void readFolder() {
		folderNames = new String[getDataPath().list().length];
		folderNames = getDataPath().list();// must return only folders
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
			File dataFolder = new File(getDataPath() + "\\" + folderNames[i] + "\\");
			dataFiles[i] = dataFolder.listFiles();
		}
		System.out.println("++++++Folder's Content+++++");
		for (int i = 0; i < dataFiles.length; i++) {
			for (int j = 0; j < dataFiles[i].length; j++) {
				System.out.print(dataFiles[i][j].getName() + "\t\t");
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
