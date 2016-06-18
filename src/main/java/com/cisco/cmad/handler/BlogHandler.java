package com.cisco.cmad.handler;



import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import com.cisco.cmad.model.*;
import com.cisco.cmad.dao.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import com.google.inject.Inject;

public class BlogHandler {
	
	  @Inject BlogDAO blogDAO;

	  Logger logger = LoggerFactory.getLogger(BlogHandler.class);
	  BlogDAO blogDao;

		private EventBus eventBus;

public void getBlogs(RoutingContext rc) {
   String queryParam = rc.request().getParam("tag");
   if (logger.isDebugEnabled()) {
      logger.debug("Tag search ? , tag :" + queryParam);
   }
   String userName = "";
   String password = "";
   
   try{	        
	   eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password","abc123"),resp->{
		   rc.vertx().executeBlocking(future -> {
		   try {
			   List<BlogEntry> blogList;
	          if (queryParam != null && queryParam.trim().length() > 0) {
	           blogList = blogDao.getBlogs(Optional.ofNullable(queryParam));	            
	          } else {
	                    //get all blogs
	            blogList = blogDao.getBlogs(Optional.empty());
	          }
	          future.complete(Json.encodePrettily(blogList));
		   } catch (Exception ex) {
               logger.error("Exception while trying to fetch blogs " + queryParam, ex);
               future.fail(ex);
           }
       }, res -> {           
    	   if (res.succeeded()) {
    	   Object obj = res.result();
    	   List<BlogEntry> blogs= (List<BlogEntry>) obj;
    	   for (int i=0;i<blogs.size();i++){
    		   JsonObject temp = blogs.get(i).toJson();
    		   eventBus.send("com.cisco.cmad.user.info",new JsonObject().put("userId",blogs.get(i).getUserId()),response->{
    			   if (response.succeeded()){
    				   Object respObj = response.result();
    				   JsonObject JsonObj = (JsonObject) obj;
    				   rc.response().write(Json.encode(temp.put("userName",JsonObj.getString("userName"))
    						   .put("firstName",JsonObj.getString("firstName"))
    						   .put("lastName",JsonObj.getString("lastName")))   						   
    						   );
    			   }
    		   });
    	   }
    	   rc.response().setStatusCode(200);
    	   rc.response().end();
    	   }
    	   else {
    		   rc.response().setStatusCode(400);
    		   rc.response().end();
    	   }
    	   
   
	        }
	        );
   });
  }
   catch (Exception e){
	   logger.error("Exception while trying to authenticate"+e);
   }
}
public void setEventBus(EventBus eb){
   eventBus =eb;
}

public JsonArray getUserInfo(List<BlogEntry> blogs){
	
	return new JsonArray();
}
public void storeBlog(RoutingContext rc) {
  String jSonString = rc.getBodyAsString(); //get JSON body as String
  
  String userName = "";
  String password = "";
  if (logger.isDebugEnabled())
     logger.debug("JSON String from POST " + jSonString);

  BlogEntry blog = Json.decodeValue(jSonString, BlogEntry.class);

  if (logger.isDebugEnabled())
     logger.debug("RegistrationDTO object after json Decode : " + blog);
try{	        
  eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password","abc123"),response->{
	  if (response.succeeded()){
		  JsonObject resp = (JsonObject) response.result().body();
		  if (resp.getString("userId")!=null){
			  blog.setUserId(new ObjectId(resp.getString("userId")));
			  blogDAO.save(blog);
              if (logger.isDebugEnabled())
                  logger.debug("Blog stored successfully");
		  }
			else {
				rc.response().setStatusCode(401).end();
			}
	  }
	  else {
	  }
  });
  }
catch (Exception e){		  
	logger.error("Error occurred while trying to save blog with " + blog.getId(), e);
	rc.response().setStatusCode(401).end();
}
	        
	        
	        //   BlogEntry blogE = new BlogEntry(Optional.ofNullable(null),rc.getBodyAsJson().getValue("content").toString(),rc.getBodyAsJson().getValue("title").toString(),rc.getBodyAsJson().getValue("tags").toString(),userid,Optional.ofNullable(null));
	        
	   
	   }

	    
public void submitComment(RoutingContext rc) {
  String jSonString = rc.getBodyAsString(); 
  String blogId = rc.request().getParam("blogId");
  if (logger.isDebugEnabled())
    logger.debug("JSON String from POST " + jSonString + " Blog Id :" + blogId);
    Comment comment = Json.decodeValue(jSonString, Comment.class);
  String userName = "";
  if (logger.isDebugEnabled())
    logger.debug("Comment object : " + comment);
  	eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password","abc123"),response->{
		if (response.succeeded()){
			JsonObject resp = (JsonObject) response.result().body();
			if (resp.getString("userId")!=null){
				comment.setUserId(new ObjectId(resp.getString("userId")));
				blogDAO.submitComments(blogId, comment);
				if (logger.isDebugEnabled())
                logger.debug("Comment updated in blog successfully");
				}
			else {
				rc.response().setStatusCode(401).end();
			}
		}
		else {
			rc.response().setStatusCode(500).end();
		}
  		
  	});     
	   
	   }
	   

	        }
