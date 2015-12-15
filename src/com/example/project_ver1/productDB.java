package com.example.project_ver1;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;


public class productDB {
	
	// Database 物件
	private Connection dbConnect = null;
	// 執行後傳入SQL的字串
	private Statement input_stat = null;
	// Query　結果
	private ResultSet result = null;
	// 執行後傳入 SQL的預備字串 -> 等待給定數值 -> 用　"?" 做標示
	private PreparedStatement prepare_input_stat = null;
	
	String Qresult;
	
	
	// Database 連線
	public productDB() {
		
		try {
			// Driver 註冊
			Class.forName("com.mysql.jdbc.Driver");
			// 取得 Connection -> 給定主機名稱 ，Database 名稱  ，使用者登入資訊
			dbConnect = DriverManager.getConnection(
							SocketServer.SQLaddress, SocketServer.SQLId, SocketServer.SQLPW
							);
			
		}catch(ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		}
		catch(SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}
	
	// 根據 userID 取得其所有的 productID 
	
	public String getUserProduct(int userID) {
		
	    Qresult = null;
	    String query = "SELECT productID FROM productdb WHERE userid = ? AND state != \"deleted\";";
	    
	    try {
	    	prepare_input_stat = dbConnect.prepareStatement(query);
			prepare_input_stat.setInt(1, userID);
			result = prepare_input_stat.executeQuery();
	    	
	    	while(result.next()) {
	    		
	    		Qresult += result.getInt("productID") + ",";
	    	}
	    }catch(SQLException e) {
			System.out.println("SelectDB Exception :" + e.toString());
		}finally {
			close();
		}
		
		return Qresult;
	}
	
	// 根據 productID 取得其所有的相關資訊 ( 名稱 , 價格  , 照片路徑 , 資訊路徑 )
	
	public String getProductInfo(int productID) {
		
		Qresult = "";
		String query = "SELECT * FROM productdb WHERE productID = ? and state = \"new\";";
		try {
	    	prepare_input_stat = dbConnect.prepareStatement(query);
			prepare_input_stat.setInt(1, productID);
			result = prepare_input_stat.executeQuery();
	    	
			if(result.next()){
				Qresult += result.getInt("productID") + "," +
						   result.getString("Pname") + "," +
						   result.getInt("price") + "," +
						   result.getString("Pphoto") + "," +
						   result.getString("Pinfo") + "," +
						   result.getInt("userID");
			}
	    			   
	    }catch(SQLException e) {
			System.out.println("SelectDB Exception :" + e.toString());
		}finally {
			close();
		}
		
		if(Qresult.equals(""))
			return null;
		return Qresult;

	}
	
	// 更新商品相關資訊 ( 名稱 , 價格 )
	
	public void updateProduct(Product new_product) {
		
		String query = "UPDATE productdb SET Pname = ? , price = ? WHERE productID = ?;";
		
		try{
			prepare_input_stat = dbConnect.prepareStatement(query);
			prepare_input_stat.setString(1, new_product.productName);
			prepare_input_stat.setInt(2, new_product.productPrice);
			prepare_input_stat.setInt(3, new_product.productID);
			prepare_input_stat.executeUpdate();
			
		}catch(SQLException e) {
			System.out.println("UpdateDB Exception :" + e.toString());
		}finally {
			close();
		}
		
		
	}
	
	// 新增一筆新的商品資訊
	
	public int insertProduct(String Pname , int price , int userID) {
		
		String insertQuery = "INSERT INTO productdb(productID,Pname,price,userID,state)"
				+ "SELECT IFNULL(max(productID),0)+1 , ? , ? , ? , ? FROM productdb;";
		
		String selectQuery = "SELECT max(productID) FROM productdb WHERE userID = ?;";
		String updateQuery = "UPDATE productdb SET Pphoto = ? , Pinfo = ? WHERE productID = ?;";
		
		int newProductID = -1;  // 初始值為 -1 
		
		try{
			prepare_input_stat = dbConnect.prepareStatement(insertQuery);
			prepare_input_stat.setString(1, Pname);
			prepare_input_stat.setInt(2, price);
			prepare_input_stat.setInt(3, userID);
			prepare_input_stat.setString(4, "new");
			prepare_input_stat.executeUpdate();
			
			try{
				prepare_input_stat = dbConnect.prepareStatement(selectQuery);
				prepare_input_stat.setInt(1,userID);
				result = prepare_input_stat.executeQuery();
				
				if(result.next()) {
					newProductID = result.getInt("max(productID)");
				}
				
				String path = "C:/DataBase/product/" + newProductID + "/";
				
				try{
					prepare_input_stat = dbConnect.prepareStatement(updateQuery);
					prepare_input_stat.setString(1,path + "photo.jpg");
					prepare_input_stat.setString(2,path + "info.txt");
					prepare_input_stat.setInt(3,newProductID);
					prepare_input_stat.executeUpdate();
				}catch(SQLException e) {
					System.out.println("UpdateDB Exception :" + e.toString());
				}
				
			}catch(SQLException e) {
				System.out.println("SelectDB Exception :" + e.toString());
			}
			
		}catch(SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		}finally {
			close();
		}
		
		System.out.println("product insert success");
		
		return newProductID;
	}
	
	// 根據 productID 去刪除商品
	
	public int deletProduct(int productID) {
		
		int f = 0;
		String query = "UPDATE productdb SET state = \"deleted\" WHERE productID = ?;";
		
		try{
			prepare_input_stat = dbConnect.prepareStatement(query);
			prepare_input_stat.setInt(1, productID);
			prepare_input_stat.executeUpdate();
			f = 1;
		}catch(SQLException e) {
			System.out.println("UpdateDB Exception :" + e.toString());
		}finally {
			close();
		}
		
		return f;
	}
	
	// 新增商品圖片時，根據 userID 去取得其最新 (max)的 productID
	
	public int getNewProductIDbyUserID(int userID) {
		
		String selectQuery = "SELECT max(productID) FROM productdb WHERE userID = ?;";
		int photoProductID = -1;
		try {
			prepare_input_stat = dbConnect.prepareStatement(selectQuery);
			prepare_input_stat.setInt(1,userID);
			result = prepare_input_stat.executeQuery();
			
			if(result.next()) {
				photoProductID = result.getInt("max(productID)");
			}
			
		}catch(SQLException e) {
			System.out.println("SelectDB Exception :" + e.toString());
		}
		
		return photoProductID;
	}
	
	
	// Close 方法
	
	private void close() {
				
		try{
			if(result != null){
				result.close();
				result = null;
			}
			if(input_stat != null){
				input_stat.close();
				input_stat = null;
			}
			if(prepare_input_stat != null){
				prepare_input_stat.close();
				prepare_input_stat = null;
			}
		}catch(SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}
}
