package com.example.shoppingcustomer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataBody {

	@SerializedName("inference")
	@Expose
	private Inference inference;
	@SerializedName("uploaded")
	@Expose
	private Boolean uploaded;
	@SerializedName("s3_path")
	@Expose
	private String s3Path;
	@SerializedName("box_conf")
	@Expose
	private Double boxConf;

	public Inference getInference() {
		return inference;
	}

	public void setInference(Inference inference) {
		this.inference = inference;
	}

	public Boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(Boolean uploaded) {
		this.uploaded = uploaded;
	}

	public String getS3Path() {
		return s3Path;
	}

	public void setS3Path(String s3Path) {
		this.s3Path = s3Path;
	}

	public Double getBoxConf() {
		return boxConf;
	}

	public void setBoxConf(Double boxConf) {
		this.boxConf = boxConf;
	}
}