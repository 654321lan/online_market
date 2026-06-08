package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.ProductSpecValue;
import com.mall.mapper.ProductSpecValueMapper;
import com.mall.service.ProductSpecValueService;
import org.springframework.stereotype.Service;

@Service
public class ProductSpecValueServiceImpl extends ServiceImpl<ProductSpecValueMapper, ProductSpecValue> implements ProductSpecValueService {
}
