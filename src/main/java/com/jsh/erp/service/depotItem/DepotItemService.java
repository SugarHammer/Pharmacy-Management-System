package com.jsh.erp.service.depotItem;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.datasource.vo.DepotItemStockWarningCount;
import com.jsh.erp.datasource.vo.DepotItemVo4Stock;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.service.MaterialExtend.MaterialExtendService;
import com.jsh.erp.service.log.LogService;
import com.jsh.erp.service.material.MaterialService;
import com.jsh.erp.service.serialNumber.SerialNumberService;
import com.jsh.erp.service.systemConfig.SystemConfigService;
import com.jsh.erp.service.user.UserService;
import com.jsh.erp.utils.QueryUtils;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DepotItemService {
    private Logger logger = LoggerFactory.getLogger(DepotItemService.class);

    private final static String TYPE = "入库";
    private final static String SUM_TYPE = "number";
    private final static String IN = "in";
    private final static String OUT = "out";

    @Resource
    private DepotItemMapper depotItemMapper;
    @Resource
    private DepotItemMapperEx depotItemMapperEx;
    @Resource
    private MaterialService materialService;
    @Resource
    private MaterialExtendService materialExtendService;
    @Resource
    SerialNumberMapperEx serialNumberMapperEx;
    @Resource
    private DepotHeadMapper depotHeadMapper;
    @Resource
    SerialNumberService serialNumberService;
    @Resource
    private UserService userService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private MaterialCurrentStockMapper materialCurrentStockMapper;
    @Resource
    private LogService logService;

    public DepotItem getDepotItem(long id)throws Exception {
        DepotItem result=null;
        try{
            result=depotItemMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<DepotItem> getDepotItem()throws Exception {
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotItem> list=null;
        try{
            list=depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotItem> select(String name, Integer type, String remark, int offset, int rows)throws Exception {
        List<DepotItem> list=null;
        try{
            list=depotItemMapperEx.selectByConditionDepotItem(name, type, remark, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long countDepotItem(String name, Integer type, String remark) throws Exception{
        Long result =null;
        try{
            result=depotItemMapperEx.countsByDepotItem(name, type, remark);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepotItem(String beanJson, HttpServletRequest request)throws Exception {
        DepotItem depotItem = JSONObject.parseObject(beanJson, DepotItem.class);
        int result =0;
        try{
            result=depotItemMapper.insertSelective(depotItem);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepotItem(String beanJson, Long id, HttpServletRequest request)throws Exception {
        DepotItem depotItem = JSONObject.parseObject(beanJson, DepotItem.class);
        depotItem.setId(id);
        int result =0;
        try{
            result=depotItemMapper.updateByPrimaryKeySelective(depotItem);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteDepotItem(Long id, HttpServletRequest request)throws Exception {
        int result =0;
        try{
            result=depotItemMapper.deleteByPrimaryKey(id);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotItem(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andIdIn(idList);
        int result =0;
        try{
            result=depotItemMapper.deleteByExample(example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andIdNotEqualTo(id).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotItem> list =null;
        try{
            list=depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<DepotItemVo4DetailByTypeAndMId> findDetailByTypeAndMaterialIdList(Map<String, String> map)throws Exception {
        String mIdStr = map.get("mId");
        Long mId = null;
        if(!StringUtil.isEmpty(mIdStr)) {
            mId = Long.parseLong(mIdStr);
        }
        List<DepotItemVo4DetailByTypeAndMId> list =null;
        try{
            list = depotItemMapperEx.findDetailByTypeAndMaterialIdList(mId, QueryUtils.offset(map), QueryUtils.rows(map));
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long findDetailByTypeAndMaterialIdCounts(Map<String, String> map)throws Exception {
        String mIdStr = map.get("mId");
        Long mId = null;
        if(!StringUtil.isEmpty(mIdStr)) {
            mId = Long.parseLong(mIdStr);
        }
        Long result =null;
        try{
            result = depotItemMapperEx.findDetailByTypeAndMaterialIdCounts(mId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepotItemWithObj(DepotItem depotItem)throws Exception {
        int result =0;
        try{
            result = depotItemMapper.insertSelective(depotItem);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepotItemWithObj(DepotItem depotItem)throws Exception {
        int result =0;
        try{
            result = depotItemMapper.updateByPrimaryKeySelective(depotItem);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<DepotItem> getListByHeaderId(Long headerId)throws Exception {
        List<DepotItem> list =null;
        try{
            DepotItemExample example = new DepotItemExample();
            example.createCriteria().andHeaderIdEqualTo(headerId);
            list = depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotItemVo4WithInfoEx> getDetailList(Long headerId)throws Exception {
        List<DepotItemVo4WithInfoEx> list =null;
        try{
            list = depotItemMapperEx.getDetailList(headerId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotItemVo4WithInfoEx> findByAll(String name, String model, String endTime, Integer offset, Integer rows)throws Exception {
        List<DepotItemVo4WithInfoEx> list =null;
        try{
            list = depotItemMapperEx.findByAll(name, model, endTime, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int findByAllCount(String name, String model, String endTime)throws Exception {
        int result=0;
        try{
            result = depotItemMapperEx.findByAllCount(name, model, endTime);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public BigDecimal buyOrSale(String type, String subType, Long MId, String MonthTime, String sumType) throws Exception{
        BigDecimal result= BigDecimal.ZERO;
        try{
            if (SUM_TYPE.equals(sumType)) {
                result= depotItemMapperEx.buyOrSaleNumber(type, subType, MId, MonthTime, sumType);
            } else {
                result= depotItemMapperEx.buyOrSalePrice(type, subType, MId, MonthTime, sumType);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;

    }

    /**
     * 统计采购或销售的总金额
     * @param type
     * @param subType
     * @param MonthTime
     * @return
     * @throws Exception
     */
    public BigDecimal inOrOutPrice(String type, String subType, String MonthTime) throws Exception{
        BigDecimal result= BigDecimal.ZERO;
        try{
            result = depotItemMapperEx.inOrOutPrice(type, subType, MonthTime);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    /**
     * 2019-02-02修改
     * 我之前对操作数量的理解有偏差
     * 这里重点重申一下：BasicNumber=OperNumber*ratio
     * */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public String saveDetials(String inserted, String deleted, String updated, Long headerId, Long tenantId, HttpServletRequest request) throws Exception{
        //查询单据主表信息
        DepotHead depotHead=null;
        try{
            depotHead =depotHeadMapper.selectByPrimaryKey(headerId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        //获得当前操作人
        User userInfo=userService.getCurrentUser();
        //转为json
            JSONArray insertedJson = JSONArray.parseArray(inserted);
            JSONArray deletedJson = JSONArray.parseArray(deleted);
            JSONArray updatedJson = JSONArray.parseArray(updated);
            /**
             * 2019-01-28优先处理删除的
             * 删除的可以继续卖，删除的需要将使用的序列号回收
             * 插入的需要判断当前货源是否充足
             * 更新的需要判断货源是否充足
             * */
            if (null != deletedJson) {
                StringBuffer bf=new StringBuffer();
                for (int i = 0; i < deletedJson.size(); i++) {
                    //首先回收序列号，如果是调拨，不用处理序列号
                    JSONObject tempDeletedJson = JSONObject.parseObject(deletedJson.getString(i));
                    if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())
                            &&!BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())){
                        DepotItem depotItem = getDepotItem(tempDeletedJson.getLong("Id"));
                        if(depotItem==null){
                            continue;
                        }
                        /**
                         * 判断药品是否开启序列号，开启的收回序列号，未开启的跳过
                         * */
                        Material material= materialService.getMaterial(depotItem.getMaterialId());
                        if(material==null){
                            continue;
                        }
                        if(BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())){
                            serialNumberService.cancelSerialNumber(depotItem.getMaterialId(),depotItem.getHeaderId(),(depotItem.getBasicNumber()==null?0:depotItem.getBasicNumber()).intValue(),
                                    userInfo);
                        }
                    }
                    bf.append(tempDeletedJson.getLong("Id"));
                    if(i<(deletedJson.size()-1)){
                        bf.append(",");
                    }
                }
                this.batchDeleteDepotItemByIds(bf.toString());
                //更新当前库存
                for (int i = 0; i < deletedJson.size(); i++) {
                    JSONObject tempDeletedJson = JSONObject.parseObject(deletedJson.getString(i));
                    DepotItem depotItem = getDepotItem(tempDeletedJson.getLong("Id"));
                    updateCurrentStock(depotItem,tenantId);
                }
            }
            if (null != insertedJson) {
                for (int i = 0; i < insertedJson.size(); i++) {
                    DepotItem depotItem = new DepotItem();
                    JSONObject tempInsertedJson = JSONObject.parseObject(insertedJson.getString(i));
                    depotItem.setHeaderId(headerId);
                    Long materialExtendId = tempInsertedJson.getLong("MaterialExtendId");
                    Long materialId = materialExtendService.getMaterialExtend(materialExtendId).getMaterialId();
                    depotItem.setMaterialId(materialId);
                    depotItem.setMaterialExtendId(tempInsertedJson.getLong("MaterialExtendId"));
                    depotItem.setMaterialUnit(tempInsertedJson.getString("Unit"));
                    if (StringUtil.isExist(tempInsertedJson.get("OperNumber"))) {
                        depotItem.setOperNumber(tempInsertedJson.getBigDecimal("OperNumber"));
                        try {
                            String Unit = tempInsertedJson.get("Unit").toString();
                            BigDecimal oNumber = tempInsertedJson.getBigDecimal("OperNumber");
                            //以下进行单位换算
                            String unitName = materialService.findUnitName(materialId); //查询计量单位名称
                            if (!StringUtil.isEmpty(unitName)) {
                                String unitList = unitName.substring(0, unitName.indexOf("("));
                                String ratioList = unitName.substring(unitName.indexOf("("));
                                String basicUnit = unitList.substring(0, unitList.indexOf(",")); //基本单位
                                String otherUnit = unitList.substring(unitList.indexOf(",") + 1); //副单位
                                Integer ratio = Integer.parseInt(ratioList.substring(ratioList.indexOf(":") + 1).replace(")", "")); //比例
                                if (Unit.equals(basicUnit)) { //如果等于基础单位
                                    depotItem.setBasicNumber(oNumber); //数量一致
                                } else if (Unit.equals(otherUnit)) { //如果等于副单位
                                    depotItem.setBasicNumber(oNumber.multiply(new BigDecimal(ratio)) ); //数量乘以比例
                                }
                            } else {
                                depotItem.setBasicNumber(oNumber); //其他情况
                            }
                        } catch (Exception e) {
                            logger.error(">>>>>>>>>>>>>>>>>>>设置基础数量异常", e);
                        }
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("UnitPrice"))) {
                        depotItem.setUnitPrice(tempInsertedJson.getBigDecimal("UnitPrice"));
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("TaxUnitPrice"))) {
                        depotItem.setTaxUnitPrice(tempInsertedJson.getBigDecimal("TaxUnitPrice"));
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("AllPrice"))) {
                        depotItem.setAllPrice(tempInsertedJson.getBigDecimal("AllPrice"));
                    }
                    depotItem.setRemark(tempInsertedJson.getString("Remark"));
                    if (tempInsertedJson.get("DepotId") != null && !StringUtil.isEmpty(tempInsertedJson.get("DepotId").toString())) {
                        depotItem.setDepotId(tempInsertedJson.getLong("DepotId"));
                    }
                    if (tempInsertedJson.get("AnotherDepotId") != null && !StringUtil.isEmpty(tempInsertedJson.get("AnotherDepotId").toString())) {
                        depotItem.setAnotherDepotId(tempInsertedJson.getLong("AnotherDepotId"));
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("TaxRate"))) {
                        depotItem.setTaxRate(tempInsertedJson.getBigDecimal("TaxRate"));
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("TaxMoney"))) {
                        depotItem.setTaxMoney(tempInsertedJson.getBigDecimal("TaxMoney"));
                    }
                    if (StringUtil.isExist(tempInsertedJson.get("TaxLastMoney"))) {
                        depotItem.setTaxLastMoney(tempInsertedJson.getBigDecimal("TaxLastMoney"));
                    }
                    if (tempInsertedJson.get("OtherField1") != null) {
                        depotItem.setOtherField1(tempInsertedJson.getString("OtherField1"));
                    }
                    if (tempInsertedJson.get("OtherField2") != null) {
                        depotItem.setOtherField2(tempInsertedJson.getString("OtherField2"));
                    }
                    if (tempInsertedJson.get("OtherField3") != null) {
                        depotItem.setOtherField3(tempInsertedJson.getString("OtherField3"));
                    }
                    if (tempInsertedJson.get("OtherField4") != null) {
                        depotItem.setOtherField4(tempInsertedJson.getString("OtherField4"));
                    }
                    if (tempInsertedJson.get("OtherField5") != null) {
                        depotItem.setOtherField5(tempInsertedJson.getString("OtherField5"));
                    }
                    if (tempInsertedJson.get("MType") != null) {
                        depotItem.setMaterialType(tempInsertedJson.getString("MType"));
                    }
                    /**
                     * 出库时判断库存是否充足
                     * */
                    if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())){
                        if(depotItem==null){
                            continue;
                        }
                        Material material= materialService.getMaterial(depotItem.getMaterialId());
                        if(material==null){
                            continue;
                        }
                        BigDecimal stock = getStockByParam(depotItem.getDepotId(),depotItem.getMaterialId(),null,null,tenantId);
                        BigDecimal thisBasicNumber = depotItem.getBasicNumber()==null?BigDecimal.ZERO:depotItem.getBasicNumber();
                        if(systemConfigService.getMinusStockFlag() == false && stock.compareTo(thisBasicNumber)<0){
                            throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_CODE,
                                    String.format(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_MSG,material==null?"":material.getName()));
                        }

                        /**出库时处理序列号*/
                        if(!BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                            /**
                             * 判断药品是否开启序列号，开启的收回序列号，未开启的跳过
                             * */
                            if(BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())) {
                                //查询单据子表中开启序列号的数据列表
                                serialNumberService.checkAndUpdateSerialNumber(depotItem, userInfo);
                            }
                        }
                    }
                    this.insertDepotItemWithObj(depotItem);
                    //更新当前库存
                    updateCurrentStock(depotItem,tenantId);
                }
            }

            if (null != updatedJson) {
                for (int i = 0; i < updatedJson.size(); i++) {
                    JSONObject tempUpdatedJson = JSONObject.parseObject(updatedJson.getString(i));
                    DepotItem depotItem = this.getDepotItem(tempUpdatedJson.getLong("Id"));
                    if(depotItem==null){
                        continue;
                    }
                    Material material= materialService.getMaterial(depotItem.getMaterialId());
                    if(material==null){
                        continue;
                    }
                    //首先回收序列号
                    if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())
                            &&!BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                        /**
                         * 判断药品是否开启序列号，开启的收回序列号，未开启的跳过
                         * */
                        if(BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())) {
                            serialNumberService.cancelSerialNumber(depotItem.getMaterialId(), depotItem.getHeaderId(), (depotItem.getBasicNumber()==null?0:depotItem.getBasicNumber()).intValue(),
                                    userInfo);
                        }
                        /**收回序列号的时候释放库存*/
                        depotItem.setOperNumber(BigDecimal.ZERO);
                        depotItem.setBasicNumber(BigDecimal.ZERO);
                        this.updateDepotItemWithObj(depotItem);
                    }
                    depotItem.setId(tempUpdatedJson.getLong("Id"));
                    Long materialId = null;
                    if (StringUtil.isExist(tempUpdatedJson.get("MaterialExtendId"))) {
                        Long materialExtendId = tempUpdatedJson.getLong("MaterialExtendId");
                        materialId = materialExtendService.getMaterialExtend(materialExtendId).getMaterialId();
                        depotItem.setMaterialId(materialId);
                        depotItem.setMaterialExtendId(tempUpdatedJson.getLong("MaterialExtendId"));
                    }
                    depotItem.setMaterialUnit(tempUpdatedJson.getString("Unit"));
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("OperNumber").toString())) {
                        depotItem.setOperNumber(tempUpdatedJson.getBigDecimal("OperNumber"));
                        try {
                            String Unit = tempUpdatedJson.get("Unit").toString();
                            BigDecimal oNumber = tempUpdatedJson.getBigDecimal("OperNumber");
                            //以下进行单位换算
                            String unitName = materialService.findUnitName(materialId); //查询计量单位名称
                            if (!StringUtil.isEmpty(unitName)) {
                                String unitList = unitName.substring(0, unitName.indexOf("("));
                                String ratioList = unitName.substring(unitName.indexOf("("));
                                String basicUnit = unitList.substring(0, unitList.indexOf(",")); //基本单位
                                String otherUnit = unitList.substring(unitList.indexOf(",") + 1); //副单位
                                Integer ratio = Integer.parseInt(ratioList.substring(ratioList.indexOf(":") + 1).replace(")", "")); //比例
                                if (Unit.equals(basicUnit)) { //如果等于基础单位
                                    depotItem.setBasicNumber(oNumber); //数量一致
                                } else if (Unit.equals(otherUnit)) { //如果等于副单位
                                    depotItem.setBasicNumber(oNumber.multiply(new BigDecimal(ratio))); //数量乘以比例
                                }
                            } else {
                                depotItem.setBasicNumber(oNumber); //其他情况
                            }
                        } catch (Exception e) {
                            logger.error(">>>>>>>>>>>>>>>>>>>设置基础数量异常", e);
                        }
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("UnitPrice").toString())) {
                        depotItem.setUnitPrice(tempUpdatedJson.getBigDecimal("UnitPrice"));
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("TaxUnitPrice").toString())) {
                        depotItem.setTaxUnitPrice(tempUpdatedJson.getBigDecimal("TaxUnitPrice"));
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("AllPrice").toString())) {
                        depotItem.setAllPrice(tempUpdatedJson.getBigDecimal("AllPrice"));
                    }
                    depotItem.setRemark(tempUpdatedJson.getString("Remark"));
                    if (tempUpdatedJson.get("DepotId") != null && !StringUtil.isEmpty(tempUpdatedJson.get("DepotId").toString())) {
                        depotItem.setDepotId(tempUpdatedJson.getLong("DepotId"));
                    }
                    if (tempUpdatedJson.get("AnotherDepotId") != null && !StringUtil.isEmpty(tempUpdatedJson.get("AnotherDepotId").toString())) {
                        depotItem.setAnotherDepotId(tempUpdatedJson.getLong("AnotherDepotId"));
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("TaxRate").toString())) {
                        depotItem.setTaxRate(tempUpdatedJson.getBigDecimal("TaxRate"));
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("TaxMoney").toString())) {
                        depotItem.setTaxMoney(tempUpdatedJson.getBigDecimal("TaxMoney"));
                    }
                    if (!StringUtil.isEmpty(tempUpdatedJson.get("TaxLastMoney").toString())) {
                        depotItem.setTaxLastMoney(tempUpdatedJson.getBigDecimal("TaxLastMoney"));
                    }
                    depotItem.setOtherField1(tempUpdatedJson.getString("OtherField1"));
                    depotItem.setOtherField2(tempUpdatedJson.getString("OtherField2"));
                    depotItem.setOtherField3(tempUpdatedJson.getString("OtherField3"));
                    depotItem.setOtherField4(tempUpdatedJson.getString("OtherField4"));
                    depotItem.setOtherField5(tempUpdatedJson.getString("OtherField5"));
                    depotItem.setMaterialType(tempUpdatedJson.getString("MType"));
                    /**
                     * create by: qiankunpingtai
                     * create time: 2019/3/25 15:18
                     * website：https://qiankunpingtai.cn
                     * description:
                     * 修改了药品类型时，库中的药品和页面传递的不同
                     * 这里需要重新获取页面传递的药品信息
                     */
                    if(!material.getId().equals(depotItem.getMaterialId())){
                        material= materialService.getMaterial(depotItem.getMaterialId());
                        if(material==null){
                            continue;
                        }
                    }
                    /**出库时处理序列号*/
                    if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())){
                        BigDecimal stock = getStockByParam(depotItem.getDepotId(),depotItem.getMaterialId(),null,null,tenantId);
                        BigDecimal thisBasicNumber = depotItem.getBasicNumber()==null?BigDecimal.ZERO:depotItem.getBasicNumber();
                        if(systemConfigService.getMinusStockFlag() == false && stock.compareTo(thisBasicNumber)<0){
                            throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_CODE,
                                    String.format(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_MSG,material==null?"":material.getName()));
                        }
                        if(!BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                            /**
                             * 判断药品是否开启序列号，开启的收回序列号，未开启的跳过
                             * */
                            if(BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())) {
                                //查询单据子表中开启序列号的数据列表
                                serialNumberService.checkAndUpdateSerialNumber(depotItem, userInfo);
                            }
                        }
                    }
                    this.updateDepotItemWithObj(depotItem);
                    //更新当前库存
                    updateCurrentStock(depotItem,tenantId);
                }
            }
        return null;
    }
    /**
     * 查询计量单位信息
     *
     * @return
     */
    public String findUnitName(Long mId) throws Exception{
        String unitName = "";
        try {
            unitName = materialService.findUnitName(mId);
            if (unitName != null) {
                unitName = unitName.substring(1, unitName.length() - 1);
                if (unitName.equals("null")) {
                    unitName = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unitName;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotItemByIds(String ids)throws Exception {
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int  result =0;
        try{
            result =depotItemMapperEx.batchDeleteDepotItemByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public List<DepotItemStockWarningCount> findStockWarningCount(int offset, Integer rows, Integer pid) {

        List<DepotItemStockWarningCount> list = null;
        try{
            list =depotItemMapperEx.findStockWarningCount( offset, rows, pid);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int findStockWarningCountTotal(Integer pid) {
        int result = 0;
        try{
            result =depotItemMapperEx.findStockWarningCountTotal(pid);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    /**
     * 库存统计
     * @param depotId
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getStockByParam(Long depotId, Long mId, String beginTime, String endTime, Long tenantId){
        //初始库存
        BigDecimal initStock = materialService.getInitStockByMid(depotId, mId);
        //盘点复盘后数量的变动
        BigDecimal stockCheckSum = depotItemMapperEx.getStockCheckSum(depotId, mId, beginTime, endTime);
        DepotItemVo4Stock stockObj = depotItemMapperEx.getStockByParam(depotId, mId, beginTime, endTime, tenantId);
        BigDecimal intNum = stockObj.getInNum();
        BigDecimal outNum = stockObj.getOutNum();
        return initStock.add(intNum).subtract(outNum).add(stockCheckSum);
    }

    /**
     * 入库统计
     * @param depotId
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getInNumByParam(Long depotId, Long mId, String beginTime, String endTime, Long tenantId){
        DepotItemVo4Stock stockObj = depotItemMapperEx.getStockByParam(depotId, mId, beginTime, endTime, tenantId);
        return stockObj.getInNum();
    }

    /**
     * 出库统计
     * @param depotId
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getOutNumByParam(Long depotId, Long mId, String beginTime, String endTime, Long tenantId){
        DepotItemVo4Stock stockObj = depotItemMapperEx.getStockByParam(depotId, mId, beginTime, endTime, tenantId);
        return stockObj.getOutNum();
    }

    /**
     * 根据单据明细来批量更新当前库存
     * @param depotItem
     * @param tenantId
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateCurrentStock(DepotItem depotItem, Long tenantId){
        MaterialCurrentStockExample example = new MaterialCurrentStockExample();
        example.createCriteria().andMaterialIdEqualTo(depotItem.getMaterialId()).andDepotIdEqualTo(depotItem.getDepotId())
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialCurrentStock> list = materialCurrentStockMapper.selectByExample(example);
        MaterialCurrentStock materialCurrentStock = new MaterialCurrentStock();
        materialCurrentStock.setMaterialId(depotItem.getMaterialId());
        materialCurrentStock.setDepotId(depotItem.getDepotId());
        materialCurrentStock.setCurrentNumber(getStockByParam(depotItem.getDepotId(),depotItem.getMaterialId(),null,null,tenantId));
        if(list!=null && list.size()>0) {
            Long mcsId = list.get(0).getId();
            materialCurrentStock.setId(mcsId);
            materialCurrentStockMapper.updateByPrimaryKeySelective(materialCurrentStock);
        } else {
            materialCurrentStockMapper.insertSelective(materialCurrentStock);
        }
    }
}
