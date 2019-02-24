package niuer.ljj.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器 监控上线，设置昵称，观察者模式
 * 
 * @author thinkpad_ljj
 *
 */
public class ChatServer{

	//TCP套接字
	ServerSocket serverSocket;
	
	//并发编程
	ExecutorService pool;
	
	//记录客户端的消息
	/*名称，端口号*/
	Map<String,Integer> users = new HashMap<>(); 
	
	//记录客户端的信息
	HashSet<User> userInfo = new HashSet<>();
	
	public void start() {
		
		System.out.println("服务器启动...");
		pool = Executors.newCachedThreadPool();
		
		try {
			//服务器的端口号
			serverSocket = new ServerSocket(9000);
			
			while(true) {
				//建立连接，把用户信息记录
				Socket socket = serverSocket.accept();
				
				//让线程池中的一个线程处理
				//套接字接收信息后放入users中
				Service onlineService = new Service(socket,users,userInfo);
				pool.execute(onlineService);
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
