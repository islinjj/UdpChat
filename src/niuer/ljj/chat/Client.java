package niuer.ljj.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * 客户端 实现的功能：用户注册，登陆 客户端与服务器断开连接
 * 客户端也操作onlineUsers表，当udpSocket关闭，在线表就减短长度并将长度发送给服务器，服务器获取当前用户的udpSocket的端口号，在服务器端输出”name断开连接“
 * 
 * @author thinkpad_ljj
 *
 */
public class Client {

	Socket tcpSocket;
	DatagramSocket udpSocket;
	DatagramPacket packet;
	String state = "上线";

	// 存储注册用户信息
	HashSet<User> userInfo = new HashSet<>();
	User user = new User();

	Scanner read = new Scanner(System.in);

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void start() {
		try {
			udpSocket = new DatagramSocket();
			tcpSocket = new Socket("127.0.0.1", 9000);
			InputStream in = tcpSocket.getInputStream();
			OutputStream out = tcpSocket.getOutputStream();
			String account = "";

			/* 读取已经注册的用户 */
			registry(in);
			/* 输入账号 */
			System.out.println("输入你的账号：");
			account = read.nextLine();

			/* 向服务器写入用户账号，服务器判断是否存在该账号 */
			out.write(account.getBytes());
			out.flush();

			/* 判断用户是否存在表中 */
			adjustUserTable(in, out, account);
			/* 登陆 */
			Login(in, out, userInfo, account);
			/* 发送udp的端口号给服务器 */
			udpPortToSev(out);
			/* 更新在线用户列表 */
			updateUserList(in, out, account);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断用户是否存在注册表中 如果存在表中就不注册，不存在则注册 。flag初始化为false代表不存在用户
	 * 当不存在用户则进入while循环，注册表为空时表示不存在已注册用户，注册表不为空时，需要遍历表中的元素 遍历表中元素找到用户则登陆，否则继续完成注册。
	 * 
	 * @param in
	 * @param out
	 * @param account 用户账号
	 * @throws IOException
	 */
	public void adjustUserTable(InputStream in, OutputStream out, String account) throws IOException {
		boolean existUser = false;

		while (!existUser) {
			if (!userInfo.isEmpty()) {
				for (User user : userInfo) {
					existUser = user.getAccount().equals(account);
					if (existUser) {
						break;
					}
				}
			}
			/* 用户注册 */
			if (!existUser) {
				Register(in, out, userInfo);
				break;
			}
		}
	}

	/**
	 * 从服务器接收用户注册表 直接使用Gson().fromJson(.,.),会出现格式不匹配的错误 造成这种问题的原因是：泛型在编译期类型被擦出
	 * 解决方案之一：将要被解析的数据解析成？？？？？？
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void registry(InputStream in) throws IOException {
		/* 读取用户注册表 */
		byte[] b = new byte[1024 * 4];
		int size = in.read(b);
		String info = new String(b, 0, size);

		/* 将用户注册表的信息解析出来 */
		JsonArray array = new JsonParser().parse(info).getAsJsonArray();
		for (final JsonElement elem : array) {
			userInfo.add(new Gson().fromJson(elem, User.class));
		}
	}

	/**
	 * 用户注册 注册时先设置昵称并确保昵称不重复,并向服务器写入昵称 由本地随机分配5位数的账号，昵称和账号都存入注册表并发送给服务器 服务器会反馈注册成功
	 * 
	 * @param userInfo 用户注册表
	 * @throws IOException
	 */
	private void Register(InputStream in, OutputStream out, HashSet<User> userInfo) throws IOException {
		/* 设置昵称 */
		System.out.println("你的账号不存在");
		System.out.println("请设置你的昵称");
		String nick = read.nextLine();
		for (User user : userInfo) {
			if (user.getName().equals(nick)) {
				System.out.println("昵称重复,重新输入");
				nick = read.nextLine();
				break;
			}
		}
		out.write(nick.getBytes());
		out.flush();

		/* 随机分配账号 */
		String acc = "";
		StringBuilder account = new StringBuilder();
		Random random = new Random();
		int i = 0;
		while (i < 5) {
			account.append(random.nextInt(10));
			i++;
		}
		out.write(account.toString().getBytes());
		out.flush();

		/* 注册成功 */
		byte[] buf = new byte[1024];
		int s = in.read(buf);
		System.out.println("注册成功:" + new String(buf, 0, s));
	}

	/**
	 * 获得在线用户 客户端接收昵称和端口号，服务器就将端口号和昵称加入到一个Map当中并且以Json的格式传给客户端
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public Map<String, Double> OnlineUsers(InputStream in ,OutputStream out) throws IOException {
		/* 获得在线好友的昵称和端口号 */
		int size;
		byte[] b = new byte[1024];
		size = in.read(b);
		String json = new String(b, 0, size);
		/* 输出在线好友的信息 */
		byte[] buf = new byte[1024];
		size = in.read(buf);
		String info = new String(buf, 0, size);
		System.out.printf("你的好友--->%s\n", info);
		Map<String, Double> map = new Gson().fromJson(json, HashMap.class);
		
		return map;
	}

