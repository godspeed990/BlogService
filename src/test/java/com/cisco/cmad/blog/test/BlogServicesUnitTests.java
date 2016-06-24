package com.cisco.cmad.blog.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.cisco.cmad.Service.BlogService;
import com.cisco.cmad.dao.BlogDAO;
import com.cisco.cmad.model.*;

import junit.framework.Assert;

import org.mongodb.morphia.Key;
import static java.lang.System.out;
@RunWith(MockitoJUnitRunner.class)
public class BlogServicesUnitTests {
    @Mock private BlogDAO blogDaoMock;
    @InjectMocks private BlogService blogService = new BlogService(blogDaoMock);
    @Mock private Key<BlogEntry> blogKey;
    private BlogEntry getBlog(String title, Optional<String> tag) {
        BlogEntry blog = Mockito.mock(BlogEntry.class);
        Mockito.when(blog.getId()).thenReturn(ObjectId.get().toHexString());
        Mockito.when(blog.getTitle()).thenReturn(title);
//        ArrayList<String> returnTags = new ArrayList<String>();
 //       returnTags.add(tag.orElse(""));
 //       Mockito.when(blog.getTags()).thenReturn(returnTags);
        Mockito.when(blog.getTags()).thenReturn(tag.orElse(""));
        return blog;
    }
    @SuppressWarnings("deprecation")
	@Test
    public void testBlogList() {

        List<BlogEntry> blogList = new ArrayList<BlogEntry>();
        blogList.add(getBlog("Dummy Title", Optional.empty()));
        blogList.add(getBlog("Dummy Title #2", Optional.empty()));

        blogList.forEach(b -> out.println("Blog ::" + b.getTitle()));

        BDDMockito.given(blogDaoMock.getBlogs(Optional.empty())).willReturn(blogList);

        List<BlogEntry> allBlogList = blogService.getBlogs(Optional.empty());

        allBlogList.forEach(b -> out.println("AllBlogList ::" + b));

        Assert.assertEquals("Blog size should match ", 2, allBlogList.size());
        Assert.assertEquals("Blog titles should match ", allBlogList.get(0).getTitle(), blogList.get(0).getTitle());
    }
    @Test
    public void testBlogListUsingKeyword() {
        List<BlogEntry> blogList = new ArrayList<>();
        blogList.add(getBlog("Dummy Title #2", Optional.ofNullable("tag1, tag2")));
        blogList.add(getBlog("Title #2", Optional.ofNullable("tag3, tag4")));

        BDDMockito.given(blogDaoMock.getBlogs(Optional.ofNullable("tag1"))).willReturn(blogList.subList(0,1));

        List<BlogEntry> taggedBlogList = blogService.getBlogs(Optional.ofNullable("tag1"));

        Assert.assertEquals("Blog size should match ", 1, taggedBlogList.size());
        Assert.assertEquals("Blog titles should match ", taggedBlogList.get(0).getTitle(), blogList.get(0).getTitle());
    }

    /**
     * Try to fetch blogs using a tag that does not exist.
     */
    @Test
    public void testBlogListUsingMissingKeyword() {
        List<BlogEntry> blogList = new ArrayList<>();

        blogList.add(getBlog("Dummy Title", Optional.ofNullable("tag1, tag2")));

        BDDMockito.given(blogDaoMock.getBlogs(Optional.ofNullable("tag1"))).willReturn(blogList);
        BDDMockito.given(blogDaoMock.getBlogs(Optional.ofNullable("tag2"))).willReturn(blogList);

        List<BlogEntry> taggedBlogList = blogService.getBlogs(Optional.ofNullable("tag3"));

        Assert.assertEquals("Blog size should match ", 0, taggedBlogList.size());
    }

    /**
     * Test that on saving a Blog, an identifier representing the blog is
     * returned
     */
    @Test
    public void testStoreBlog() {

        BlogEntry blog = getBlog("Test Title", Optional.ofNullable("tag1, tag2"));

        String objId = (new ObjectId()).toHexString();

        BDDMockito.given(blogDaoMock.save(blog)).willReturn(blogKey);
        BDDMockito.given(blogKey.getId()).willReturn(objId);

        String retVal = blogService.storeBlog(blog);

        Assert.assertEquals("ObjectId should be returned when Blog is saved ", retVal, objId);

    }

    /**
     * Test to ensure that the blog that is saved can be retrieved back post saving
     */
    @Test
    public void testStoreAndRetrieveBlog() {

        List<BlogEntry> blogList = new ArrayList<>();
        BlogEntry blog = getBlog("Dummy Title", Optional.ofNullable("tag1, tag2"));
        blogList.add(blog);

        String objId = (new ObjectId()).toHexString();
        BDDMockito.given(blogDaoMock.save(blog)).willReturn(blogKey);
        BDDMockito.given(blogKey.getId()).willReturn(objId);
        BDDMockito.given(blogDaoMock.getBlogs(Optional.empty())).willReturn(blogList);

        blogService.storeBlog(blog);

        List<BlogEntry> allBlogList = blogService.getBlogs(Optional.empty());

        Assert.assertEquals("Blog size should match ", 1, allBlogList.size());
    }

    @Test
    public void testUpdateBlogWithComments() {

        String firstName = "Krishnan";
        String lastName = "Y";

        ArrayList<Comment> commentList = new ArrayList<>();

        Comment comment = Mockito.mock(Comment.class);
        Mockito.when(comment.getContent()).thenReturn("Comment #1");
        commentList.add(comment);

        List<BlogEntry> blogList = new ArrayList<>();
        BlogEntry blog = getBlog("Photography", Optional.ofNullable("macro, dslr"));

        Mockito.when(blog.getComment()).thenReturn(commentList);
        blogList.add(blog);

        BDDMockito.given(blogDaoMock.getBlogs(Optional.empty())).willReturn(blogList);

        List<BlogEntry> allBlogList = blogService.getBlogs(Optional.empty());

        Assert.assertEquals("Comment saved should be same when retrieved ", allBlogList.get(0).getComment(), blogList.get(0).getComment());
        Assert.assertEquals("Blog size should match ", 1, allBlogList.size());

    }
}
