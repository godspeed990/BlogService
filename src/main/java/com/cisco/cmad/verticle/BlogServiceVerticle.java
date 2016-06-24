package com.cisco.cmad.verticle;



import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.BodyHandler;
import com.cisco.cmad.handler.BlogHandler;
import com.cisco.cmad.module.BlogModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class BlogServiceVerticle  extends AbstractVerticle{
	 static Logger logger = LoggerFactory.getLogger(BlogServiceVerticle.class);
	private static EventBus eventBus;

	@Inject BlogHandler blogHandler;
	@Override
    public void start(Future<Void> startFuture) throws Exception {
		   logger.info("authVerticle started " + Thread.currentThread().getId());
		   eventBus = getVertx().eventBus();
	        Injector injector = Guice.createInjector(new BlogModule());
	        BlogModule module = injector.getInstance(BlogModule.class);
	        Guice.createInjector(module).injectMembers(this);
	        //Router object is responsible for dispatching the HTTP requests to the right handler
	        Router router = Router.router(vertx);


	        setRoutes(router);

	        blogHandler.setEventBus(eventBus);;
	        
	        //Enable SSL - 
	        HttpServerOptions httpOpts = new HttpServerOptions();
//	        httpOpts.setKeyStoreOptions(new JksOptions().setPath("mykeystore.jks").setPassword("cmad.cisco"));
//	        httpOpts.setSsl(true);
	        
	        //Start Server
	        HttpServer server = vertx.createHttpServer(httpOpts);
	        int port = 8100;
	        try{
		         port = Integer.parseInt(System.getenv("LISTEN_PORT"));
		        }
		        catch (Exception e){
		        	logger.error("Failed to get ENV PORT");
		        }
	        server.requestHandler(router::accept)
            .listen(config().getInteger("http.port",port), result -> {
                        if (result.succeeded()) {
                        	logger.error("Get Services Verticles running over");
                            startFuture.complete();
                        } else {
                        	logger.error("Get Services Verticles failed to startover");
                            startFuture.fail(result.cause());
                        }
                    }
            );


	} 

    
    private void setRoutes(Router router){
        router.route().handler(BodyHandler.create());
        //GET Operations
 
		router.get("/Services/rest/blogs").handler(blogHandler::getBlogs);
		router.post("/Services/rest/blogs").handler(blogHandler::storeBlog);
		router.post("/Services/rest/blogs/:blogId/comments").handler(blogHandler::submitComment);

		router.route().handler(StaticHandler.create().setCachingEnabled(true).setMaxAgeSeconds(60)::handle);

    	
    }

}
