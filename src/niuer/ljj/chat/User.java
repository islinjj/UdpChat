package niuer.ljj.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * �洢�û���Ϣ
 * 
 * @author thinkpad_ljj
 *
 */
public class User {

	/*�û���*/
	String name;
	/*�˺�*/
	String account;
	/*�û�״̬,Ĭ������*/
	boolean state = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public boolean offline() {
		return false;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "   name:" + name + "   account:" + account + "  state:" + state;
	}

}
