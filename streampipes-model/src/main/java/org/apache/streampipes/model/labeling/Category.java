package org.apache.streampipes.model.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {
    private String name;
    private String internalName;
    private Label superLabel;
    private List<Label> labels;

    @JsonProperty("_id")
    private @SerializedName("_id") String id;

    @JsonProperty("_rev")
    private @SerializedName("_rev") String rev;

    public Category() { }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return internalName;
    }
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Label getSuperLabel() { return superLabel; }
    public void setSuperLabel(Label superLabel) { this.superLabel = superLabel; }

    public List<Label> getLabels() { return labels; }
    public void setLabels(List<Label> labels) { this.labels = labels; }

    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getRev() {
        return rev;
    }
    public void setRev(String rev) {
        this.rev = rev;
    }
}
