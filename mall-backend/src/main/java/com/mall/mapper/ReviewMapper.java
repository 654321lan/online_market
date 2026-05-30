package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}

