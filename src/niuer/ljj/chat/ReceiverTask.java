package niuer.ljj.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReceiverTask implements Runnable {
	
	DatagramSocket socket;
	DatagramPacket packet;
	String msg = "";

	public ReceiverTask(DatagramSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			
			do {
				/*���յ����ݻ�Ž�packet����*/
				byte[] buf = new byte[1024];
				packet = new DatagramPacket(buf, buf.length);

				// ������receive�ǲ�ִ�е�
				socket.receive(packet);

				// ��ӡ�������ݣ�Ŀǰ�ǿ��Խ��յ������˷���7000�˿ڵ�����
				byte[] data = packet.getData();
				msg = new String(data, 0, packet.getLength());
				System.out.println("�յ���Ϣ��" + msg);
			} while (!msg.equals("goodbye"));

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
