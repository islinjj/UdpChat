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
 * �ͻ��� ʵ�ֵĹ��ܣ��û�ע�ᣬ��½ �ͻ�����������Ͽ�����
 * �ͻ���Ҳ����onlineUsers����udpSocket�رգ����߱�ͼ��̳��Ȳ������ȷ��͸�����������������ȡ��ǰ�û���udpSocket�Ķ˿ںţ��ڷ������������name�Ͽ����ӡ�
 * 
 * @author thinkpad_ljj
 *
 */
public class Client {

	Socket tcpSocket;
	DatagramSocket udpSocket;
	DatagramPacket packet;
	String state = "����";

	// �洢ע���û���Ϣ
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

			/* ��ȡ�Ѿ�ע����û� */
			registry(in);
			/* �����˺� */
			System.out.println("��������˺ţ�");
			account = read.nextLine();

			/* �������д���û��˺ţ��������ж��Ƿ���ڸ��˺� */
			out.write(account.getBytes());
			out.flush();

			/* �ж��û��Ƿ���ڱ��� */
			adjustUserTable(in, out, account);
			/* ��½ */
			Login(in, out, userInfo, account);
			/* ����udp�Ķ˿ںŸ������� */
			udpPortToSev(out);
			/* ���������û��б� */
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
	 * �ж��û��Ƿ����ע����� ������ڱ��оͲ�ע�ᣬ��������ע�� ��flag��ʼ��Ϊfalse���������û�
	 * ���������û������whileѭ����ע���Ϊ��ʱ��ʾ��������ע���û���ע���Ϊ��ʱ����Ҫ�������е�Ԫ�� ��������Ԫ���ҵ��û����½������������ע�ᡣ
	 * 
	 * @param in
	 * @param out
	 * @param account �û��˺�
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
			/* �û�ע�� */
			if (!existUser) {
				Register(in, out, userInfo);
				break;
			}
		}
	}

	/**
	 * �ӷ����������û�ע��� ֱ��ʹ��Gson().fromJson(.,.),����ָ�ʽ��ƥ��Ĵ��� ������������ԭ���ǣ������ڱ��������ͱ�����
	 * �������֮һ����Ҫ�����������ݽ����ɣ�����������
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void registry(InputStream in) throws IOException {
		/* ��ȡ�û�ע��� */
		byte[] b = new byte[1024 * 4];
		int size = in.read(b);
		String info = new String(b, 0, size);

		/* ���û�ע������Ϣ�������� */
		JsonArray array = new JsonParser().parse(info).getAsJsonArray();
		for (final JsonElement elem : array) {
			userInfo.add(new Gson().fromJson(elem, User.class));
		}
	}

	/**
	 * �û�ע�� ע��ʱ�������ǳƲ�ȷ���ǳƲ��ظ�,���������д���ǳ� �ɱ����������5λ�����˺ţ��ǳƺ��˺Ŷ�����ע������͸������� �������ᷴ��ע��ɹ�
	 * 
	 * @param userInfo �û�ע���
	 * @throws IOException
	 */
	private void Register(InputStream in, OutputStream out, HashSet<User> userInfo) throws IOException {
		/* �����ǳ� */
		System.out.println("����˺Ų�����");
		System.out.println("����������ǳ�");
		String nick = read.nextLine();
		for (User user : userInfo) {
			if (user.getName().equals(nick)) {
				System.out.println("�ǳ��ظ�,��������");
				nick = read.nextLine();
				break;
			}
		}
		out.write(nick.getBytes());
		out.flush();

		/* ��������˺� */
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

		/* ע��ɹ� */
		byte[] buf = new byte[1024];
		int s = in.read(buf);
		System.out.println("ע��ɹ�:" + new String(buf, 0, s));
	}

	/**
	 * ��������û� �ͻ��˽����ǳƺͶ˿ںţ��������ͽ��˿ںź��ǳƼ��뵽һ��Map���в�����Json�ĸ�ʽ�����ͻ���
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public Map<String, Double> OnlineUsers(InputStream in ,OutputStream out) throws IOException {
		/* ������ߺ��ѵ��ǳƺͶ˿ں� */
		int size;
		byte[] b = new byte[1024];
		size = in.read(b);
		String json = new String(b, 0, size);
		/* ������ߺ��ѵ���Ϣ */
		byte[] buf = new byte[1024];
		size = in.read(buf);
		String info = new String(buf, 0, size);
		System.out.printf("��ĺ���--->%s\n", info);
		Map<String, Double> map = new Gson().fromJson(json, HashMap.class);
		
		return map;
	}

	/**
	 * �����û��Ķ˿ںŸ������� ÿ���û��Ķ˿��ǲ�ͬ�ģ�ͨ���˿ں���������Ϣ ��ʼʹ�õ���getPort()��õ���-1��û������
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
	 * �����û������б� �Ȼ�����ߵı���whileѭ������·��ͷ������߱�֮ǰδ���µ��³��ֿ�ָ�������Ϊ�����û���Ϊ�գ����������ͺͽ��յ��߳�
	 * ��whileѭ������ֻҪ���û������˾ͳ������²����͸����е��û������ͷ�Ҳ���ϸ��������û��� �߳�������Ϊ�˽�cpu��Դ�ó�
	 * 
	 * @param in
	 * @param json
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void updateUserList(InputStream in, OutputStream out, String account)
			throws InterruptedException, IOException {
		/* ������ߺ��� */
		Map<String, Double> onlineUsers = OnlineUsers(in ,out);

		String nick = "";

		SendTask send = new SendTask(udpSocket);
		ReceiverTask receive = new ReceiverTask(udpSocket);

		/* ����SendTask�������û��б� */
		/* �ڵ�һ���˵�½��ʱ����ֻ���д�send */
		send.setUser(onlineUsers);

		Thread sender = new Thread(send);
		Thread receiver = new Thread(receive);

		sender.start();
		receiver.start();

		/* ���������û� */
		while (true) {
			
			onlineUsers = OnlineUsers(in ,out);

			/* ����SendTask�������û��б� */
			send.setUser(onlineUsers);

			/*��ʾ�ı����ͷ������Ǵ�������״̬�����Ǽ�����κ��޷��������ߺ����б�*/
			out.write("1".getBytes());
			out.flush();
			Thread.sleep(5000);
			
		}
	}

	/**
	 * �û���½ ��������û��˺ţ�����userInfo����˺Ŷ�Ӧ���û��������������� �������ٷ��ص�½�ɹ�����Ϣ
	 * �û���½Ӧ�úͷ����������ӣ����ر��˺ͷ����������ӵĴ���������
	 * 
	 * @param in
	 * @param out
	 * @param userInfo �û�ע���
	 * @param account  �û��˺�
	 * @throws IOException
	 */
	public void Login(InputStream in, OutputStream out, HashSet<User> userInfo, String account) throws IOException {
		int size;
		byte[] buf;
		/* д���û����������û����������� */
		String nick = "";
		for (User user : userInfo) {
			if (user.getAccount().equals(account)) {
				nick = user.getName();
			}
		}
		out.write(nick.getBytes());
		out.flush();

		/* �����������û���½�ɹ�����Ϣ */
		buf = new byte[1024];
		size = in.read(buf, 0, buf.length);
		System.out.println("��½�ɹ�:" + new String(buf, 0, size));
	}

	public static void main(String[] args) {
		/* �����ͻ��� */
		Client chat = new Client();
		chat.start();
	}

}
