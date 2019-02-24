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
 * �������Ӻ���Դ�socket��ȡ�û���Ϣ
 * 
 * @author thinkpad_ljj
 *
 */
public class Service implements Runnable {

	Socket socket;
	User user = new User();

	// �û�ע���
	HashSet<User> userInfo;
	// �û����߱�
	Map<String, Integer> onlineUsers;

	public Service(Socket socket, Map<String, Integer> onlineUsers, HashSet<User> userInfo) {
		this.socket = socket;
		this.onlineUsers = onlineUsers;
		this.userInfo = userInfo;

	}

	// �û����߱�ĳ���
	int userLength = 0;

	@Override
	public void run() {

		try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
			/* ע�� */
			register(in, out);
			/* ���ص�½�ɹ���Ϣ */
			String nick = LoginMsgToClient(in, out);
			/* ���ظ��ͻ������û���Ϣ */
			OnlineListToClient(in, out, onlineUsers, nick);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �û�ע�� ��ͻ��˷����û�ע���userInfo���ͻ��˿��Բ鿴�Ƿ�����û���ʹ��json����
	 * �����û�������˺�acc���ڷ��������ж��Ƿ�����û���flag=false��ʱ�򲻴����û�,���б�Ϊ�ղ������û���ʱ��Ͷ�ȡ�û����ǳƺ��˺�
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public void register(InputStream in, OutputStream out) throws IOException {
		int size;
		/* �ͻ�����Ҫ��ȡ�Ѵ��ڵ��û� */
		Gson gson = new Gson();
		String info = gson.toJson(userInfo);
		out.write(info.getBytes());
		out.flush();

		/* �����û�������˺� */
		byte[] buffer = new byte[10];
		size = in.read(buffer);
		String acc = new String(buffer, 0, size);

		boolean existUser = false;
		while (!existUser) {
			if (!userInfo.isEmpty()) {
				for (User user : userInfo) {
					// ���û�ע����л�����˺ţ�˵�����ڸ��û���flag=true
					existUser = user.getAccount().equals(acc);
					// �����û���Ϊtrue
					if (existUser) {
						System.out.println("�˺Ŵ���");
						break;
						// ��½
					}
				}
			}
			/* �û�ע�� */
			if (!existUser) {
				byte[] b = new byte[1024];
				size = in.read(b);
				String name = new String(b, 0, size);
				user.setName(name);
				// ��ȡ�û��˺�
				byte[] buf = new byte[10];
				size = in.read(b);
				String account = new String(b, 0, size);
				user.setAccount(account);

				userInfo.add(user);

				// ����ע��ɹ�
				out.write(user.toString().getBytes());
				out.flush();

				System.out.println(name + "ע��ɹ�");
				break;
			}
		}
	}

	/**
	 * ��������û� �Ȼ�ö˿ںţ����������û���Ϣ�� ���������û���ķ����ǵ���ĳ��Ⱥ���ǰ�ı�ĳ��Ȳ�һ����ʱ����и���
	 * �������ߺ��ѵ���Ϣ���͸��ͻ���,���ͻ�����������Ŀͻ��˵�״̬
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
		/* �����û��Ķ˿ں� */
		byte[] b = new byte[5];
		int s = in.read(b);
		int port = Integer.parseInt(new String(b, 0, s));

		// �洢�û����ǳƺͶ˿ںţ�������ͨ���˿ں���ȷ���������
		onlineUsers.put(nick, port);

		// �������ߺ��ѵ���Ϣ
		// ��users����Ϣ--->Json/XML�����շ��ٽ��н���
		while (true) {
			/* ��������û���ĳ��ȱ�ԭ������˵��ҪҪ���������û��� */
			if (onlineUsers.size() > userLength) {
				connUser(out, in ,onlineUsers, nick);
			}
			userLength = onlineUsers.size();
			
			/*���������Ϊ������ʾ�ı����������Ϳͻ����Ƿ������ӵģ���û�ж������ݵ�ʱ����ǿͻ��˹ر��ˣ����Ǽ�����κ��޷��������ߺ����б�*/
			try {
				b = new byte[2];
				s = in.read(b);
			}catch(IOException e) {
				System.out.println(nick + "�Ͽ�����");
				Thread.currentThread().stop();
			}
			
			// ��3s�͸����û��б�
			Thread.sleep(3000);
			
		}
	}

	/**
	 * �û����ߣ����ظ��ͻ������߱���û���Ϣ��
	 * 
	 * @param out
	 * @param onlineUsers
	 * @param nick
	 * @throws IOException
	 */
	public void connUser(OutputStream out, InputStream in,Map<String, Integer> onlineUsers, String nick) throws IOException {
		/* ���ظ��ͻ��������û� */
		Gson gson = new Gson();
		String json = gson.toJson(onlineUsers);
		out.write(json.getBytes());
		out.flush();

		/* �����û�ע����ҵ���ͬ���Ƶģ������û���Ϣ */
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
	 * ���ص�½�ɹ���Ϣ���ͻ���
	 * 
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public String LoginMsgToClient(InputStream in, OutputStream out) throws IOException {
		// ��ȡ�ǳ�
		byte[] buf = new byte[1024 * 4];
		int size = in.read(buf);
		String nick = new String(buf, 0, size);
		// �������ͻ���½�ɹ�
		out.write(nick.getBytes());
		out.flush();

		System.out.println("�����ӣ�" + nick);
		return nick;
	}

	public static void main(String[] args) {
		/* ���������� */
		ChatServer chatServer = new ChatServer();
		chatServer.start();
	}

}
