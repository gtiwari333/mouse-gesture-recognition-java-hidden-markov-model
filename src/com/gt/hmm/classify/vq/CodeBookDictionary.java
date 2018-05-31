/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package com.gt.hmm.classify.vq;

import com.gt.db.Model;

import java.io.Serializable;

/**
 * @author Ganesh Tiwari
 */
public class CodeBookDictionary implements Serializable, Model {

    private static final long serialVersionUID = 2094442679375932181L;
    protected int dimension;
    protected Centroid[] cent;

    public CodeBookDictionary() {
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public Centroid[] getCent() {
        return cent;
    }

    public void setCent(Centroid[] cent) {
        this.cent = cent;
    }

    public String getModelType() {
        return "CodeBook";
    }

    @Override
    public void showParameters() {
        for (Centroid aCent : cent) {
            // bw.write("c" + c + ": (");
            for (int k = 0; k < dimension; k++) {
                System.out.print(aCent.getCo(k) + "\t");
            }
            System.out.println();
        }

    }
}
