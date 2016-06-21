package com.cisco.cmad.dao;
import com.cisco.cmad.model.*;
import com.google.inject.Inject;
import com.mongodb.MongoClient;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

public class BlogDAO extends BasicDAO<BlogEntry, ObjectId>{

	@Inject
    public BlogDAO(Datastore ds) {
        super(ds);
        
    }
    public List<BlogEntry> getBlogs(Optional<String> searchKeyword) {
        return  searchKeyword.map( tag -> {
            return createQuery().field("tags").contains(tag).order("-date").asList();
        }).orElseGet(() -> {
            return createQuery().order("-date").asList();
        });
    }

    public void submitComments(String blogId,Comment comment) {
        UpdateOperations<BlogEntry> ops;        
        Query<BlogEntry> query = createQuery().field("_id").equal(blogId);
        update(query, createUpdateOperations().add("comments", comment));
    }
    
    public String storeBlog(BlogEntry blog) {
        return super.save(blog).getId().toString();   
    }
}
