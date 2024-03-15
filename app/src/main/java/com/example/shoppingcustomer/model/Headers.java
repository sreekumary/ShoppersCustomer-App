package com.example.shoppingcustomer.model;

import com.google.gson.annotations.SerializedName;

public class Headers{

	@SerializedName("content-type")
	private String contentType;

	public String getContentType(){
		return contentType;
	}
}