package blservice.infoservice;

import vo.CommodityVO;
import vo.MyTableModel;

public interface GetCommodityInterface {

	public CommodityVO getCommodity(String id);

	/**
	 * ����ĳһ�������Ƿ�����Ʒ
	 * @param categoryId ��Ʒ����id
	 * @return
	 */
	public boolean hasCommodity(String categoryId);
	
	public MyTableModel getCategoryCommodities(String categoryId);
}