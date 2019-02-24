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
				/*接收的内容会放进packet当中*/
				byte[] buf = new byte[1024];
				packet = new DatagramPacket(buf, buf.length);

				// 无数据receive是不执行的
				socket.receive(packet);

				// 打印接收内容，目前是可以接收到所有人发到7000端口的数据
				byte[] data = packet.getData();
				msg = new String(data, 0, packet.getLength());
				System.out.println("收到消息：" + msg);
			} while (!msg.equals("goodbye"));

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
