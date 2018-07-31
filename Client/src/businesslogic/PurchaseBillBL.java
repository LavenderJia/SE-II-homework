package businesslogic;

import java.rmi.RemoteException;
import java.util.ArrayList;

import blservice.MailBLService;
import blservice.billblservice.BillExamineService;
import blservice.billblservice.BillOperationService;
import blservice.billblservice.PurchaseBillBLService;
import businesslogic.inter.AddLogInterface;
import dataservice.CommodityDataService;
import dataservice.CustomerDataService;
import dataservice.PurchaseBillDataService;
import ds_stub.CommodityDs_stub;
import ds_stub.CustomerDs_stub;
import ds_stub.PurchaseBillDs_stub;
import po.CommodityPO;
import po.CustomerPO;
import po.billpo.BillPO;
import po.billpo.PurchaseBillPO;
import po.billpo.SalesItemsPO;
import presentation.tools.Timetools;
import rmi.Rmi;
import vo.MyTableModel;
import vo.billvo.BillVO;
import vo.billvo.PurchaseBillVO;

/**
 * @author �Ҷ��
 */
public class PurchaseBillBL implements PurchaseBillBLService, BillOperationService, BillExamineService{
    
    private PurchaseBillDataService purchaseBillDs = Rmi.flag ? Rmi.getRemote(PurchaseBillDataService.class) : new PurchaseBillDs_stub();
    private AddLogInterface addLog = new LogBL();
	private MailBLService mailBL = new MailBL();
    private CustomerDataService customerDs = Rmi.flag ? Rmi.getRemote(CustomerDataService.class) : new CustomerDs_stub();
    private CommodityDataService commodityDs = Rmi.flag ? Rmi.getRemote(CommodityDataService.class) : new CommodityDs_stub();

