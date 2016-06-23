package com.cisco.cmad.blog.test;



import org.junit.*;
import org.junit.runner.RunWith;

import com.cisco.cmad.verticle.BlogServiceVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;


import java.net.ServerSocket;
@RunWith(VertxUnitRunner.class)
public class BlogVerticleDeployTest {

	private int port;
	private Vertx vertx;

    @Before
	public void before(TestContext context) throws Exception {
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		
    	vertx = Vertx.vertx();
   		DeploymentOptions depoptions = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
    	vertx.deployVerticle(BlogServiceVerticle.class.getName(), depoptions, context.asyncAssertSuccess());


	}

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
	@Test
	public void testHomePageFetch(TestContext context) {
		
		final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/", resp -> {

            context.assertEquals(200, resp.statusCode(), "Status code should be 200 ");
            resp.bodyHandler(body -> {
            	context.assertTrue(true,"Body reached");
                async.complete();
        });
        });
	}
}
