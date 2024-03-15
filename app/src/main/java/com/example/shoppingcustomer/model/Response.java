package com.example.shoppingcustomer.model;

import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("headers")
	private Headers headers;

	@SerializedName("body")
	private DataBody body;

	@SerializedName("statusCode")
	private int statusCode;

	public Headers getHeaders(){
		return headers;
	}

	public DataBody getBody(){
		return body;
	}

	public int getStatusCode(){
		return statusCode;
	}
}