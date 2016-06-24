package com.cisco.cmad.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Entity("blogs")
@Indexes({
    @Index(fields = @Field("tags")),
    @Index(fields = @Field("userId"))
})

public class BlogEntry {

		@Id
		private ObjectId id;
	    private String content;
		private String title;
		private ArrayList<String> tags = new ArrayList<String>();
//		private BlogUsers user;
		private Date date ;
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
		@Embedded
		private ArrayList<Comment> comment= new ArrayList<Comment>();
		private ObjectId userId ;
		
		 public BlogEntry(Optional<ObjectId> id, String content, String title, String tags, 
				 Optional<ObjectId> userId,Optional<ArrayList<Comment>> comment) {
			
			if (id.isPresent()) this.id = id.get();
			this.content = content;
			this.title = title;
			List<String> temp = Arrays.asList(tags.split(","));
			this.tags.addAll(temp);
			if (id.isPresent()) this.userId = userId.get();
			if (comment.isPresent()) this.comment = comment.get();
		}
		
			@SuppressWarnings("deprecation")
			public BlogEntry(JsonObject js){
				if (js.containsKey("_id")) this.id = new ObjectId(js.getString("_id"));
				if (js.containsKey("content")) this.content = js.getString("content");
				if (js.containsKey("tags")) this.tags.addAll(js.getJsonArray("tags").getList());
				if (js.containsKey("title")) this.title = js.getString("title");
				if (js.containsKey("user")) this.userId = new ObjectId(js.getString("user"));
				if (js.containsKey("date")) this.date = new Date(js.getString("date"));
				if (js.containsKey("comment")){
				JsonArray arr = js.getJsonArray("comment");
				for (int i =0;i<arr.size();i++){
				this.comment.add(new Comment(arr.getJsonObject(i)));
				}
				}
				}
			public JsonArray commenttoJsonArray(){
					JsonArray jArray = new JsonArray();
					for (int i =0;i<comment.size();i++){
						jArray.add(comment.get(i).toJson());
					}
					return jArray;
			}
			public JsonObject toJson(){
					JsonObject json = new JsonObject();
				    if (id != null ) {
					      json.put("_id", id.toHexString());
					    }
				    
				    json.put("content", this.content)
				    .put("tags",Arrays.asList(tags))
				    .put("title",this.title)
				    .put("user",userId.toHexString())
				    .put("date",this.date)
				    .put("comment",commenttoJsonArray() )	    
				    ;
					return json;
			}
		 public String getUserId() {
			return userId.toHexString();
		}
		@PrePersist void prePersist() {date= new Date();}

	    public String getId() {
	        return (id != null) ? id.toHexString() : "";
	    }

		public void setUserId(ObjectId userId) {
			this.userId = userId;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public ArrayList<String> getTags() {
			return tags;
		}
		public void addTags(String tags) {
			this.tags.add(tags);
		}
		public void addTags(ArrayList<String> tags) {
			this.tags.addAll(tags);
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
