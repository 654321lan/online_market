package com.mall.controller.admin;

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
 * 管理员端 - 公告管理接口
 */
@RestController
@RequestMapping("/api/admin/notice")
@Tag(name = "管理员端-公告管理")
public class AdminNoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @GetMapping("/list")
    @Operation(summary = "公告列表")
    public Result<IPage<Notice>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Notice> pageParam = new Page<>(page, size);
        IPage<Notice> result = noticeMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime));
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加公告")
    public Result<String> addNotice(@RequestBody Notice notice) {
        notice.setStatus(1);
        noticeMapper.insert(notice);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新公告")
    public Result<String> updateNotice(@RequestBody Notice notice) {
        noticeMapper.updateById(notice);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除公告")
    public Result<String> deleteNotice(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "公告上下架")
    public Result<String> updateNoticeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        notice.setStatus(status);
        noticeMapper.updateById(notice);
        return Result.success(status == 1 ? "发布成功" : "下架成功");
    }
}

