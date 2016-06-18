package com.cisco.cmad.verticle;


import com.mongodb.MongoClient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import com.cisco.cmad.handler.BlogHandler;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class BlogServiceVerticle  extends AbstractVerticle{
	 Logger logger = LoggerFactory.getLogger(BlogServiceVerticle.class);
	 private static final String TABLE_PREFIX = "Auth_";
	 private static MongoClient mongoClient;
	private static EventBus eventBus;

	@Inject BlogHandler blogHandler;
	@Override
    public void start(Future<Void> startFuture) throws Exception {
		   logger.info("authVerticle started " + Thread.currentThread().getId());
		   eventBus = getVertx().eventBus();
	        //Router object is responsible for dispatching the HTTP requests to the right handler
	        Router router = Router.router(vertx);
	        setRoutes(router);

	        blogHandler.setEventBus(eventBus);;
	        
	        //Enable SSL - currently using self signed certs
	        HttpServerOptions httpOpts = new HttpServerOptions();
	        httpOpts.setKeyStoreOptions(new JksOptions().setPath("keystore.jks").setPassword("cmad@cisco"));
	        httpOpts.setSsl(true);
	        try {
				//setUp("blog_db");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        //Start Server
	        HttpServer server = vertx.createHttpServer(httpOpts);
	        server.requestHandler(router::accept)
	                .listen(
	                        config().getInteger("https.port", 8443), result -> {
	                            if (result.succeeded()) {
	                                startFuture.complete();
	                            } else {
	                                startFuture.fail(result.cause());
	                            }
	                        }
	                );



	} 
	

    
    
    private void setRoutes(Router router){
        router.route().handler(ctx -> {
            ctx.response()
                    .putHeader("Cache-Control", "no-store, no-cache")
                    .putHeader("X-Content-Type-Options", "nosniff")
                    .putHeader("X-Download-Options", "noopen")
                    .putHeader("X-XSS-Protection", "1; mode=block")
                    .putHeader("Access-Control-Allow-Origin", "*")
                    .putHeader("Access-Control-Allow-Methods", "GET, POST")
                    .putHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type, Authorization")
                    .putHeader("X-FRAME-OPTIONS", "DENY");

            ctx.next();
        });

        //GET Operations

		router.get("/Services/rest/blogs").handler(blogHandler::getBlogs);
		router.post("/Services/rest/blogs").handler(blogHandler::storeBlog);
		router.post("/Services/rest/blogs/:blogId/comments").handler(blogHandler::submitComment);


        router.route().failureHandler(rc->{
	        int failCode = rc.statusCode();
	        logger.error("In FailureHandler, Status code :" + failCode);
	        HttpServerResponse response = rc.response();
	        response.setStatusCode(failCode).end();
        });
    	
    }


}
