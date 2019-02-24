package niuer.ljj.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class SendTask implements Runnable {

	DatagramSocket socket;
	DatagramPacket packet;
	InetAddress address;
	String msg = "";
	Map<String, Double> onlineUsers;
	Client client = new Client();

	public SendTask(DatagramSocket socket) {
		this.socket = socket;
	}

	public void setUser(Map<String, Double> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}

	@Override
	public void run() {

		try {
			address = InetAddress.getByName("127.0.0.1");

			do {
				Scanner read = new Scanner(System.in);
				System.out.print("Ŀ�����");
				String goalObject = read.nextLine();

				System.out.print("������Ϣ:");
				msg = read.nextLine();
				byte[] data = msg.getBytes();

				// ���ݰ�,����Ϣ���͸�������,������ͨ��value�ҵ�key
				double port = onlineUsers.get(goalObject);
				packet = new DatagramPacket(data, data.length, address, (int) port);

				// ����
				socket.send(packet);

			} while (!msg.equals("goodbye"));
			
			//���û�����"goodbye"�ʹ��������ߣ��ͷ������߸�Client
			Thread.currentThread().setName("����");

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}