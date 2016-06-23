package com.cisco.cmad.verticle;



import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

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
	        
	        //Enable SSL - currently using self signed certs
	        HttpServerOptions httpOpts = new HttpServerOptions();
	        httpOpts.setKeyStoreOptions(new JksOptions().setPath("mykeystore.jks").setPassword("cmad.cisco"));
	        httpOpts.setSsl(true);
	        
	        //Start Server
	        HttpServer server = vertx.createHttpServer(httpOpts);
	        
	        server.requestHandler(router::accept)
	                .listen(
	                        config().getInteger("https.port",8100), result -> {
	                            if (result.succeeded()) {
	                            if	(logger.isDebugEnabled())
	                            		logger.debug("Verticle up at:"+config().getInteger("https.port",8100));
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

		router.route().handler(StaticHandler.create().setCachingEnabled(true).setMaxAgeSeconds(60)::handle);
//        router.route().failureHandler(rc->{
//	        int failCode = rc.statusCode();
//	        logger.error("In FailureHandler, Status code :" + failCode);
//	        HttpServerResponse response = rc.response();
//	        response.setStatusCode(failCode).end();
//        });
    	
    }

	public static void main(String args[]){
		
		ClusterManager mgr = new HazelcastClusterManager();
		VertxOptions options = new VertxOptions().setWorkerPoolSize(Integer.parseInt(args[1])).setClusterManager(mgr).setClusterHost("192.168.1.101");
		Vertx.clusteredVertx(options, res -> {
		  if (res.succeeded()) {
		    Vertx vertx = res.result();
		    DeploymentOptions depOptions = new DeploymentOptions().setConfig(new JsonObject().put("https.port", Integer.parseInt(args[0])));
		    vertx.deployVerticle(BlogServiceVerticle.class.getName(),depOptions);
		    eventBus = vertx.eventBus();
		  } else {
		    logger.error("Verticle GetServices Deployment on port"+args[0]+"failed");
		  }
		});
	}


}
