package com.java.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.java.dto.Result;
import com.java.entity.Blog;
import com.java.entity.User;
import com.java.mapper.BlogMapper;
import com.java.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.service.IUserService;
import com.java.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务实现类
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IBlogService blogService;

    @Autowired
    private IUserService userService;

    /**
     * 根据点赞数查询笔记列表
     * @param current
     * @return
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(this::queryBlogUser);
        return Result.ok(records);
    }

    /**
     * 根据id查询笔记详情
     * @param id
     * @return
     */
    @Override
    public Result queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在");
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        return Result.ok(blog);
    }

    /**
     * 根据笔记查询用户
     * @param blog
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
