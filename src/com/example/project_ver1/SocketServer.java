package com.example.project_ver1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang.SerializationUtils;

import java.util.*;


public class SocketServer {

	static String SQLaddress = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=Big5"
				,SQLId = "user"
				,SQLPW = "12345678";
	
//	 static String SQLaddress = "jdbc:mysql://localhost:8038/schoolproject?useUnicode=true&characterEncoding=Big5"
//	,SQLId = "root"
//	,SQLPW = "steveandfrank";
	
	userdb DBuser = new userdb();
    locationDB DBmap = new locationDB();
    productDB DBproduct = new productDB();
    chatdb DBchat = new chatdb();
	public void run() {

		MessageRecevicer mr = new MessageRecevicer();
		PhotoRecevicer pmr = new PhotoRecevicer();
		mr.start();
		pmr.start();
	}
	
	public static void main(String args[]) {
		
		InetAddress ip = null;
		String hostname;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			System.out.println("IP:" + ip + "\nname:" + hostname);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		(new SocketServer()).run();
		

	}
	
	
	class MessageRecevicer extends Thread {

		private ServerSocket socket1;
		private Socket conn1;
		private final int Port1 = 3838;

		public void run() {
			try {
				socket1 = new ServerSocket(Port1);

			} catch (Exception e) {
				e.printStackTrace();
			}

			int threadNo;
			for (threadNo = 1;; threadNo++) {
				System.out.println("MessageRecevicer waiting...");
				String conIp = "";
				try {
					conn1 = socket1.accept();
					conIp = conn1.getInetAddress().toString();
					System.out.println("MessageRecevicer: receive " + conIp
							+ " calling....");
					conn1.setSoTimeout(15000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("MessageRecevicer: Thread " + threadNo
						+ "handling");
				MessageReceviceThread newMessageThread = new MessageReceviceThread(
						conn1, threadNo);
				newMessageThread.start();

			}
		}
	}
	
	class PhotoRecevicer extends Thread {

		private ServerSocket socket1;
		private Socket conn1;
		private final int Port1 = 3839;

		public void run() {
			try {
				socket1 = new ServerSocket(Port1);

			} catch (Exception e) {
				e.printStackTrace();
			}

			int threadNo;
			for (threadNo = 1;; threadNo++) {
				System.out.println("PhotoRecevicer waiting...");
				String conIp = "";
				try {
					conn1 = socket1.accept();
					conIp = conn1.getInetAddress().toString();
					System.out.println("PhotoRecevicer: receive " + conIp
							+ " calling....");
					conn1.setSoTimeout(15000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("PhotoRecevicer: Thread " + threadNo
						+ "handling");
				PhotoReceviceThread newPhotoMessageThread = new PhotoReceviceThread(
						conn1, threadNo);
				newPhotoMessageThread.start();

			}
		}
	}
	
	class PhotoReceviceThread extends Thread {
		private Socket conn;
		int threadNo;
		FileManager fileMgr;

		public PhotoReceviceThread(Socket conn, int threadNo) {
			this.conn = conn;
			this.threadNo = threadNo;

			System.out.println("PhotoReceviceThread " + threadNo + " handle "
					+ conn.getInetAddress() + "'s calling");
		}

		public void run() {
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				
				String command;
				command = br.readLine(); // �Ĥ@�欰���O

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(
						conn.getOutputStream(), "utf-8"), true);
				
				if (command.equals("GetPhoto")) {
					String photoPath = br.readLine();
					File f = new File(photoPath);
					if (f.exists()) {
						pw.println("success");
						System.out.println("�ɮצs�b");

						FileInputStream fis = new FileInputStream(f);
						ObjectOutputStream oos = new ObjectOutputStream(
								conn.getOutputStream());
						byte[] buffer = new byte[1024];
						int len = -1;
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						while ((len = fis.read(buffer)) != -1) {
							outStream.write(buffer, 0, len);
						}
						byte[] photo = outStream.toByteArray();
						oos.writeObject(photo);
						fis.close();

					} else {
						pw.println("fail");
						System.out.println("�ɮפ��s�b");
					}
				}
				else if (command.equals("UploadUserPhoto")) {
					
					String account=br.readLine();
					pw.println("OK");
					ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
					byte[] buffer;
					try {
						if ((buffer = (byte[]) ois.readObject()) != null) // �P�_�O�_���Ӥ�
						{
							FileManager photo = new FileManager(
									"/UserPhoto/" + account + ".jpg");
							photo.writeObjec(buffer);
							DBuser.setPhoto(account, photo.savePath); // �ܧ�ϥΪ̷Ӥ�
							pw.println("success");
						}
					}
					
					catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						pw.println("fail");
					}
				}
				else if(command.equals("uploadProductPhoto")) {
					
					int userID = DBuser.getUserID(br.readLine());
					pw.println("msg1 success");
					
					int productID = DBproduct.getNewProductIDbyUserID(userID);  // ���s�ھ� userID ���o productID
					
					ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
					byte[] buffer;
					
					try {
						if((buffer=(byte[])ois.readObject())!=null)	//�P�_�O�_�� product photo �Ǩ�
						{
							FileManager photo = new FileManager("/product/"+ productID + "/" + "photo.jpg");
							photo.writeObjec(buffer);  // �N�Ӥ��s����w���|
							
							pw.println("msg2 success");
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				pw.close();
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				conn.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class MessageReceviceThread extends Thread {
		private Socket conn;
		int threadNo;
		FileManager fileMgr;

		public MessageReceviceThread(Socket conn, int threadNo) {
			this.conn = conn;
			this.threadNo = threadNo;

			System.out.println("MessageReceviceThread " + threadNo + " handle "
					+ conn.getInetAddress() + "'s calling");
		}

		public void run() {
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				
				String command;
				command = br.readLine(); // �Ĥ@�欰���O

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(
						conn.getOutputStream(), "utf-8"), true);
				
				
				//�P�_���O�O����
				if (command.equals("Login")) { // �n�J���O,�|�^�ǬO�_���\
					if (DBuser.Login(br.readLine(), br.readLine())) {
						pw.println("success");
						System.out.println("login success");
					} else {
						pw.println("fail");
						System.out.println("login fail");
					}
				} 
				else if (command.equals("SignUp")) { // �إ߱b����O,�|�^�ǬO�_���\(�b��O�_�w�s�b)
					String account = br.readLine();
					String password = br.readLine();
					String username = br.readLine();
					if (DBuser.SignUp(account, password, username)) {
						System.out.println("Sign up success");
						
						pw.println("success");
					} else {
						System.out.println("Sign up fail"); // �b��w�s�b
						pw.println("fail");
					}
				}

				else if (command.equals("GetUserInfo")) {
					String account = br.readLine();
					String userInfo = DBuser.getUserInfo(account);
					int userID = DBuser.getUserID(account);
					String userDetail = DBuser.getUserDetail(userID);

					if (userInfo != null) {
						pw.println("success");
						pw.println(userInfo);
						pw.println(userDetail);
					} else {
						pw.print("fail");
					}
				}

			    else if (command.equals("UpdateUserInfo")) {
					String account = br.readLine();
					String username = br.readLine();
					String age = br.readLine();
					String birth = br.readLine();
					String sex = br.readLine();
					String phone = br.readLine();
					String email = br.readLine();
	
					if (DBuser.updateUserinfo(username, account)) {
						int userID = DBuser.getUserID(account);
						if (DBuser.updateUserDetail(userID,
										Integer.parseInt(age), birth, sex,
										phone, email)) {
							pw.println("success");
						}
					} else
						pw.println("fail");
				}
				else if(command.equals("DownloadMessage"))
				{
					int chatID = Integer.parseInt(br.readLine());
					String Account = br.readLine();
					int userID = DBuser.getUserID(Account);
					DBchat.cancelNotificatiom(chatID, userID);
					FileManager chatData = new FileManager("/chatroom/"+chatID+".txt");
					String[] tem_data = chatData.readAllLine();
					String data="";
					for(int i=0;i<tem_data.length;i++)
					{
						data += tem_data[i];
					}
					pw.println("success");
					pw.println(data);
					
				}
				
				else if(command.equals("GetChatRoom"))
				{
					int PID = Integer.parseInt(br.readLine());
					int SID = Integer.parseInt(br.readLine());
					int BID = Integer.parseInt(br.readLine());
					int chatID = DBchat.getChatroom(PID, SID, BID);
					if(chatID!=-1)
					{
						pw.println("success");
						pw.println(chatID);
					}
					else
						pw.println("fail");
					
				}
				
				else if(command.equals("UpdateMessage"))
				{
					int chatID = Integer.parseInt(br.readLine());
					String Account = br.readLine();
					int userID = DBuser.getUserID(Account);
					
					DBchat.sendNotification(chatID, userID);
					FileManager chatData = new FileManager("/chatroom/"+chatID+".txt");
					String line;
					while(!(line=br.readLine()).equals("----MESSAGE----END----"))
					{
						chatData.writeLine(line);
					}
					pw.println("success");
				}
				
				else if(command.equals("ListChatRoom"))
				{
					String Account = br.readLine();
					int userID = DBuser.getUserID(Account);
					String B = DBchat.getChatroomForBuyer(userID);
					if(B.equals(""))	//�p�G�r��O�Ū�split�|�����D,�ҥH�W�[�@��ť�
						B=" ";
					String S = DBchat.getChatroomForSeller(userID);
					if(S.equals(""))	//�p�G�r��O�Ū�split�|�����D,�ҥH�W�[�@��ť�
						S=" ";
					pw.println("success");
					pw.println(B);
					pw.println(S);
				}
				else if(command.equals("updateLocate")) {
					
					int userID = DBuser.getUserID(br.readLine());
					if(DBmap.uploadLocation(userID,
											Double.parseDouble(br.readLine()),
											Double.parseDouble(br.readLine()))) {
						pw.println("success");
						System.out.println("location upload success");
					}
					else {
						pw.println("failed");
						System.out.println("location upload failed");
					}
				}
				else if(command.equals("InsertProduct")) {
					//System.out.println("Product start insert");

					String productInfo = "";
					String [] s_productInfo;
					int productID;
					
					String productName = br.readLine();   // Ū��  product �W��
					int productPrice = Integer.parseInt(br.readLine());  // Ū�� product ���B
					int userID = DBuser.getUserID(br.readLine());  // Ū���ϥΪ�ID
					 
					pw.println("msg1 success");
					
					productID = DBproduct.insertProduct(productName,productPrice,userID); 
					if(productID != -1) {  // �Y insert ���\ -> �إ߰ӫ~���| ( �ΨӦs photo , info )
						
						try {
							File product_dir = new File("C:/DataBase/product/"+ productID);
							product_dir.mkdir();
						}catch(Exception e) {
							System.out.println("Create directory failed");
							e.printStackTrace();
						}
				
						ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
						byte[] buffer;
						
						try {
							if((buffer=(byte[])ois.readObject())!=null)	//�P�_�O�_�� product info�Ǩ�
							{
				 
								FileManager info = new FileManager("/product/"+ productID +"/" + "info.txt");
								productInfo = new String(buffer,StandardCharsets.UTF_8);
								s_productInfo = productInfo.split("\n");
								info.writeAllLine(s_productInfo);;  // �Nproduct  info�s����w���|
								
								pw.println("msg2 success");
								pw.println(productID);
							}
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}
					else {
						System.out.println("Product insert failed!!");
						pw.println("upload failed");
					}
					
				}
				else if(command.equals("getUserProduct")) {    // ���o�ϥΪ̪��Ҧ��ӫ~ID
					
					String [] pid_set = null;
					String [] pinfo = null;
					Product temp_product = null;
					ArrayList <Product> product_set = new ArrayList <Product>();
					int productID = -1;
					
					int userID = DBuser.getUserID(br.readLine());  // ���o userID
					pid_set = DBproduct.getUserProduct(userID).split(",");  // ���o user �U���Ҧ��ӫ~ ID
					
					if(pid_set != null) {
						pw.println("success");
						ObjectOutputStream oos = new ObjectOutputStream(
								conn.getOutputStream());
						for(int i = 0; i < pid_set.length; i++) {
							productID = Integer.parseInt(pid_set[i]);
							pinfo = DBproduct.getProductInfo(productID).split(",");
							File f = new File(pinfo[3]);
							if (f.exists()) {
								FileInputStream fis = new FileInputStream(f);
								byte[] buffer = new byte[1024];
								int len = -1;
								ByteArrayOutputStream outStream = new ByteArrayOutputStream();  // �N�Ӥ�Ū�i��
								while ((len = fis.read(buffer)) != -1) {
									outStream.write(buffer, 0, len);
								}
								byte[] photo = outStream.toByteArray();  // �N outStream Ū�쪺����ন photo
								temp_product = new Product(Integer.parseInt(pinfo[0]),pinfo[1],Integer.parseInt(pinfo[2]),pinfo[4],photo);
								product_set.add(temp_product);
								fis.close();
							}	
						}
						byte[] send_P = SerializationUtils.serialize(product_set);
						oos.writeObject(send_P);
						oos.flush();
					}
					else {
						pw.print("fail");
					}
					
				}
				else if(command.equals("getProductInfo")) {
					// �O�d
				}

				pw.close();
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				conn.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
