/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

/**
 * 
 * @author Ganesh Tiwari
 * 
 */
public interface Database {
	/**
	 * sets the current DB operation mode
	 */
	public void setMode(DBMode mode);

	/**
	 * read all registered models in current folder
	 * 
	 * @return array of registered models
	 */
	public String[] getRegisteredModelNames();

	/**
	 * read model file with given name
	 * 
	 * @param name
	 * @return
	 */
	public Model readModel(String name);

	/**
	 * used for training/testing data set<br>
	 * reads all models of current type<br>
	 * [trainingData]x[numOfCurrentTrainingData]<br>
	 * for trained model, it will be of 1xN dimension<br>
	 * 
	 * @return 2D model array of current model type
	 */
	public Model[][] readAllDataofCurrentMode();

	/**
	 * save given model of current type into respective location using given
	 * name
	 * 
	 * @param m
	 *            model to save
	 * @param name
	 *            name of model
	 */
	public void saveModel(Model m, String name);
}
