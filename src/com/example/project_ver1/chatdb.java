package com.example.project_ver1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class chatdb {

	private Connection dbConnect = null; // Database objects
	// �s��object
	private Statement stat = null;
	// ����,�ǤJ��sql������r��
	private ResultSet rs = null;
	// ���G��
	private PreparedStatement pst = null;
	// ����,�ǤJ��sql���w�x���r��,�ݭn�ǤJ�ܼƤ���m
	// ���Q��?�Ӱ��Х�

	public chatdb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// System.out.println("���Udriver");
			// ���Udriver
			dbConnect = DriverManager.getConnection(SocketServer.SQLaddress, SocketServer.SQLId, SocketServer.SQLPW);
			// System.out.println("���odbConnectnection");
			// ���odbConnectnection

			// jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
			// localhost�O�D���W,test�Odatabase�W
			// useUnicode=true&characterEncoding=Big5�ϥΪ��s�X

		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		} // ���i��|����sqlexception
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

			while (rs.next()) // ���Φr����o�Ҧ�chatID
			{
				tem += rs.getString("chatID") + "\n";
				length++;
			}
			if (length > 0) {
				result = new int[length];
			}
			String[] tem2 = tem.split("\n"); // �ϥ�split���}�C��chatID
			for (int i = 0; i < length; i++) {
				result[i] = Integer.parseInt(tem2[i]); // ��string��chatID�ഫ��int
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

				if (userID == BID && newB == 1) // �n�q��Buyer
				{
					return true;
				} else if (userID == SID && newS == 1) // �n�q��Seller
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

				if (userID == BID && notiB == 1) // �n�q��Buyer
				{
					query = "update chatroomdb set notiBuyer=0 where chatID=?"; // ����q�������קK���гq��
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.executeUpdate();
					return true;
				} else if (userID == SID && notiS == 1) // �n�q��Seller
				{
					query = "update chatroomdb set notiSeller=0 where chatID=?"; // ����q�������קK���гq��
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
				System.out.println("��ѫǤw�s�b");
				return rs.getInt("chatID");
			}

			pst = dbConnect.prepareStatement(createQuery); // ����insert�@���s�����
			pst.setInt(1, ProductID);
			pst.setInt(2, SellerID);
			pst.setInt(3, BuyerID);
			pst.executeUpdate();

			createQuery = "select MAX(chatID) from chatroomdb where buyerID = ?"; // ����insert��ƪ�chatID��X��
			pst = dbConnect.prepareStatement(createQuery);
			pst.setInt(1, BuyerID);
			rs = pst.executeQuery();

			System.out.println("�إ߷s����ѫ�");
			if (rs.next())
				return rs.getInt("MAX(chatID)"); // �^��chatID
			else
				return -1;

		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // ���~
		} finally {
			Close();
		}
	}

	public int getChatRoomProductID(int chatID) {
		String query = "select productID from chatroomdb where chatID=?"; // ���chatroom���
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

		String query = "select * from chatroomdb where chatID=?"; // ���chatroom���
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next())
				if (userID == rs.getInt("buyerID")) // �p�G�O�R��,�q�����
				{
					query = "update chatroomdb set newForSeller=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID"))// �p�G�O���,�q���R��
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

		String query = "select * from chatroomdb where chatID=?"; // �����ѫǸ��
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next())
				if (userID == rs.getInt("buyerID")) // �p�G�O�R��,�����R�誺�q��
				{
					query = "update chatroomdb set newForBuyer=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID")) // �p�G�O���,������誺�q��
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

		String query = "select * from chatroomdb where chatID=?"; // ���chatroom���
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();
			if (rs.next())
				if (userID == rs.getInt("buyerID")) // �p�G�O�R��,�q�����
				{
					query = "update chatroomdb set notiSeller=1 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID"))// �p�G�O���,�q���R��
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

		String query = "select * from chatroomdb where chatID=?"; // �����ѫǸ��
		try {

			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, chatID);
			rs = pst.executeQuery();

			if (rs.next())
				if (userID == rs.getInt("buyerID")) // �p�G�O�R��,�����R�誺�q��
				{
					query = "update chatroomdb set notiBuyer=0 where chatID=?";
					pst = dbConnect.prepareStatement(query);
					pst.setInt(1, chatID);
					pst.execute();
				} else if (userID == rs.getInt("sellerID")) // �p�G�O���,������誺�q��
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
		String query = "select chatID from chatroomdb where buyerID = ?"; // ����o�ӶR�誺chatroom
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, userID);
			rs = pst.executeQuery();

			while (rs.next()) {
				result += rs.getInt("chatID") + ","; // �ϥ�,�ӹj�}�C��ID
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
		String query = "select chatID from chatroomdb where sellerID = ?"; // ����o�ӽ�誺chatroom
		try {
			pst = dbConnect.prepareStatement(query);
			pst.setInt(1, userID);
			rs = pst.executeQuery();

			while (rs.next()) {
				result += rs.getInt("chatID") + ","; // �ϥ�,�ӹj�}�C��ID
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
