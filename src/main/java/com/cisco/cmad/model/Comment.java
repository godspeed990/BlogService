package com.cisco.cmad.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import io.vertx.core.json.JsonObject;

@Embedded
public class Comment {

		private ObjectId id;
		private String content;
//		private BlogUsers user;
		private Date date ;
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss zs");
		@Embedded
		private List<Comment> comment = new ArrayList<Comment>();
		private ObjectId userId ;
		
		 public Comment(ObjectId id, String content, ObjectId userId) {
			super();
			this.id = id;
			this.content = content;
			this.userId = userId;
		}
		 
		 public Comment( String content, ObjectId userId) {
			super();
			this.content = content;
			this.userId = userId;
		} 
		 
			public JsonObject toJson(){
				JsonObject json = new JsonObject();
			    if (id != null ) {
				      json.put("_id", id.toHexString());
				    }
			    json.put("content", this.content)
			    .put("user",userId.toHexString())
			    .put("date",dateFormat.format(this.date))	    
			    ;
				return json;
		}
			public Comment(JsonObject js){
				if (js.containsKey("_id")) this.id = new ObjectId(js.getString("_id"));
				if (js.containsKey("content")) this.content = js.getString("content");
				if (js.containsKey("user")) this.userId = new ObjectId(js.getString("user"));
			    if (js.containsKey("date")) this.date = new Date(js.getString("date"));
				}
		public String getUserId() {
			return userId.toHexString();
		}
		public void setUserId(ObjectId userId) {
		 this.userId = userId;
		}
		@PrePersist void prePersist() {date= new Date();}

	    public String getId() {
	        return (id != null) ? id.toHexString() : "";
	    }

	    public void setId(String id) {
	        if (id != null)
	            this.id = new ObjectId(id);
	    }

		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public List<Comment> getComment() {
			return comment;
		}
		public void addComment(Comment comment) {
			if (comment !=null)
				this.comment.add(comment);
		}
		public void addComment(List<Comment> comment) {
			this.comment.addAll(comment);
		}
		public Date getDate() {
			return date;
		}

}
