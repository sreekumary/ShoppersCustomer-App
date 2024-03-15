package com.example.shoppingcustomer.model;

public class ProductTestRequest {

    public ProductTestRequest(Double box_conf, int annotate, String version, String return_name) {
        this.box_conf = box_conf;
        this.annotate = annotate;
        this.version = version;
        this.return_name = return_name;
    }

    public Double getBox_conf() {
        return box_conf;
    }

    public void setBox_conf(Double box_conf) {
        this.box_conf = box_conf;
    }

    public int getAnnotate() {
        return annotate;
    }

    public void setAnnotate(int annotate) {
        this.annotate = annotate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReturn_name() {
        return return_name;
    }

    public void setReturn_name(String return_name) {
        this.return_name = return_name;
    }

    Double box_conf;
    int annotate;
    String version;
    String return_name;

}
