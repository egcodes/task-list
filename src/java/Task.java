/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;

/**
 *
 * @author erdigurbuz
 */
public class Task implements Serializable {

    private Boolean parent;
    private Boolean completed;
    private Boolean expand;
    private String rowKey;
    private String value;

    public Task() {
    }

    public Task(String value, String rowKey, Boolean parent, Boolean completed, Boolean expand) {
        this.value = value;
        this.rowKey = rowKey;
        this.parent = parent;
        this.completed = completed;
        this.expand = expand;
    }

    public Boolean getParent() {
        return parent;
    }

    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Boolean getExpand() {
        return expand;
    }

    public void setExpand(Boolean expand) {
        this.expand = expand;
    }

}
