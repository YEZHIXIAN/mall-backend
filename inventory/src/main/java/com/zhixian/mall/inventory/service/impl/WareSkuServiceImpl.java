package com.zhixian.mall.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.zhixian.mall.common.exception.NoStockException;
import com.zhixian.mall.common.to.mq.StockDetailTo;
import com.zhixian.mall.common.to.mq.StockLockedTo;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.common.vo.SkuHasStockVo;
import com.zhixian.mall.inventory.dao.WareSkuDao;
import com.zhixian.mall.inventory.entity.WareOrderTaskDetailEntity;
import com.zhixian.mall.inventory.entity.WareOrderTaskEntity;
import com.zhixian.mall.inventory.entity.WareSkuEntity;
import com.zhixian.mall.inventory.feign.ProductFeignService;
import com.zhixian.mall.inventory.service.WareOrderTaskDetailService;
import com.zhixian.mall.inventory.service.WareOrderTaskService;
import com.zhixian.mall.inventory.service.WareSkuService;
import com.zhixian.mall.inventory.vo.LockStockResult;
import com.zhixian.mall.inventory.vo.OrderItemVo;
import com.zhixian.mall.inventory.vo.SkuWareHasStock;
import com.zhixian.mall.inventory.vo.WareSkuLockVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private ProductFeignService productFeignService;

    @Transactional
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isNullOrEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        if (!StringUtils.isNullOrEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    @SuppressWarnings("unchecked")
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        String skuName = "";
        R info = productFeignService.info(skuId);

        if (info.getCode() == 0) {
            Map<String, Object> skuInfoMap = (Map<String, Object>) info.get("skuInfo");
            // 从 Map 中提取 skuName
            skuName = (String) skuInfoMap.get("skuName");
            System.out.println("Sku Name: " + skuName);
        } else {
            System.out.println("skuInfo is not of type Map");
        }


        // 1. 判断是否有库存记录
        List<WareSkuEntity> list = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (list == null || list.isEmpty()) {
            // 1.1. 无则新增
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setSkuName(skuName);
            wareSkuEntity.setStockLocked(0);
            this.save(wareSkuEntity);
        } else {
            // 1.2. 有则更改
            this.update(new UpdateWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId).setSql("stock = stock + " + skuNum));
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        return skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            WareSkuEntity wareSku = this.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(wareSku != null && wareSku.getStock() > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<LockStockResult> orderLockStock(WareSkuLockVo vo) {

        // 1. 创建工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 2. 获取每个锁定商品的库存信息
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());

            List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).gt("stock", 0));
            List<Long> wareIds = wareSkuEntities.stream().map(WareSkuEntity::getWareId).collect(Collectors.toList());
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 3. 锁定库存
        for (SkuWareHasStock stock : skuWareHasStocks) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareIds();
            if (wareIds == null || wareIds.isEmpty()) {
                // 3.1. 无库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                int count = this.count(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId).ge("stock", stock.getNum()));
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", stock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            if (!skuStocked) {
                throw new NoStockException(skuId);
            }
        }


        return List.of();
    }

}