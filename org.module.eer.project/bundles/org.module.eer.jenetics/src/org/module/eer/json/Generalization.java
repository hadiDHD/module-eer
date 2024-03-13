
package org.module.eer.json;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Generalization {

    @SerializedName("parent")
    @Expose
    private Parent parent;
    @SerializedName("child")
    @Expose
    private Child child;

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

}
