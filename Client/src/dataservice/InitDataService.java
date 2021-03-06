package dataservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import po.AccountPO;
import po.CommodityPO;
import po.CustomerPO;

public interface InitDataService extends Remote{

	/**
	 * 获得各期初信息的起始时间
	 * @return
	 */
	public String[] getInitInfo() throws RemoteException;
	/**
	 * 获得当前账套，如果没有就返回null
	 * @return
	 */
	public String getYear() throws RemoteException;
	
	public ArrayList<CommodityPO> getCommodityInfo(String year) throws RemoteException;
	
	public ArrayList<CustomerPO> getCustomerInfo(String year) throws RemoteException;
	
	public ArrayList<AccountPO> getAccountInfo(String year) throws RemoteException;
	/**
	 * 期初信息初始化
	 * @return
	 */
	public boolean initNewOne() throws RemoteException;
}
