package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价点赞Mapper
 */
@Mapper
public interface ReviewLikeMapper extends BaseMapper<ReviewLike> {
}