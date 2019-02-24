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
 * ������ ������ߣ������ǳƣ��۲���ģʽ
 * 
 * @author thinkpad_ljj
 *
 */
public class ChatServer{

	//TCP�׽���
	ServerSocket serverSocket;
	
	//�������
	ExecutorService pool;
	
	//��¼�ͻ��˵���Ϣ
	/*���ƣ��˿ں�*/
	Map<String,Integer> users = new HashMap<>(); 
	
	//��¼�ͻ��˵���Ϣ
	HashSet<User> userInfo = new HashSet<>();
	
	public void start() {
		
		System.out.println("����������...");
		pool = Executors.newCachedThreadPool();
		
		try {
			//�������Ķ˿ں�
			serverSocket = new ServerSocket(9000);
			
			while(true) {
				//�������ӣ����û���Ϣ��¼
				Socket socket = serverSocket.accept();
				
				//���̳߳��е�һ���̴߳���
				//�׽��ֽ�����Ϣ�����users��
				Service onlineService = new Service(socket,users,userInfo);
				pool.execute(onlineService);
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
