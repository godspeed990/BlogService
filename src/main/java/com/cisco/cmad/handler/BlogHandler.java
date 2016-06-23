package com.cisco.cmad.handler;



import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import com.cisco.cmad.model.*;
import com.cisco.cmad.Service.BlogService;
import java.util.Base64;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import com.google.inject.Inject;

public class BlogHandler {
	

	  Logger logger = LoggerFactory.getLogger(BlogHandler.class);
	    @Inject BlogService blogService;
	    
		private EventBus eventBus;

public void getBlogs(RoutingContext rc) {
   String queryParam = rc.request().getParam("tag");
   if (logger.isDebugEnabled()) {
      logger.debug("Tag search ? , tag :" + queryParam);
   }
   String authorization = rc.request().getHeader("Authorization"); 
   String userName = Base64.getDecoder().decode(authorization.substring(0,authorization.indexOf(":"))).toString();
   String password = Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)).toString();
   
   try{	        
	   eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password",password),resp->{
		   rc.vertx().executeBlocking(future -> {
		   try {
			   List<BlogEntry> blogList;
	          if (queryParam != null && queryParam.trim().length() > 0) {
	           blogList = blogService.getBlogs(Optional.ofNullable(queryParam));	            
	          } else {
	                    //get all blogs
	            blogList = blogService.getBlogs(Optional.empty());
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
    				   JsonObject JsonObj = (JsonObject) respObj;
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
	   rc.response().setStatusCode(401);
	   rc.response().end();
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
  String authorization = rc.request().getHeader("Authorization");
  
  String userName = Base64.getDecoder().decode(authorization.substring(0,authorization.indexOf(":"))).toString();
  String password = Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)).toString();

  if (logger.isDebugEnabled())
     logger.debug("JSON String from POST " + jSonString);

  BlogEntry blog = Json.decodeValue(jSonString, BlogEntry.class);

  if (logger.isDebugEnabled())
     logger.debug("RegistrationDTO object after json Decode : " + blog);
try{	        
  eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password",password),response->{
	  if (response.succeeded()){
		  JsonObject resp = (JsonObject) response.result().body();
		  if (resp.getString("userId")!=null){
			  blog.setUserId(new ObjectId(resp.getString("userId")));
			  blogService.storeBlog(blog);
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
    String authorization = rc.request().getHeader("Authorization");
    
    String userName = Base64.getDecoder().decode(authorization.substring(0,authorization.indexOf(":"))).toString();
    String password = Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)).toString();

  if (logger.isDebugEnabled())
    logger.debug("Comment object : " + comment);
  	eventBus.send("com.cisco.cmad.user.authenticate",new JsonObject().put("userName",userName).put("password",password),response->{
		if (response.succeeded()){
			JsonObject resp = (JsonObject) response.result().body();
			if (resp.getString("userId")!=null){
				comment.setUserId(new ObjectId(resp.getString("userId")));
				blogService.updateBlogWithComments(blogId, comment);
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
