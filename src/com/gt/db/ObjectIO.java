/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.db;

import java.io.*;

/**
 * This Class works for both any object <code><T></code>, which implements the Model interface
 *
 * @param <T>
 * @author Ganesh Tiwari
 */
public class ObjectIO<T> {

    T model;

    /**
     * default constructor of modelDB
     */
    public ObjectIO() {
    }

    /**
     * sets the model to save to db
     *
     * @param model model of current type to save into db
     */
    public void setModel(T model) {
        this.model = model;
    }

    /**
     * saves the model to {@code filePath} of type T
     *
     * @param filePath
     */
    public void saveModel(String filePath) {
        // if parent folder doesnot exists, create one
        File f = new File(filePath).getAbsoluteFile().getParentFile();
        if (!f.exists()) {
            f.mkdirs();
        }
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath))) {
            output.writeObject(model);
        } catch (IOException e) {
            throw new UncheckedIOException("could not save model to " + filePath, e);
        }
    }

    /**
     * read the model from {@code filePath} of type T
     *
     * @param filePath
     * @return the model of type T, or null when the file does not exist
     */
    @SuppressWarnings("unchecked")
    public T readModel(String filePath) {
        if (!new File(filePath).isFile()) {
            System.out.println("File Not Found, while reading model " + filePath);
            return null;
        }
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath))) {
            model = (T) input.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException("could not read model from " + filePath, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("unexpected class in " + filePath, e);
        }
        return model;
    }
}
