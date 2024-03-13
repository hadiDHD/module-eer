
package org.module.eer.json;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Relation {

    @SerializedName("a")
    @Expose
    private A a;
    @SerializedName("b")
    @Expose
    private B b;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

}
