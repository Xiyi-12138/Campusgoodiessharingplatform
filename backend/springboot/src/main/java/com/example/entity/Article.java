package com.example.entity;

public class Article {
    private Integer id;
    private String img;
    private String title;
    private String description;
    private String content;
    private String time;
    private Integer userId;
    private String userName;
    private String avatar;
    private String status;
    private String reason;
    private Integer likeCount;
    private Integer commentCount;
    private Integer likedId;
    private Integer loginUserId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLikedId() { return likedId; }
    public void setLikedId(Integer likedId) { this.likedId = likedId; }
    public Integer getLoginUserId() { return loginUserId; }
    public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }
}
