package com.zhixian.mall.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.constant.WareConstant;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.inventory.dao.PurchaseDao;
import com.zhixian.mall.inventory.entity.PurchaseDetailEntity;
import com.zhixian.mall.inventory.entity.PurchaseEntity;
import com.zhixian.mall.inventory.service.PurchaseDetailService;
import com.zhixian.mall.inventory.service.PurchaseService;
import com.zhixian.mall.inventory.service.WareSkuService;
import com.zhixian.mall.inventory.vo.MergeVo;
import com.zhixian.mall.inventory.vo.PurchaseDoneVo;
import com.zhixian.mall.inventory.vo.PurchaseItemDoneVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
@RequiredArgsConstructor
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    private final PurchaseDetailService purchaseDetailService;

    private final WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchases(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).or().eq("status", 0);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detail = new PurchaseDetailEntity();
            detail.setId(item);
            detail.setPurchaseId(finalPurchaseId);
            detail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detail;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


    @Transactional
    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配
        List<PurchaseEntity> collect = ids.stream()
                .map(this::getById)
                .filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                        || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    item.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());

        // 2. 改变采购单状态
        this.updateBatchById(collect);

        // 3. 改变采购项状态
        collect.forEach(purchaseEntity -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(purchaseEntity.getId());
            entities.forEach(entity -> entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode()));
            purchaseDetailService.updateBatchById(entities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {

        // 1. 改变采购项状态
        List<PurchaseItemDoneVo> items = vo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();

        boolean flag = true;
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailUpdate = new PurchaseDetailEntity();
            Integer status = item.getStatus();
            Long itemId = item.getItemId();

            detailUpdate.setStatus(status);
            detailUpdate.setId(itemId);
            updates.add(detailUpdate);

            if (status == WareConstant.PurchaseStatusEnum.HASERROR.getCode()) {
                flag = false;
            }
            else {
                // 2. 将成功采购项入库
                PurchaseDetailEntity detail = purchaseDetailService.getById(itemId);
                wareSkuService.addStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
            }
        }

        purchaseDetailService.updateBatchById(updates);

        // 3. 改变采购单状态
        Long id = vo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag
                ? WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode()
                : WareConstant.PurchaseDetailStatusEnum.FAILURE.getCode()
        );
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}