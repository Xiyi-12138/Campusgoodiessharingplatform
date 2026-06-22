package com.example.entity;

public class Charge {

    /**ID */
    private Integer id;
    /**被交换物品ID */
    private Integer itemId;
    private String itemName;
    private String requirement;
    /**交换物品 */
    private String content;
    /**交换理由 */
    private String remark;
    /**申请人 */
    private Integer userId;
    private String userName;
    /**申请时间 */
    private String time;
    /**审核状态 */
    private String status;
    /**审核理由 */
    private String reason;
    /**交换地点 */
    private String location;
    /**交换时间 */
    private String shareTime;
    /**物品主人ID */
    private Integer itemUserid;
    private String itemUserName;

    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShareTime() {
        return shareTime;
    }

    public void setShareTime(String shareTime) {
        this.shareTime = shareTime;
    }

    public Integer getItemUserid() {
        return itemUserid;
    }

    public void setItemUserid(Integer itemUserid) {
        this.itemUserid = itemUserid;
    }

    public String getItemUserName() {
        return itemUserName;
    }

    public void setItemUserName(String itemUserName) {
        this.itemUserName = itemUserName;
    }
}
