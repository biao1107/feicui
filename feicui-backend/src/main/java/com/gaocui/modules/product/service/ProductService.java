package com.gaocui.modules.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.common.oss.OssService;
import com.gaocui.common.security.SecurityContext;
import com.gaocui.modules.ai.service.ProductKnowledgeBase;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.service.MerchantService;
import com.gaocui.modules.product.dto.ProductDetailVO;
import com.gaocui.modules.product.dto.ProductListItemVO;
import com.gaocui.modules.product.dto.ProductSaveRequest;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品服务: 上传图片、草稿/发布/上下架状态机、CRUD、额度校验.
 */
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final MerchantService merchantService;
    private final OssService ossService;
    private final ProductKnowledgeBase knowledgeBase;

    // ---------- 图片上传 ----------
    public String uploadImage(MultipartFile file) {
        return ossService.uploadImage(file);
    }

    // ---------- 创建草稿 ----------
    public Long createDraft(ProductSaveRequest req) {
        Product p = new Product();
        p.setMerchantId(SecurityContext.currentMerchantId());
        applyFields(p, req);
        p.setStatus(Product.STATUS_DRAFT);
        p.setAiGenerated(req.getAiGenerated() == null ? 0 : req.getAiGenerated());
        productMapper.insert(p);
        return p.getId();
    }

    // ---------- 编辑 ----------
    public void update(Long id, ProductSaveRequest req) {
        Product p = mustGetOwned(id);
        applyFields(p, req);
        productMapper.updateById(p);
        // 已上架商品编辑后文本变化, 需同步向量库; 草稿/下架不进检索
        if (Product.STATUS_LISTED.equals(p.getStatus())) {
            knowledgeBase.rebuild();
        }
    }

    // ---------- 发布 (草稿→上架), 校验额度 ----------
    public void publish(Long id) {
        Product p = mustGetOwned(id);
        if (Product.STATUS_LISTED.equals(p.getStatus())) {
            return; // 已上架, 幂等
        }
        assertCanList(p.getMerchantId(), id);
        p.setStatus(Product.STATUS_LISTED);
        productMapper.updateById(p);
        knowledgeBase.rebuild();
    }

    // ---------- 上下架状态切换 ----------
    public void changeStatus(Long id, String targetStatus) {
        Product p = mustGetOwned(id);
        if (!Product.STATUS_LISTED.equals(targetStatus) && !Product.STATUS_DELISTED.equals(targetStatus)) {
            throw new BusinessException(ResultCode.PRODUCT_STATUS_INVALID);
        }
        if (Product.STATUS_LISTED.equals(targetStatus)) {
            assertCanList(p.getMerchantId(), id);
        }
        p.setStatus(targetStatus);
        productMapper.updateById(p);
        knowledgeBase.rebuild();
    }

    // ---------- 删除(逻辑删除) ----------
    public void delete(Long id) {
        Product p = mustGetOwned(id);
        productMapper.deleteById(p.getId());
        knowledgeBase.rebuild();
    }

    // ---------- 商品管理列表(按状态过滤, 分页) ----------
    public PageResult<ProductListItemVO> page(long current, long size, String status) {
        Long merchantId = SecurityContext.currentMerchantId();
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(status != null && !status.isBlank(), Product::getStatus, status)
                .orderByDesc(Product::getCreatedTime);
        Page<Product> result = productMapper.selectPage(page, qw);
        List<ProductListItemVO> records = result.getRecords().stream().map(this::toListVO).toList();
        return PageResult.of(result, records);
    }

    // ---------- 商家自己看详情(任意状态) ----------
    public ProductDetailVO detail(Long id) {
        Product p = mustGetOwned(id);
        return toDetailVO(p);
    }

    // ---------- 游客看详情(仅已上架) ----------
    public ProductDetailVO homeDetail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || !Product.STATUS_LISTED.equals(p.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return toDetailVO(p);
    }

    // ==================== 内部工具 ====================

    /** 取当前商家名下的商品, 不存在或不归属则抛错 */
    private Product mustGetOwned(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || !p.getMerchantId().equals(SecurityContext.currentMerchantId())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return p;
    }

    /** 校验上架额度: 当前已上架数(不含自己)必须小于上限 */
    private void assertCanList(Long merchantId, Long excludeProductId) {
        Merchant m = merchantService.getCurrent();
        int limit = merchantService.productLimitOf(merchantService.effectiveTier(m));
        Long listed = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, Product.STATUS_LISTED)
                .ne(excludeProductId != null, Product::getId, excludeProductId));
        if (listed >= limit) {
            throw new BusinessException(ResultCode.PRODUCT_LIMIT_REACHED);
        }
    }

    private void applyFields(Product p, ProductSaveRequest req) {
        p.setTitle(req.getTitle());
        p.setBrief(req.getBrief());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setTags(req.getTags());
        p.setImages(req.getImages());
        if (req.getAiGenerated() != null) {
            p.setAiGenerated(req.getAiGenerated());
        }
    }

    private ProductListItemVO toListVO(Product p) {
        ProductListItemVO vo = new ProductListItemVO();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setPrice(p.getPrice());
        vo.setStatus(p.getStatus());
        vo.setCreatedTime(p.getCreatedTime());
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            vo.setCoverImage(p.getImages().get(0));
        }
        return vo;
    }

    private ProductDetailVO toDetailVO(Product p) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(p.getId());
        vo.setMerchantId(p.getMerchantId());
        vo.setTitle(p.getTitle());
        vo.setBrief(p.getBrief());
        vo.setDescription(p.getDescription());
        vo.setPrice(p.getPrice());
        vo.setTags(p.getTags());
        vo.setImages(p.getImages());
        vo.setStatus(p.getStatus());
        vo.setAiGenerated(p.getAiGenerated());
        vo.setCreatedTime(p.getCreatedTime());
        return vo;
    }
}
