
package org.module.eer.json;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class JsonER {

    @SerializedName("entities")
    @Expose
    private List<Entity> entities;
    @SerializedName("relations")
    @Expose
    private List<Relation> relations;
    @SerializedName("generalizations")
    @Expose
    private List<Generalization> generalizations;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public List<Generalization> getGeneralizations() {
        return generalizations;
    }

    public void setGeneralizations(List<Generalization> generalizations) {
        this.generalizations = generalizations;
    }

}
