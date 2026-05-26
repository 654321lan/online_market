package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Notice;
import com.mall.mapper.NoticeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 公告接口
 */
@RestController
@RequestMapping("/api/notice")
@Tag(name = "用户端-公告接口")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @GetMapping("/list")
    @Operation(summary = "公告列表")
    public Result<IPage<Notice>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Notice> pageParam = new Page<>(page, size);
        IPage<Notice> result = noticeMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByAsc(Notice::getSort)
                        .orderByDesc(Notice::getCreateTime));
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "公告详情")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        Notice notice = noticeMapper.selectById(id);
        return Result.success(notice);
    }
}

