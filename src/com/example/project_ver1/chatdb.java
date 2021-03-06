package com.example.project_ver1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class chatdb {

	private Connection dbConnect = null; // Database objects
	// 連接object
	private Statement stat = null;
	// 執行,傳入之sql為完整字串
	private ResultSet rs = null;
	// 結果集
	private PreparedStatement pst = null;
	// 執行,傳入之sql為預儲之字申,需要傳入變數之位置
	// 先利用?來做標示

	public chatdb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// System.out.println("註冊driver");
			// 註冊driver
			dbConnect = DriverManager.getConnection(SocketServer.SQLaddress, SocketServer.SQLId, SocketServer.SQLPW);
			// System.out.println("取得dbConnectnection");
			// 取得dbConnectnection

			// jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
			// localhost是主機名,test是database名
			// useUnicode=true&characterEncoding=Big5使用的編碼

		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		} // 有可能會產生sqlexception
		catch (SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}

	public int[] getChatID(int userID) {
		String tem = "";
		int length = 0;
		int[] result = null;
		String query = "select chatID from chatroomdb where sellerID=? or buyerID=?";
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, userID);
			pst.setInt(2, userID);
			rs = pst.executeQuery();

			while (rs.next()) // 先用字串取得所有chatID
			{
				tem += rs.getString("chatID") + "\n";
				length++;
			}
			if (length > 0) {
				result = new int[length];
			}
			String[] tem2 = tem.split("\n"); // 使用split分開每個chatID
			for (int i = 0; i < length; i++) {
				result[i] = Integer.parseInt(tem2[i]); // 把string的chatID轉換成int
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			Close();
		}

	}

	public String getChatRoute(int chatID) {
		String query = "select chatRoute from chatroomdb where chatID=?";
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next()) {
				return rs.getString("chatRoute");
			}
			return null;
		} catch (Exception e) {
			return null;
		}

	}

	public boolean checkNewMessage(int chatID, int userID) {

		int BID, SID, newB, newS;
		String query = "select * from chatroomdb where chatID=?";
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next()) {
				BID = rs.getInt("buyerID");
				SID = rs.getInt("sellerID");
				newB = rs.getInt("newForBuyer");
				newS = rs.getInt("newForSeller");

				if (userID == BID && newB == 1) // 要通知Buyer
				{
					return true;
				} else if (userID == SID && newS == 1) // 要通知Seller
				{
					return true;
				} else {
					return false;
				}
			} else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			Close();
		}
	}

	public boolean checkNotification(int chatID, int userID) {

		int BID, SID, notiB, notiS;
		String query = "select * from chatroomdb where chatID=?";
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next()) {
				BID = rs.getInt("buyerID");
				SID = rs.getInt("sellerID");
				notiB = rs.getInt("notiBuyer");
				notiS = rs.getInt("notiSeller");

				if (userID == BID && notiB == 1) // 要通知Buyer
				{
					query = "update chatroomdb set notiBuyer=0 where chatID=?"; // 先把通知關掉避免重覆通知
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.executeUpdate();
					return true;
				} else if (userID == SID && notiS == 1) // 要通知Seller
				{
					query = "update chatroomdb set notiSeller=0 where chatID=?"; // 先把通知關掉避免重覆通知
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.executeUpdate();
					return true;
				} else {
					return false;
				}
			} else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			Close();
		}
	}

	public int getChatroom(int ProductID, int SellerID, int BuyerID) {

		String searchQuery = "select chatID from chatroomdb where productID=? and sellerID=? and buyerID=?";

		String createQuery = "insert chatroomdb(chatID, productID, sellerID, buyerID)"
				+ "select ifNULL(max(chatID),0)+1,?,?,? from chatroomdb";

		try {

			pst = dbConnect.prepareStatement(searchQuery);
			pst.setInt(1, ProductID);
			pst.setInt(2, SellerID);
			pst.setInt(3, BuyerID);
			rs = pst.executeQuery();
			if (rs.next()) {
				System.out.println("聊天室已存在");
				return rs.getInt("chatID");
			}

			pst = dbConnect.prepareStatement(createQuery); // 執行insert一筆新的資料
			pst.setInt(1, ProductID);
			pst.setInt(2, SellerID);
			pst.setInt(3, BuyerID);
			pst.executeUpdate();

			createQuery = "select MAX(chatID) from chatroomdb where buyerID = ?"; // 把剛剛insert資料的chatID抓出來
			pst = dbConnect.prepareStatement(createQuery);
			pst.setInt(1, BuyerID);
			rs = pst.executeQuery();

			System.out.println("建立新的聊天室");
			if (rs.next())
				return rs.getInt("MAX(chatID)"); // 回傳chatID
			else
				return -1;

		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // 錯誤
		} finally {
			Close();
		}
	}

	public int getChatRoomProductID(int chatID) {
		String query = "select productID from chatroomdb where chatID=?"; // 抓取chatroom資料
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next())
				return rs.getInt("productID");
			else
				return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} finally {
			Close();
		}
	}

	public void setNewMessage(int chatID, int userID) {

		String query = "select * from chatroomdb where chatID=?"; // 抓取chatroom資料
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next())
				if (userID == rs.getInt("buyerID")) // 如果是買方,通知賣方
				{
					query = "update chatroomdb set newForSeller=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID"))// 如果是賣方,通知買方
				{
					query = "update chatroomdb set newForBuyer=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public void cancelNewMessage(int chatID, int userID) {

		String query = "select * from chatroomdb where chatID=?"; // 抓取聊天室資料
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next())
				if (userID == rs.getInt("buyerID")) // 如果是買方,關掉買方的通知
				{
					query = "update chatroomdb set newForBuyer=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID")) // 如果是賣方,關掉賣方的通知
				{
					query = "update chatroomdb set newForSeller=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public void sendNotification(int chatID, int userID) {

		String query = "select * from chatroomdb where chatID=?"; // 抓取chatroom資料
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next())
				if (userID == rs.getInt("buyerID")) // 如果是買方,通知賣方
				{
					query = "update chatroomdb set notiSeller=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID"))// 如果是賣方,通知買方
				{
					query = "update chatroomdb set notiBuyer=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public void cancelNotificatiom(int chatID, int userID) {

		String query = "select * from chatroomdb where chatID=?"; // 抓取聊天室資料
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next())
				if (userID == rs.getInt("buyerID")) // 如果是買方,關掉買方的通知
				{
					query = "update chatroomdb set notiBuyer=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID")) // 如果是賣方,關掉賣方的通知
				{
					query = "update chatroomdb set notiSeller=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public String getChatroomForBuyer(int userID) {

		String result = "";
		String query = "select chatID from chatroomdb where buyerID = ?"; // 抓取這個買方的chatroom
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, userID);
			rs = pst.executeQuery();

			while (rs.next()) {
				result += rs.getInt("chatID") + ","; // 使用,來隔開每個ID
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			Close();
		}
	}

	public String getChatroomForSeller(int userID) {
		String result = "";
		String query = "select chatID from chatroomdb where sellerID = ?"; // 抓取這個賣方的chatroom
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, userID);
			rs = pst.executeQuery();

			while (rs.next()) {
				result += rs.getInt("chatID") + ","; // 使用,來隔開每個ID
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			Close();
		}
	}

	private void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}

//	 public static void main(String[] arg)
//	 {
//	 chatdb DB = new chatdb();
//	 DB.cancelNewMessage(4, 2);
//	 DB.cancelNewMessage(4, 4);
//	 if(DB.checkNewMessage(4, 2))
//		 System.out.println("new Message");;
//		 if(DB.checkNewMessage(4, 4))
//			 System.out.println("new Message");;
//	 }

}
