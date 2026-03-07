package com.medcommunity.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medcommunity.entity.*;
import com.medcommunity.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DrugCategoryMapper drugCategoryMapper;

    @Autowired
    private DrugInfoMapper drugInfoMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private DrugTransferMapper drugTransferMapper;

    @Autowired
    private DrugTransferItemMapper drugTransferItemMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        Long adminCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );

        if (adminCount > 0) {
            log.info("初始数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化数据...");

        initAdmin();
        List<Hospital> hospitals = initHospitals();
        List<DrugCategory> categories = initDrugCategories();
        List<DrugInfo> drugs = initDrugs(categories);
        initInventory(hospitals, drugs);
        initPurchaseOrders(hospitals, drugs);
        initTransfers(hospitals, drugs);
        initInventoryLogs(hospitals, drugs);

        log.info("数据初始化完成");
    }

    private void initAdmin() {
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        sysUserMapper.insert(admin);

        log.info("创建用户: admin/admin123");
    }

    private List<Hospital> initHospitals() {
        List<Hospital> list = new ArrayList<>();

        Hospital h1 = new Hospital();
        h1.setName("市中心医院");
        h1.setCode("H001");
        h1.setType("CENTER");
        h1.setAddress("市中心解放路128号");
        h1.setContact("张院长");
        h1.setPhone("0571-88881234");
        h1.setStatus(1);
        hospitalMapper.insert(h1);
        list.add(h1);

        Hospital h2 = new Hospital();
        h2.setName("东区分院");
        h2.setCode("H002");
        h2.setType("BRANCH");
        h2.setAddress("东区建设路56号");
        h2.setContact("李主任");
        h2.setPhone("0571-88882345");
        h2.setStatus(1);
        hospitalMapper.insert(h2);
        list.add(h2);

        Hospital h3 = new Hospital();
        h3.setName("西区卫生院");
        h3.setCode("H003");
        h3.setType("CLINIC");
        h3.setAddress("西区民生路88号");
        h3.setContact("王医生");
        h3.setPhone("0571-88883456");
        h3.setStatus(1);
        hospitalMapper.insert(h3);
        list.add(h3);

        Hospital h4 = new Hospital();
        h4.setName("南街社区卫生服务中心");
        h4.setCode("H004");
        h4.setType("CLINIC");
        h4.setAddress("南街幸福路22号");
        h4.setContact("赵护士");
        h4.setPhone("0571-88884567");
        h4.setStatus(1);
        hospitalMapper.insert(h4);
        list.add(h4);

        Hospital h5 = new Hospital();
        h5.setName("北区第二分院");
        h5.setCode("H005");
        h5.setType("BRANCH");
        h5.setAddress("北区工业路99号");
        h5.setContact("刘主任");
        h5.setPhone("0571-88885678");
        h5.setStatus(1);
        hospitalMapper.insert(h5);
        list.add(h5);

        log.info("创建机构: 5个（含地址、联系人、联系电话）");
        return list;
    }

    private List<DrugCategory> initDrugCategories() {
        List<DrugCategory> list = new ArrayList<>();

        // 西药 - 根
        DrugCategory c1 = new DrugCategory();
        c1.setName("西药");
        c1.setParentId(0L);
        c1.setSortOrder(1);
        drugCategoryMapper.insert(c1);
        list.add(c1);

        DrugCategory c1_1 = new DrugCategory();
        c1_1.setName("抗生素");
        c1_1.setParentId(c1.getId());
        c1_1.setSortOrder(1);
        drugCategoryMapper.insert(c1_1);
        list.add(c1_1);

        DrugCategory c1_2 = new DrugCategory();
        c1_2.setName("解热镇痛");
        c1_2.setParentId(c1.getId());
        c1_2.setSortOrder(2);
        drugCategoryMapper.insert(c1_2);
        list.add(c1_2);

        DrugCategory c1_3 = new DrugCategory();
        c1_3.setName("心血管");
        c1_3.setParentId(c1.getId());
        c1_3.setSortOrder(3);
        drugCategoryMapper.insert(c1_3);
        list.add(c1_3);

        DrugCategory c1_4 = new DrugCategory();
        c1_4.setName("消化系统");
        c1_4.setParentId(c1.getId());
        c1_4.setSortOrder(4);
        drugCategoryMapper.insert(c1_4);
        list.add(c1_4);

        // 中药 - 根
        DrugCategory c2 = new DrugCategory();
        c2.setName("中药");
        c2.setParentId(0L);
        c2.setSortOrder(2);
        drugCategoryMapper.insert(c2);
        list.add(c2);

        DrugCategory c2_1 = new DrugCategory();
        c2_1.setName("补益类");
        c2_1.setParentId(c2.getId());
        c2_1.setSortOrder(1);
        drugCategoryMapper.insert(c2_1);
        list.add(c2_1);

        DrugCategory c2_2 = new DrugCategory();
        c2_2.setName("清热类");
        c2_2.setParentId(c2.getId());
        c2_2.setSortOrder(2);
        drugCategoryMapper.insert(c2_2);
        list.add(c2_2);

        DrugCategory c2_3 = new DrugCategory();
        c2_3.setName("活血化瘀");
        c2_3.setParentId(c2.getId());
        c2_3.setSortOrder(3);
        drugCategoryMapper.insert(c2_3);
        list.add(c2_3);

        // 中成药 - 根
        DrugCategory c3 = new DrugCategory();
        c3.setName("中成药");
        c3.setParentId(0L);
        c3.setSortOrder(3);
        drugCategoryMapper.insert(c3);
        list.add(c3);

        DrugCategory c3_1 = new DrugCategory();
        c3_1.setName("感冒类");
        c3_1.setParentId(c3.getId());
        c3_1.setSortOrder(1);
        drugCategoryMapper.insert(c3_1);
        list.add(c3_1);

        DrugCategory c3_2 = new DrugCategory();
        c3_2.setName("肠胃类");
        c3_2.setParentId(c3.getId());
        c3_2.setSortOrder(2);
        drugCategoryMapper.insert(c3_2);
        list.add(c3_2);

        DrugCategory c3_3 = new DrugCategory();
        c3_3.setName("心脑血管");
        c3_3.setParentId(c3.getId());
        c3_3.setSortOrder(3);
        drugCategoryMapper.insert(c3_3);
        list.add(c3_3);

        log.info("创建药品分类: 3个根分类 + 10个子分类");
        return list;
    }

    private List<DrugInfo> initDrugs(List<DrugCategory> categories) {
        List<DrugInfo> list = new ArrayList<>();
        String[] drugData = {
                "D001|阿莫西林胶囊|阿莫西林|抗生素|0.5g*24粒|盒|胶囊剂|国药准字H12345678|华润|25.00",
                "D002|头孢克肟颗粒|头孢克肟|抗生素|50mg*6袋|盒|颗粒剂|国药准字H23456789|石药|38.00",
                "D003|布洛芬缓释胶囊|布洛芬|解热镇痛|0.3g*20粒|盒|胶囊剂|国药准字H34567890|中美史克|18.00",
                "D004|对乙酰氨基酚片|对乙酰氨基酚|解热镇痛|0.5g*20片|盒|片剂|国药准字H45678901|强生|12.00",
                "D005|硝苯地平缓释片|硝苯地平|心血管|10mg*30片|盒|片剂|国药准字H56789012|拜耳|35.00",
                "D006|阿司匹林肠溶片|阿司匹林|心血管|100mg*30片|盒|片剂|国药准字H67890123|拜耳|28.00",
                "D007|奥美拉唑肠溶胶囊|奥美拉唑|消化系统|20mg*14粒|盒|胶囊剂|国药准字H78901234|阿斯利康|42.00",
                "D008|蒙脱石散|蒙脱石|消化系统|3g*10袋|盒|散剂|国药准字H89012345|博福|22.00",
                "D009|六味地黄丸|熟地黄等|补益类|9g*10丸|盒|丸剂|国药准字Z12345678|同仁堂|45.00",
                "D010|板蓝根颗粒|板蓝根|清热类|10g*20袋|盒|颗粒剂|国药准字Z23456789|白云山|18.00",
                "D011|复方丹参片|丹参等|活血化瘀|0.32g*60片|瓶|片剂|国药准字Z34567890|天士力|32.00",
                "D012|感冒灵颗粒|三叉苦等|感冒类|10g*9袋|盒|颗粒剂|国药准字Z45678901|999|25.00",
                "D013|藿香正气水|广藿香等|肠胃类|10ml*10支|盒|酊剂|国药准字Z56789012|太极|15.00",
                "D014|复方降压片|利血平等|心脑血管|30片|瓶|片剂|国药准字Z67890123|北京降压|28.00",
                "D015|阿奇霉素分散片|阿奇霉素|抗生素|0.25g*6片|盒|片剂|国药准字H90123456|辉瑞|55.00",
                "D016|氨氯地平片|氨氯地平|心血管|5mg*7片|盒|片剂|国药准字H01234567|辉瑞|48.00",
                "D017|雷贝拉唑钠肠溶片|雷贝拉唑|消化系统|10mg*7片|盒|片剂|国药准字H12345678|卫材|68.00",
                "D018|双黄连口服液|金银花等|清热类|10ml*10支|盒|口服液|国药准字Z78901234|哈药|22.00",
                "D019|云南白药气雾剂|三七等|活血化瘀|60g+60g|套|气雾剂|国药准字Z89012345|云南白药|38.00",
                "D020|小儿氨酚黄那敏颗粒|对乙酰氨基酚等|感冒类|10g*12袋|盒|颗粒剂|国药准字H23456789|葵花|28.00",
                "D021|甲硝唑片|甲硝唑|抗生素|0.2g*21片|瓶|片剂|国药准字H34567890|石药|8.00",
                "D022|稳心颗粒|党参等|心脑血管|9g*9袋|盒|颗粒剂|国药准字Z90123456|步长|52.00"
        };

        int idx = 0;
        for (String line : drugData) {
            String[] parts = line.split("\\|");
            DrugInfo d = new DrugInfo();
            d.setCode(parts[0]);
            d.setName(parts[1]);
            d.setGenericName(parts[2]);
            d.setCategoryId(categories.get(1 + idx % 12).getId()); // 子分类
            d.setManufacturer(parts[8]);
            d.setSpec(parts[4]);
            d.setUnit(parts[5]);
            d.setDosageForm(parts[6]);
            d.setApprovalNumber(parts[7]);
            d.setPrice(new BigDecimal(parts[9]));
            d.setStatus(1);
            drugInfoMapper.insert(d);
            list.add(d);
            idx++;
        }

        log.info("创建药品: 22个");
        return list;
    }

    private void initInventory(List<Hospital> hospitals, List<DrugInfo> drugs) {
        for (Hospital h : hospitals) {
            for (int i = 0; i < drugs.size(); i++) {
                DrugInfo drug = drugs.get(i);
                DrugInventory inv = new DrugInventory();
                inv.setDrugId(drug.getId());
                inv.setHospitalId(h.getId());
                inv.setQuantity(150 + i * 20 + (int)(h.getId() * 10));
                inv.setWarningThreshold(100);
                inv.setBatchNo("B" + String.format("%04d", i + 1) + "-" + h.getCode());
                inv.setExpireDate(LocalDate.now().plusMonths(12 + i % 6));
                drugInventoryMapper.insert(inv);
            }
        }
        log.info("创建库存: {} 条", hospitals.size() * drugs.size());
    }

    private void initPurchaseOrders(List<Hospital> hospitals, List<DrugInfo> drugs) {
        String[] suppliers = {"华东医药", "国药控股", "九州通", "上药股份", "华润医药"};
        String[] statuses = {"PENDING", "PENDING", "APPROVED", "DELIVERED", "COMPLETED", "COMPLETED", "COMPLETED"};

        for (int i = 0; i < 22; i++) {
            PurchaseOrder order = new PurchaseOrder();
            order.setOrderNo("PO" + String.format("%06d", i + 1));
            order.setHospitalId(hospitals.get(i % hospitals.size()).getId());
            order.setSupplier(suppliers[i % suppliers.length]);
            order.setStatus(statuses[i % statuses.length]);
            order.setCreatedBy(1L);

            BigDecimal total = BigDecimal.ZERO;
            int itemCount = 2 + (i % 4);
            for (int j = 0; j < itemCount; j++) {
                DrugInfo drug = drugs.get((i + j) % drugs.size());
                int qty = 50 + (i + j) * 10;
                BigDecimal unitPrice = drug.getPrice();
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));
                total = total.add(amount);
            }
            order.setTotalAmount(total);
            order.setRemark("采购订单" + (i + 1));
            purchaseOrderMapper.insert(order);

            for (int j = 0; j < itemCount; j++) {
                DrugInfo drug = drugs.get((i + j) % drugs.size());
                int qty = 50 + (i + j) * 10;
                BigDecimal unitPrice = drug.getPrice();
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setOrderId(order.getId());
                item.setDrugId(drug.getId());
                item.setQuantity(qty);
                item.setUnitPrice(unitPrice);
                item.setAmount(amount);
                purchaseOrderItemMapper.insert(item);
            }
        }
        log.info("创建采购订单: 22个");
    }

    private void initTransfers(List<Hospital> hospitals, List<DrugInfo> drugs) {
        String[] statuses = {"PENDING", "PENDING", "APPROVED", "SHIPPING", "COMPLETED", "COMPLETED", "COMPLETED"};

        for (int i = 0; i < 22; i++) {
            DrugTransfer transfer = new DrugTransfer();
            transfer.setTransferNo("TF" + String.format("%06d", i + 1));
            transfer.setFromHospitalId(hospitals.get(i % hospitals.size()).getId());
            int toIdx = (i + 1) % hospitals.size();
            transfer.setToHospitalId(hospitals.get(toIdx).getId());
            transfer.setStatus(statuses[i % statuses.length]);
            transfer.setCreatedBy(1L);
            transfer.setRemark("调拨单" + (i + 1));
            drugTransferMapper.insert(transfer);

            int itemCount = 1 + (i % 3);
            for (int j = 0; j < itemCount; j++) {
                DrugTransferItem item = new DrugTransferItem();
                item.setTransferId(transfer.getId());
                item.setDrugId(drugs.get((i + j) % drugs.size()).getId());
                item.setQuantity(30 + (i + j) * 5);
                item.setBatchNo("B" + String.format("%04d", i + j + 1));
                drugTransferItemMapper.insert(item);
            }
        }
        log.info("创建调拨单: 22个");
    }

    private void initInventoryLogs(List<Hospital> hospitals, List<DrugInfo> drugs) {
        String[] types = {"IN", "OUT", "TRANSFER_IN", "TRANSFER_OUT", "ADJUST"};
        String[] operators = {"admin", "system"};

        for (int i = 0; i < 25; i++) {
            InventoryLog invLog = new InventoryLog();
            invLog.setDrugId(drugs.get(i % drugs.size()).getId());
            invLog.setHospitalId(hospitals.get(i % hospitals.size()).getId());
            invLog.setType(types[i % types.length]);
            invLog.setQuantity(i % 2 == 0 ? 50 + i * 5 : -(20 + i * 3));
            invLog.setBatchNo("B" + String.format("%04d", (i % 20) + 1));
            invLog.setOperator(operators[i % operators.length]);
            invLog.setRemark("库存操作" + (i + 1));
            inventoryLogMapper.insert(invLog);
        }
        log.info("创建库存日志: 25条");
    }
}
