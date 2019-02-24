package niuer.ljj.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * 建立连接后可以从socket中取用户信息
 * 
 * @author thinkpad_ljj
 *
 */
public class Service implements Runnable {

	Socket socket;
	User user = new User();

	// 用户注册表
	HashSet<User> userInfo;
	// 用户在线表
	Map<String, Integer> onlineUsers;

	public Service(Socket socket, Map<String, Integer> onlineUsers, HashSet<User> userInfo) {
		this.socket = socket;
		this.onlineUsers = onlineUsers;
		this.userInfo = userInfo;

	}

	// 用户在线表的长度
	int userLength = 0;

	@Override
	public void run() {

		try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
			/* 注册 */
			register(in, out);
			/* 返回登陆成功信息 */
			String nick = LoginMsgToClient(in, out);
			/* 返回给客户在线用户信息 */
			OnlineListToClient(in, out, onlineUsers, nick);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户注册 向客户端发送用户注册表userInfo，客户端可以查看是否存在用户，使用json传送
	 * 接收用户输入的账号acc，在服务器端判断是否存在用户，flag=false的时候不存在用户,当列表为空不存在用户的时候就读取用户的昵称和账号
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public void register(InputStream in, OutputStream out) throws IOException {
		int size;
		/* 客户端需要读取已存在的用户 */
		Gson gson = new Gson();
		String info = gson.toJson(userInfo);
		out.write(info.getBytes());
		out.flush();

		/* 接收用户输入的账号 */
		byte[] buffer = new byte[10];
		size = in.read(buffer);
		String acc = new String(buffer, 0, size);

		boolean existUser = false;
		while (!existUser) {
			if (!userInfo.isEmpty()) {
				for (User user : userInfo) {
					// 在用户注册表当中获得了账号，说明存在该用户，flag=true
					existUser = user.getAccount().equals(acc);
					// 存在用户则为true
					if (existUser) {
						System.out.println("账号存在");
						break;
						// 登陆
					}
				}
			}
			/* 用户注册 */
			if (!existUser) {
				byte[] b = new byte[1024];
				size = in.read(b);
				String name = new String(b, 0, size);
				user.setName(name);
				// 读取用户账号
				byte[] buf = new byte[10];
				size = in.read(b);
				String account = new String(b, 0, size);
				user.setAccount(account);

				userInfo.add(user);

				// 返回注册成功
				out.write(user.toString().getBytes());
				out.flush();

				System.out.println(name + "注册成功");
				break;
			}
		}
	}

	/**
	 * 获得在线用户 先获得端口号，存入在线用户信息表 更新在线用户表的方法是当表的长度和先前的表的长度不一样的时候进行更新
	 * 并将在线好友的信息发送给客户端,当客户端在线则更改客户端的状态
	 * 
	 * @param in
	 * @param out
	 * @param nick
	 * @param userInfo2
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void OnlineListToClient(InputStream in, OutputStream out, Map<String, Integer> onlineUsers, String nick)
			throws IOException, InterruptedException {
		/* 接收用户的端口号 */
		byte[] b = new byte[5];
		int s = in.read(b);
		int port = Integer.parseInt(new String(b, 0, s));

		// 存储用户的昵称和端口号，服务器通过端口号来确定聊天对象
		onlineUsers.put(nick, port);

		// 发送在线好友的信息
		// 将users的信息--->Json/XML，接收方再进行解析
		while (true) {
			/* 如果在线用户表的长度比原来长则说明要要更新在线用户表 */
			if (onlineUsers.size() > userLength) {
				connUser(out, in ,onlineUsers, nick);
			}
			userLength = onlineUsers.size();
			
			/*加上这段是为了能显示的表明服务器和客户端是否是连接的，当没有读到数据的时候就是客户端关闭了，但是加上这段后无法更新在线好友列表*/
			try {
				b = new byte[2];
				s = in.read(b);
			}catch(IOException e) {
				System.out.println(nick + "断开连接");
				Thread.currentThread().stop();
			}
			
			// 过3s就更新用户列表
			Thread.sleep(3000);
			
		}
	}

	/**
	 * 用户上线，返回给客户端在线表和用户信息表
	 * 
	 * @param out
	 * @param onlineUsers
	 * @param nick
	 * @throws IOException
	 */
	public void connUser(OutputStream out, InputStream in,Map<String, Integer> onlineUsers, String nick) throws IOException {
		/* 返回给客户端在线用户 */
		Gson gson = new Gson();
		String json = gson.toJson(onlineUsers);
		out.write(json.getBytes());
		out.flush();

		/* 遍历用户注册表，找到相同名称的，返回用户信息 */
		StringBuilder info = new StringBuilder();
		for (User user : userInfo) {
			if (user.getName().equals(nick)) {
				user.setState(true);
			}
			info.append(user.toString());
		}
		out.write(info.toString().getBytes());
		out.flush();
		
	}

	/**
	 * 返回登陆成功消息给客户端
	 * 
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public String LoginMsgToClient(InputStream in, OutputStream out) throws IOException {
		// 读取昵称
		byte[] buf = new byte[1024 * 4];
		int size = in.read(buf);
		String nick = new String(buf, 0, size);
		// 反馈给客户登陆成功
		out.write(nick.getBytes());
		out.flush();

		System.out.println("已连接：" + nick);
		return nick;
	}

	public static void main(String[] args) {
		/* 启动服务器 */
		ChatServer chatServer = new ChatServer();
		chatServer.start();
	}

}
