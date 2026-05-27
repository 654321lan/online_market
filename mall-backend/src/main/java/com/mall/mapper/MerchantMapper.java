package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家Mapper
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}

