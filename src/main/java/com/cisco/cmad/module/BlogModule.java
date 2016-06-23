package com.cisco.cmad.module;

import com.cisco.cmad.dao.*;
import com.cisco.cmad.Service.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import net.jmob.guice.conf.core.BindConfig;
import net.jmob.guice.conf.core.ConfigurationModule;
import net.jmob.guice.conf.core.InjectConfig;

import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
@BindConfig(value = "conf/BlogServices")
@Singleton
public class BlogModule extends AbstractModule {

    @InjectConfig Optional<Integer> dbPort;
    @InjectConfig Optional<String> dbHost;
    @InjectConfig Optional<String> dbName;
	@Override
    protected void configure() {
		 install(ConfigurationModule.create());
        bind(BlogService.class);
        bind(BlogDAO.class).in(Singleton.class);
    }

    @Provides @Singleton
    public Datastore getDataStore() {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + dbHost.orElse("localhost") + ":" + dbPort.orElse(27017)));
        //create a new morphia instance
        Datastore datastore = new Morphia()
                .mapPackage("com.cisco.cmad.model")
                .createDatastore(mongoClient, dbName.orElse("blog_db"));
        datastore.ensureIndexes();
        return datastore;
    }

}
