package com.example.project_ver1;


import java.io.Serializable;

public class Product implements Serializable{
	
	int productID;
	String productName;
	int productPrice;
	String productInfo;
	byte[] productPhoto;
	
	Product(int pID , String pName, int pPrice , String pInfo , byte[] pPhoto) {
		this.productID = pID;
		this.productName = pName;
		this.productPrice = pPrice;
		this.productInfo = pInfo;
		this.productPhoto = pPhoto;
	}
}
