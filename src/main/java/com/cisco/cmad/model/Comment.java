package com.cisco.cmad.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Entity("comment")
@Indexes({
    @Index(fields = @Field("userId"))
})
public class Comment {

		@Id
		private ObjectId id;
		private String content;
//		private BlogUsers user;
		private Date date ;
		private String type;
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss zs");
		@Reference("blogs")
		private ArrayList<Comment> comment= new ArrayList<Comment>();
		private ObjectId userId ;
		
		 public Comment(ObjectId id, String content, ObjectId userId) {
			super();
			this.id = id;
			this.content = content;
			this.type = "comment";
			this.userId = userId;
		}
		 
		 public Comment( String content, ObjectId userId) {
			super();
			this.content = content;
			this.type = "comment";
			this.userId = userId;
		} 
		 
			public JsonObject toJson(){
				JsonObject json = new JsonObject();
			    if (id != null ) {
				      json.put("_id", id.toHexString());
				    }
			    json.put("content", this.content)
			    .put("user",userId.toHexString())
			    .put("type", this.type)
			    .put("date",dateFormat.format(this.date))	    
			    ;
				return json;
		}
			public Comment(JsonObject js){
				this.id = new ObjectId(js.getString("_id"));
				this.content = js.getString("content");
				this.userId = new ObjectId(js.getString("user"));
				this.type = js.getString("type");
				this.date = new Date(js.getString("date"));
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
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public ArrayList<Comment> getComment() {
			return comment;
		}
		public void addComment(Comment comment) {
			this.comment.add(comment);
		}
		public void addComment(ArrayList<Comment> comment) {
			this.comment.addAll(comment);
		}
		public Date getDate() {
			return date;
		}

}
