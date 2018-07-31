package po.billpo;

import java.io.Serializable;

public abstract class BillPO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5233390848113316495L;
	public static int DRAFT = 0;
	public static int SAVED = 1;
	public static int COMMITED = 2;
	public static int PASS = 3;
	public static int NOTPASS = 4;
	
	private String date;
	private String time;
	private String id;
	private String operator;
	private int state;
	
	/**
	 * BillVO�Ĺ��췽��
	 * @param date �ƶ�����
	 * @param time �ƶ�ʱ��
	 * @param id ÿһ��֮�ڵı��
	 * @param operator ������Ա��id
	 */
	public BillPO(){};
	public BillPO(String date, String time, String id, String operator, int state) {
		this.date = date;
		this.time = time;
		this.id = id;
		this.operator = operator;
		this.state = state;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getId() {
		return id;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public int getState() {
		return state;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setState(int n) {
		state = n;
	}

	public void setOperatorId(String operatorId) {
		this.operator = operatorId;
	}
	
	/**
	 * �޸Ĵ˴�����->�޸�MainPanel���ܷ�ɾ�����ж�
	 * @return ����״̬����������
	 */
	public String getStateName() {
		switch(state) {
			case 0 : return "�ݸ�";
			case 1 : return "�ѱ���";
			case 2 : return "���ύ";
			case 3 : return "����ͨ��";
			case 4 : return "����δͨ��";
		}
		return "δ֪״̬";
	}
	/**
	 * 
	 * @return ����PO����������Id
	 */
	public abstract String getAllId();
}