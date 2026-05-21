package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "subservice_fields")
public class SubserviceField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "subservice_id", nullable = false)
    private int subserviceId;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "label_ru", nullable = false)
    private String labelRu;

    @Column(name = "field_type", nullable = false, columnDefinition = "ENUM('text','number','date','select','file','checkbox')")
    private String fieldType;

    @Column(nullable = false)
    private boolean required = true;

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    @Column(name = "is_repeating")
    private boolean isRepeating = false;

    public SubserviceField() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSubserviceId() { return subserviceId; }
    public void setSubserviceId(int subserviceId) { this.subserviceId = subserviceId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabelRu() { return labelRu; }
    public void setLabelRu(String labelRu) { this.labelRu = labelRu; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isRepeating() { return isRepeating; }
    public void setRepeating(boolean repeating) { isRepeating = repeating; }
}