	/**
	 * 发送用户的端口号给服务器 每个用户的端口是不同的，通过端口号来接收消息 开始使用的是getPort()获得的是-1即没有连接
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void udpPortToSev(OutputStream out) throws IOException {
		int udpPort = udpSocket.getLocalPort();
		out.write(String.valueOf(udpPort).getBytes());
		out.flush();
	}

	/**
	 * 更新用户在线列表 先获得在线的表，在while循环外更新发送方的在线表（之前未更新导致出现空指针错误，因为在线用户表为空），启动发送和接收的线程
	 * 在while循环当中只要有用户上线了就持续更新并发送给所有的用户，发送方也不断更新在线用户表 线程休眠是为了将cpu资源让出
	 * 
	 * @param in
	 * @param json
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void updateUserList(InputStream in, OutputStream out, String account)
			throws InterruptedException, IOException {
		/* 获得在线好友 */
		Map<String, Double> onlineUsers = OnlineUsers(in ,out);

		String nick = "";

		SendTask send = new SendTask(udpSocket);
		ReceiverTask receive = new ReceiverTask(udpSocket);

		/* 更新SendTask的在线用户列表 */
		/* 在第一个人登陆的时候是只进行此send */
		send.setUser(onlineUsers);

		Thread sender = new Thread(send);
		Thread receiver = new Thread(receive);

		sender.start();
		receiver.start();

		/* 更新在线用户 */
		while (true) {
			
			onlineUsers = OnlineUsers(in ,out);

			/* 更新SendTask的在线用户列表 */
			send.setUser(onlineUsers);

			/*显示的表明和服务器是处在连接状态，但是加上这段后无法更新在线好友列表*/
			out.write("1".getBytes());
			out.flush();
			Thread.sleep(5000);
			
		}
	}

	/**
	 * 用户登陆 传入的是用户账号，遍历userInfo获得账号对应的用户名，传给服务器 服务器再返回登陆成功的信息
	 * 用户登陆应该和服务器相连接，当关闭了和服务器相连接的窗口则下线
	 * 
	 * @param in
	 * @param out
	 * @param userInfo 用户注册表
	 * @param account  用户账号
	 * @throws IOException
	 */
	public void Login(InputStream in, OutputStream out, HashSet<User> userInfo, String account) throws IOException {
		int size;
		byte[] buf;
		/* 写入用户名并发送用户名给服务器 */
		String nick = "";
		for (User user : userInfo) {
			if (user.getAccount().equals(account)) {
				nick = user.getName();
			}
		}
		out.write(nick.getBytes());
		out.flush();

		/* 服务器反馈用户登陆成功的消息 */
		buf = new byte[1024];
		size = in.read(buf, 0, buf.length);
		System.out.println("登陆成功:" + new String(buf, 0, size));
	}

	public static void main(String[] args) {
		/* 启动客户端 */
		Client chat = new Client();
		chat.start();
	}

}
