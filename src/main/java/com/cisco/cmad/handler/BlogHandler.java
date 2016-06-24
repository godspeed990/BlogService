package com.cisco.cmad.handler;



import java.util.List;
import java.util.Optional;
import com.cisco.cmad.model.*;
import com.cisco.cmad.Service.BlogService;
import java.util.Base64;


import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
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
   System.out.println(authorization);
   String userName = new String(Base64.getDecoder().decode(authorization.substring(authorization.lastIndexOf(" ")+1,authorization.indexOf(":"))));
   String password = new String(Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)));
   JsonObject req = new JsonObject().put("userName",userName).put("password",password);
   logger.error("Request send to :"+req.encode());
   eventBus.send("com.cisco.cmad.user.authenticate",req,message->{
	if (message.succeeded()){
		JsonObject user = (JsonObject) message.result().body();
		if (user.containsKey("userId")){
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
				   System.out.println(res.result());
			   rc.response().setStatusCode(200);
			   rc.response().end(res.result().toString());
			   }
			   else {
				   rc.response().setStatusCode(400);
				   rc.response().end();
			   }
			}   
		);
		}else {
			rc.response().setStatusCode(404).end();
			logger.error("Auth failed"+message.result());
		}
	}
	else {
		rc.response().setStatusCode(404);
		logger.error("Auth failed"+message.result());
	}
   }); 
}
public void setEventBus(EventBus eb){
   eventBus =eb;
}

public void storeBlog(RoutingContext rc) {
  String jSonString = rc.getBodyAsString(); //get JSON body as String
  String authorization = rc.request().getHeader("Authorization");
  //JsonObject jSonString = rc.getBodyAsJson();
  String userName = new String(Base64.getDecoder().decode(authorization.substring(authorization.lastIndexOf(" ")+1,authorization.indexOf(":"))));
  String password = new String(Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)));
  JsonObject req = new JsonObject().put("userName",userName).put("password",password);
  logger.debug("Request send to :"+req.encode());
  if (logger.isDebugEnabled())
     logger.debug("JSON String from POST " + jSonString);

  BlogEntry blog = Json.decodeValue(jSonString, BlogEntry.class);
/*BlogEntry blog =new BlogEntry(Optional.empty(),rc.getBodyAsJson().getValue("content").toString(),
		rc.getBodyAsJson().getValue("title").toString(),rc.getBodyAsJson().getValue("tags").toString(),
				Optional.empty(),Optional.empty()
		);*/
  try{
  eventBus.send("com.cisco.cmad.user.authenticate",req,message->{
	  logger.debug("Message body"+message.result().body());
	  if (message.succeeded()){
		  JsonObject body = (JsonObject) message.result().body();
		  	blog.setFirstName(body.getString("firstName"));
		  	blog.setFirstName(body.getString("lastName"));
		  	blogService.storeBlog(blog);
			if (logger.isDebugEnabled())
				logger.debug("RegistrationDTO object after json Decode : " + blog);
			rc.response().setStatusCode(201).end();
			}
	  else {
		  rc.response().setStatusCode(400).end();
			logger.debug("Message failed " + message.cause()+message.result());
	  }

  });
  }
  catch (Exception e){
	  logger.debug("Error"+e.getStackTrace());
  }
 }
	        

	    
public void submitComment(RoutingContext rc) {
  String jSonString = rc.getBodyAsString(); 
  String blogId = rc.request().getParam("blogId");

  if (logger.isDebugEnabled())
    logger.debug("JSON String from POST " + jSonString + " Blog Id :" + blogId);
    Comment comment = Json.decodeValue(jSonString, Comment.class);
    String authorization = rc.request().getHeader("Authorization");
    comment.setDate();
    String userName = new String(Base64.getDecoder().decode(authorization.substring(authorization.lastIndexOf(" ")+1,authorization.indexOf(":"))));
    String password = new String(Base64.getDecoder().decode(authorization.substring(authorization.indexOf(":")+1)));
    JsonObject req = new JsonObject().put("userName",userName).put("password",password);
    logger.error("Request send to :"+req.encode());
  try{
	 eventBus.send("com.cisco.cmad.user.authenticate",req,message->{
		 if (message.succeeded()){
			JsonObject user = (JsonObject) message.result().body();
				if (user.containsKey("userId")){
					comment.setFirstName(user.getString("firstName"));
					comment.setLastName(user.getString("lastName"));
					blogService.updateBlogWithComments(blogId, comment);
					rc.response().setStatusCode(201).end();
					logger.debug("Comment added");
				}
				else {
					rc.response().setStatusCode(400).end();
				}
		 }
		 else {
			 rc.response().setStatusCode(404).end();
		 } 
	 });

	   }
  catch (Exception e){
	  logger.error("Logging error in updating comments"+e.getStackTrace());
  }
	   
}
	        }
