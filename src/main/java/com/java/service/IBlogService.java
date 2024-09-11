package com.java.service;

import com.java.dto.Result;
import com.java.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 */
public interface IBlogService extends IService<Blog> {

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);
}
