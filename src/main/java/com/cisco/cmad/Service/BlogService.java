package com.cisco.cmad.Service;

import com.cisco.cmad.dao.BlogDAO;
import com.cisco.cmad.model.BlogEntry;
import com.cisco.cmad.model.Comment;
import com.google.inject.Inject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BlogService  {

    Logger logger = LoggerFactory.getLogger(BlogService.class);

    BlogDAO blogDao;

    @Inject
    public BlogService(BlogDAO blogdao) {
        if (logger.isDebugEnabled())
            logger.debug("Created BlogServiceImpl..");
        this.blogDao = blogdao;
    }

    public List<BlogEntry> getBlogs(Optional<String> searchKeyword) {
        return blogDao.getBlogs(searchKeyword);
    }

    public String storeBlog(BlogEntry blog) {
        return blogDao.save(blog).getId().toString();
    }

    public void updateBlogWithComments(String blogId, Comment comment) {
        blogDao.submitComments(blogId, comment);
    }
}
