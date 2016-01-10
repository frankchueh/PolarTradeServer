package com.example.project_ver1;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang.SerializationUtils;
import java.util.*;

@SuppressWarnings("restriction")
public class SocketServer {

	// static String SQLaddress =
	// "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=Big5"
	// ,SQLId = "user"
	// ,SQLPW = "12345678";

	static String SQLaddress = "jdbc:mysql://localhost:8038/schoolproject?useUnicode=true&characterEncoding=Big5",
			SQLId = "root", SQLPW = "steveandfrank";

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
					System.out.println("MessageRecevicer: receive " + conIp + " calling....");
					conn1.setSoTimeout(15000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("MessageRecevicer: Thread " + threadNo + "handling");
				MessageReceviceThread newMessageThread = new MessageReceviceThread(conn1, threadNo);
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
				System.out.println("Recevicer waiting...");
				String conIp = "";
				try {
					conn1 = socket1.accept();
					conIp = conn1.getInetAddress().toString();
					System.out.println("PhotoRecevicer: receive " + conIp + " calling....");
					conn1.setSoTimeout(15000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("PhotoRecevicer: Thread " + threadNo + "handling");
				PhotoReceviceThread newPhotoMessageThread = new PhotoReceviceThread(conn1, threadNo);
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

			System.out.println("PhotoReceviceThread " + threadNo + " handle " + conn.getInetAddress() + "'s calling");
		}

		public void run() {
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String command;
				command = br.readLine(); // 第一行為指令

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"), true);

				if (command.equals("GetPhoto")) {
					String photoPath = br.readLine();
					File f = new File(photoPath);
					if (f.exists()) {
						pw.println("success");
						System.out.println("檔案存在");
						byte[] photo = getFile(f);
						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						oos.writeObject(photo);

					} else {
						pw.println("fail");
						System.out.println("檔案不存在");
					}
				} else if (command.equals("UploadUserPhoto")) {

					String account = br.readLine();
					pw.println("OK");
					ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
					byte[] photo;
					try {
						if ((photo = (byte[]) ois.readObject()) != null) // 判斷是否有照片
						{
							FileManager photo_mgr = new FileManager("/UserPhoto/" + account + ".jpg");
							photo_mgr.writeObject(photo);
							photo_mgr = new FileManager("/UserPhoto/" + account + "_compress" + ".jpg");
							photo_mgr.writePhoto(photo, 0.2f);
							DBuser.setPhoto(account, photo_mgr.savePath); // 變更使用者照片
							pw.println("success");
						}
					}

					catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						pw.println("fail");
					}
				} else if (command.equals("uploadProductPhoto")) {

					int userID = DBuser.getUserID(br.readLine());
					pw.println("msg1 success");

					int productID = DBproduct.getNewProductIDbyUserID(userID); // 重新根據
																				// userID
																				// 取得
																				// productID

					ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
					byte[] photo;

					try {
						if ((photo = (byte[]) ois.readObject()) != null) // 判斷是否有
																			// product
																			// photo
																			// 傳來
						{
							FileManager photo_mgr = new FileManager("/product/" + productID + "/" + "photo.jpg");
							photo_mgr.writeObject(photo); // 將照片存到指定路徑
							photo_mgr = new FileManager("/product/" + productID + "_compress" + ".jpg");
							photo_mgr.writePhoto(photo, 0.2f);
							pw.println("msg2 success");
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				pw.close();
				// try {
				// Thread.sleep(3000);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

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

			System.out.println("MessageReceviceThread " + threadNo + " handle " + conn.getInetAddress() + "'s calling");
		}

		public void run() {
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

				String command;
				command = br.readLine(); // 第一行為指令

				System.out.println("NO." + threadNo + " : " + command);

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"), true);

				// 判斷指令是什麼
				if (command.equals("Login")) { // 登入指令,會回傳是否成功
					if (DBuser.Login(br.readLine(), br.readLine())) {
						pw.println("success");
						System.out.println("login success");
					} else {
						pw.println("fail");
						System.out.println("login fail");
					}
				} else if (command.equals("SignUp")) { // 建立帳戶指令,會回傳是否成功(帳戶是否已存在)
					String account = br.readLine();
					String password = br.readLine();
					String username = br.readLine();
					if (DBuser.SignUp(account, password, username)) {
						System.out.println("Sign up success");

						pw.println("success");
					} else {
						System.out.println("Sign up fail"); // 帳戶已存在
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
						if (DBuser.updateUserDetail(userID, Integer.parseInt(age), birth, sex, phone, email)) {
							pw.println("success");
						}
					} else
						pw.println("fail");
				} else if (command.equals("DownloadMessage")) {
					int chatID = Integer.parseInt(br.readLine());
					String Account = br.readLine();
					int userID = DBuser.getUserID(Account);
					DBchat.cancelNotificatiom(chatID, userID);
					FileManager chatData = new FileManager("/chatroom/" + chatID + ".txt");
					String[] tem_data = chatData.readAllLine();
					String data = "";
					for (int i = 0; i < tem_data.length; i++) {
						data += tem_data[i] + "\n";
					}
					pw.println("success");
					pw.println(data);

				}

				else if (command.equals("GetChatRoom")) {
					int PID = Integer.parseInt(br.readLine());
					int SID = Integer.parseInt(br.readLine());
					String Account = br.readLine();
					int BID = DBuser.getUserID(Account);
					int chatID = DBchat.getChatroom(PID, SID, BID);
					if (chatID != -1) {
						pw.println("success");
						pw.println(chatID);
					} else
						pw.println("fail");

				}

				else if (command.equals("UpdateMessage")) {
					try {
						ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
						String msg;
						msg = (String) ois.readObject();
						String[] msg_array = msg.split("\n");

						int chatID = Integer.parseInt(msg_array[0]);
						String Account = msg_array[1];
						int userID = DBuser.getUserID(Account);
						DBchat.sendNotification(chatID, userID);

						msg = "" + DBuser.getUserName(Account) + "(" + Account + "):\n";
						FileManager chatData = new FileManager("/chatroom/" + chatID + ".txt");
						for (int i = 2; i < msg_array.length; i++)
							msg += "\t" + msg_array[i] + "\n";
						chatData.writeLine(msg);
						pw.println("success");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				else if (command.equals("ListChatRoom")) {
					String Account = br.readLine();
					int userID = DBuser.getUserID(Account);
					String B = DBchat.getChatroomForBuyer(userID);
					if (B.equals("")) // 如果字串是空的split會有問題,所以增加一格空白
						B = " ";
					String S = DBchat.getChatroomForSeller(userID);
					if (S.equals("")) // 如果字串是空的split會有問題,所以增加一格空白
						S = " ";
					pw.println("success");
					pw.println(B);
					pw.println(S);
				}

				else if (command.equals("CheckMessage")) {
					String account = br.readLine();
					int userID = DBuser.getUserID(account);
					int[] chatID = DBchat.getChatID(userID);
					String new_message = "";
					if (chatID != null) {
						for (int ID : chatID) {
							if (DBchat.checkNotification(ID, userID)) {
								DBchat.cancelNotificatiom(ID, userID);
								new_message += ID + ",";
							}
						}
					}
					if (!new_message.equals("")) {
						pw.println("get new message");
						pw.println(new_message);
					} else {
						pw.println("no message");
					}
				} else if (command.equals("getLocate")) {
					int userID = DBuser.getUserID(br.readLine());
					double[] position = DBmap.getUserLocate(userID);
					if (position != null) {
						pw.println("success");
						pw.println(position[0]);
						pw.println(position[1]);
						System.out.println("get location: " + position[0] + ", " + position[1]);
					} else {
						pw.println("fail");
						System.out.println("get location failed");
					}
				} else if (command.equals("updateLocate")) {

					int userID = DBuser.getUserID(br.readLine());
					if (DBmap.uploadLocation(userID, Double.parseDouble(br.readLine()),
							Double.parseDouble(br.readLine()))) {
						pw.println("success");
						System.out.println("location upload success");
					} else {
						pw.println("failed");
						System.out.println("location upload failed");
					}
				} else if (command.equals("InsertProduct")) {
					// System.out.println("Product start insert");

					String productInfo = "";
					String[] s_productInfo;
					int productID;

					String productName = br.readLine(); // 讀取 product 名稱
					int productPrice = Integer.parseInt(br.readLine()); // 讀取
																		// product
																		// 金額
					int userID = DBuser.getUserID(br.readLine()); // 讀取使用者ID

					pw.println("msg1 success");

					productID = DBproduct.insertProduct(productName, productPrice, userID);
					if (productID != -1) { // 若 insert 成功 -> 建立商品路徑 ( 用來存 photo
											// , info )

						try {
							File product_dir = new File("C:/DataBase/product/" + productID);
							product_dir.mkdir();
						} catch (Exception e) {
							System.out.println("Create directory failed");
							e.printStackTrace();
						}

						ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
						byte[] buffer;

						try {
							if ((buffer = (byte[]) ois.readObject()) != null) // 判斷是否有
																				// product
																				// info傳來
							{

								FileManager info = new FileManager("/product/" + productID + "/" + "info.txt");
								productInfo = new String(buffer, StandardCharsets.UTF_8);
								s_productInfo = productInfo.split("\n");
								info.writeAllLine(s_productInfo); // 將product
																	// info存到指定路徑

								pw.println("msg2 success");
								pw.println(productID);
							}
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.out.println("Product insert failed!!");
						pw.println("upload failed");
					}

				} else if (command.equals("getCompressProduct")) { // 取得傳入商品ID的資料

					String[] pid_set = null;
					String[] pinfo = null;
					String[] temp_info = null;
					Product temp_product = null;
					byte[] photo = null;
					String pt = "";
					ArrayList<Product> product_set = new ArrayList<Product>();
					int productID = -1;

					// 接收pid,pid,pid....
					pid_set = br.readLine().split(",");

					if (pid_set != null) {
						pw.println("success");
						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						for (int i = 0; i < pid_set.length; i++) {
							productID = Integer.parseInt(pid_set[i]);
							String temp;
							// 有些商品可能以被刪除
							if ((temp = DBproduct.getProductInfo(productID)) != null) {
								pinfo = temp.split(",");
								FileManager info = new FileManager("/product/" + productID + "/" + "info.txt");
								temp_info = info.readAllLine();
								pt = buildStr(temp_info);
								String compress_photo_path = pinfo[3].replace(".jpg", "_compress.jpg");
								File f = new File(compress_photo_path); // get
																		// product
								// photo
								if (!f.exists()) { // 如果壓縮相片不存在, 使用原圖再壓一次
									f = new File(pinfo[3]);
									if (f.exists()) {
										photo = getFile(f);
										FileManager photo_mgr = new FileManager(
												"/product/" + productID + "/" + "photo_compress.jpg");
										photo_mgr.writePhoto(photo, 0.2f);
										photo = getFile(new File(compress_photo_path));
										System.out.println("Compress Photo success");
									}
								} else {
									photo = getFile(f);
								}

								if (photo != null) {
									System.out.println("get photo success");
								}
								temp_product = new Product(Integer.parseInt(pinfo[0]), pinfo[1],
										Integer.parseInt(pinfo[2]), pt.getBytes(Charset.forName("UTF-8")), photo,
										Integer.parseInt(pinfo[5]));
								product_set.add(temp_product);

							}
						}
						byte[] send_P = SerializationUtils.serialize(product_set);
						oos.writeObject(send_P);
						oos.flush();
					} else {
						pw.print("fail");
					}

				} else if (command.equals("getProduct")) { // 取得傳入商品ID的資料

					String[] pid_set = null;
					String[] pinfo = null;
					String[] temp_info = null;
					Product temp_product = null;
					byte[] photo = null;
					String pt = "";
					ArrayList<Product> product_set = new ArrayList<Product>();
					int productID = -1;

					// 接收pid,pid,pid....
					pid_set = br.readLine().split(",");

					if (pid_set != null) {
						pw.println("success");
						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						for (int i = 0; i < pid_set.length; i++) {
							productID = Integer.parseInt(pid_set[i]);
							String temp;
							// 有些商品可能以被刪除
							if ((temp = DBproduct.getProductInfo(productID)) != null) {
								pinfo = temp.split(",");
								FileManager info = new FileManager("/product/" + productID + "/" + "info.txt");
								temp_info = info.readAllLine();
								pt = buildStr(temp_info);
								File f = new File(pinfo[3]); // get product
																// photo
								if (f.exists()) {
									photo = getFile(f);
									if (photo != null) {
										System.out.println("get photo success");
									}
									temp_product = new Product(Integer.parseInt(pinfo[0]), pinfo[1],
											Integer.parseInt(pinfo[2]), pt.getBytes(Charset.forName("UTF-8")), photo,
											Integer.parseInt(pinfo[5]));
									product_set.add(temp_product);
								}
							}
						}
						byte[] send_P = SerializationUtils.serialize(product_set);
						oos.writeObject(send_P);
						oos.flush();
					} else {
						pw.print("fail");
					}

				}

				else if (command.equals("getUserProduct")) { // 取得使用者的所有商品ID

					String[] pid_set = null;
					String[] pinfo = null;
					String[] temp_info = null;
					Product temp_product = null;
					byte[] photo = null;
					String pt = "";
					String p_result = "";
					ArrayList<Product> product_set = new ArrayList<Product>();
					int productID = -1;

					int userID = DBuser.getUserID(br.readLine()); // 取得 userID
					p_result = DBproduct.getUserProduct(userID);

					if (p_result != "") {
						pid_set = p_result.split(",");
						System.out.println("product exist");
						pw.println("success");
						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						for (int i = 0; i < pid_set.length; i++) {
							productID = Integer.parseInt(pid_set[i]);
							pinfo = DBproduct.getProductInfo(productID).split(",");

							FileManager info = new FileManager("/product/" + productID + "/" + "info.txt");
							temp_info = info.readAllLine();
							pt = buildStr(temp_info);
							String compress_photo_path = pinfo[3].replace(".jpg", "_compress.jpg");
							File f = new File(compress_photo_path); // get
																	// product
							// photo
							if (!f.exists()) { // 如果壓縮相片不存在, 使用原圖再壓一次
								f = new File(pinfo[3]);
								if (f.exists()) {
									photo = getFile(f);
									FileManager photo_mgr = new FileManager(
											"/product/" + productID + "/" + "photo_compress.jpg");
									photo_mgr.writePhoto(photo, 0.2f);
									photo = getFile(new File(compress_photo_path));
									System.out.println("Compress Photo success");
								}
							} else {
								photo = getFile(f);
							}

							if (photo != null) {
								System.out.println("get photo success");
							}
							temp_product = new Product(Integer.parseInt(pinfo[0]), pinfo[1], Integer.parseInt(pinfo[2]),
									pt.getBytes(Charset.forName("UTF-8")), photo, Integer.parseInt(pinfo[5]));
							product_set.add(temp_product);

						}

						byte[] send_P = SerializationUtils.serialize(product_set);
						oos.writeObject(send_P);
						oos.flush();
					} else {
						pw.print("fail");
					}

				} else if (command.equals("updateProduct")) {

					Product u_product = null;
					String productInfo = "";
					String[] s_productInfo = null;
					FileManager info = null;
					FileOutputStream photoAinfo = null;
					pw.println("msg1 success");

					ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
					byte[] buffer;

					try {
						if ((buffer = (byte[]) ois.readObject()) != null) // 判斷是否有
																			// product
																			// photo
																			// 傳來
						{
							u_product = (Product) SerializationUtils.deserialize(buffer);
							DBproduct.updateProduct(u_product);

							info = new FileManager("/product/" + u_product.productID + "/" + "info.txt");
							productInfo = new String(u_product.productInfo, StandardCharsets.UTF_8);
							s_productInfo = productInfo.split("\n");
							info.writeAllLine(s_productInfo); // 將product
																// info存到指定路徑

							photoAinfo = new FileOutputStream(
									"C:/DataBase" + "/product/" + u_product.productID + "/" + "photo.jpg");
							photoAinfo.write(u_product.productPhoto);

							pw.println("msg2 success");
							photoAinfo.close();
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (command.equals("deleteProduct")) {

					int pos = Integer.parseInt(br.readLine());
					int pid = Integer.parseInt(br.readLine());

					if (DBproduct.deletProduct(pid) == 1) {
						System.out.println("Success delete product" + String.valueOf(pid));

						pw.println("success");
						pw.println(String.valueOf(pos));
					} else {
						pw.println("fail");
					}
				}

				else if (command.equals("searchProduct")) {
					// 接收參數
					double lat = Double.parseDouble(br.readLine());
					double lng = Double.parseDouble(br.readLine());
					String account = br.readLine();
					// 使用account取得userID
					int userID = DBuser.getUserID(account);
					// 回傳userID,lat,lng的字串陣列
					String[] around_users = DBmap.getRangeID(lat, lng, userID);
					if (around_users == null) {
						pw.println("no result");
					} else {
						String result = "";
						for (String user : around_users) {
							// 如果result不是第一行就加\n
							if (!result.equals(""))
								result += "\n";

							int around_userID = Integer.parseInt(user.split(",")[0]);
							String user_product = DBproduct.getUserProduct(around_userID);
							// 有product才加入result
							if (!user_product.equals("")) {
								result += user + ":" + user_product;
							}
						}
						pw.println("success");

						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						oos.writeObject(result);
						oos.flush();
					}
				}

				else if (command.equals("getNearUser")) { // 取得使用者的所有商品ID

					// 接收參數
					String account = br.readLine();
					double lat = Double.parseDouble(br.readLine());
					double lng = Double.parseDouble(br.readLine());
					double[] usr_position = new double[2];
					// 使用account取得userID
					int userID = DBuser.getUserID(account);
					// 回傳userID,lat,lng的字串陣列
					String[] around_users = DBmap.getRangeID(lat, lng, userID);
					if (around_users == null) {
						pw.println("no result");
					} else {
						String result = "";
						for (String user : around_users) {
							// 如果result不是第一行就加\n
							if (!result.equals(""))
								result += "\n";

							int around_userID = Integer.parseInt(user.split(",")[0]);
							usr_position = DBmap.getUserLocate(around_userID);
							// 有product才加入result
							if (usr_position != null) {
								result += DBuser.getUserNameByID(around_userID) + ":" + user.split(",")[1] + ":"
										+ user.split(",")[2];
							}
						}
						System.out.println(result);
						pw.println("success");

						ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
						oos.writeObject(result);
						oos.flush();
					}
				}
				pw.close();
				// try {
				// Thread.sleep(3000);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				conn.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private byte[] getFile(File file) {
		byte[] photo = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len = -1;
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			while ((len = fis.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}

			photo = outStream.toByteArray();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return photo;
	}

	protected String buildStr(String[] str_array) {

		String str = "";
		for (String s : str_array) {
			str += s + "\n";
		}
		return str;
	}
}