    @Override
    public String getNewId() {
        try {
            return "JHD-" + Timetools.getDate() + "-" + purchaseBillDs.getNewId();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteBill(String id) {
        try{
            PurchaseBillPO bill = purchaseBillDs.getBillById(id);
            if(bill.getState() == BillPO.PASS) return false;
            if (purchaseBillDs.deleteBill(id)) {
            	addLog.add("ɾ��������", "ɾ���Ľ��������ݱ��Ϊ"+id);
            	return true;
            } else return false;
        }catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
	public boolean saveBill(PurchaseBillVO bill) {
		return saveBill(bill, "���������", "����Ľ��������ݱ��Ϊ"+bill.getAllId());
	}
	
    private boolean saveBill(PurchaseBillVO bill, String operation, String detail) {
    	try{
            if (purchaseBillDs.saveBill(toPO(bill))) {
            	addLog.add(operation, detail);
            	return true;
            } else return false;
        }catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public MyTableModel getFinishedBills(String customerId) {
        try{
            String field = "CONCAT(PBCondition,',',PBSupplierID)";
            String key = BillPO.PASS + "," + customerId;
            ArrayList<PurchaseBillPO> bills = purchaseBillDs.getBillsBy(field, key, true);
            return toModel(bills);
        }catch(RemoteException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MyTableModel search(String type, String key) {
        try{
            String field = null;
            if("���������".equals(type)){
                field = "PBID";
            }
            ArrayList<PurchaseBillPO> bills = purchaseBillDs.getBillsBy(field, key, true);
            return toModel(bills);
        }catch(RemoteException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MyTableModel getBillByDate(String from, String to) {
        try{
            ArrayList<PurchaseBillPO> bills = purchaseBillDs.getBillsByDate(from, to);
            return toModel(bills);
        }catch(RemoteException e){
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean offsetBill(String id){
        try{
            PurchaseBillPO bill = purchaseBillDs.getBillById(id);
            ArrayList<SalesItemsPO> items = new ArrayList<>();
            bill.getPurchaseBillItems().forEach(i -> items.add(new SalesItemsPO(
                i.getComId(), i.getComRemark(), -i.getComQuantity(), i.getComPrice(), -i.getComSum()
            )));
            PurchaseBillPO offset =new PurchaseBillPO(
                Timetools.getDate(), Timetools.getTime(), purchaseBillDs.getNewId(), bill.getOperator(), BillPO.PASS,
                bill.getSupplierId(), bill.getRemark(), -bill.getSum(), items
            );
            if (purchaseBillDs.saveBill(offset)) {
            	addLog.add("��������", "�����Ľ��������ݱ��Ϊ"+bill.getAllId());
            	return true;
            } else return false;
        }catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean copyBill(BillVO bill){
        if(bill instanceof PurchaseBillVO){
            PurchaseBillVO old = (PurchaseBillVO) bill;
            PurchaseBillVO copy = new PurchaseBillVO(
                Timetools.getDate(), Timetools.getTime(), this.getNewId().split("-")[2], old.getOperator(),
                BillVO.PASS, old.getCustomerId(), old.getModel(), old.getRemark(), old.getSum()
            );
            return saveBill(copy, "��岢���ƽ�����", "��岢���ƺ��µĽ��������ݱ��Ϊ"+copy.getAllId());
        }
        return false;
    }

    @Override
    public boolean examineBill(String id){
        try{
        	PurchaseBillPO billPO = purchaseBillDs.getBillById(id);
            PurchaseBillVO billVO = BillTools.toPurchaseBillVO(billPO);
            ArrayList<SalesItemsPO> list = billPO.getPurchaseBillItems();
            ArrayList<CommodityPO> commodityList = new ArrayList<CommodityPO>();
            boolean flag = true;
            CustomerPO customerPO = customerDs.findById(billPO.getSupplierId());
            flag = customerPO.setReceivable(billPO.getSum() + customerPO.getReceivable());
            
            for (int i = 0; i < list.size(); i++) {
            	SalesItemsPO item = list.get(i);
            	CommodityPO commodityPO = commodityDs.findById(item.getComId());
            	commodityPO.setRecentInPrice(item.getComPrice());
            	if (!commodityPO.setAmount(commodityPO.getAmount()+item.getComQuantity())) flag = false;
            	commodityList.add(commodityPO);
            }
            
            if (flag) {
            	customerDs.update(customerPO);
            	for (CommodityPO c : commodityList) commodityDs.update(c);
            	billVO.setState(3);
            	mailBL.saveMail("0001", billPO.getOperator(), "���ݱ��Ϊ"+id+"�Ľ�����ͨ����ˣ��뾡�������Ʒ������");
                return saveBill(billVO, "��˽�����", "ͨ����˵Ľ��������ݱ��Ϊ"+id);
            } else {
            	notPassBill(id);
            	return false;
            }
        }catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean notPassBill(String id){
        try{
            PurchaseBillVO billVO = BillTools.toPurchaseBillVO(purchaseBillDs.getBillById(id));
            billVO.setState(4);
            return saveBill(billVO, "��˽�����", "���ݱ��Ϊ"+id+"�Ľ��������δͨ��");
        }catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * This method is not intended for personal use
     */
    public ArrayList<PurchaseBillPO> getBillPOsByDate(String from, String to){
        try{
            return purchaseBillDs.getBillsByDate(from, to);
        }catch(RemoteException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private PurchaseBillPO toPO(PurchaseBillVO bill){
        ArrayList<SalesItemsPO> items = new ArrayList<>();
        for(int i = 0; i < bill.getModel().getRowCount(); i++){
            String[] row = bill.getModel().getValueAtRow(i);
            int num = Integer.parseInt(row[5]);
            double price = Double.parseDouble(row[4]),
                   sum = Double.parseDouble(row[6]);
            items.add(new SalesItemsPO(
                row[0], row[7], num, price, sum));
        }
        return new PurchaseBillPO(bill.getDate(), bill.getTime()
            , bill.getId(), bill.getOperator(), bill.getState()
            , bill.getCustomerId(), bill.getRemark()
            , bill.getSum(), items);
    }

    private MyTableModel toModel(ArrayList<PurchaseBillPO> bills){
        String[] columnNames = {"�ƶ�ʱ��", "���ݱ��"};
        String[][] data = new String[bills.size()][columnNames.length];
        for(int i = 0; i < data.length; i++){
            PurchaseBillPO purchaseBill = bills.get(i);
            data[i][0] = purchaseBill.getDate() + " " + purchaseBill.getTime();
            data[i][1] = "JHD-" + purchaseBill.getDate() + "-" + purchaseBill.getId(); 
        }
        return new MyTableModel(data, columnNames);
    }

	@Override
	public BillVO getBillById(String billId) {
		try {
			return BillTools.toPurchaseBillVO(purchaseBillDs.getBillById(billId));
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
}