package com.example.campusgoodiessharingplatform.api;

import com.example.campusgoodiessharingplatform.model.*;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("login")
    Call<ApiResponse<User>> login(@Body Map<String, Object> body);

    @POST("register")
    Call<ApiResponse<Object>> register(@Body Map<String, Object> body);

    @PUT("user/update")
    Call<ApiResponse<Object>> updateUser(@Body Map<String, Object> body);

    @GET("article/selectPage")
    Call<ApiResponse<PageResult<Article>>> articlePage(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("title") String title, @Query("status") String status, @Query("userId") Integer userId, @Query("loginUserId") Integer loginUserId);

    @GET("article/selectById/{id}")
    Call<ApiResponse<Article>> articleById(@Path("id") int id, @Query("loginUserId") Integer loginUserId);

    @POST("article/add")
    Call<ApiResponse<Article>> addArticle(@Body Map<String, Object> body);

    @DELETE("article/delete/{id}")
    Call<ApiResponse<Object>> deleteArticle(@Path("id") int id);

    @GET("items/selectPage")
    Call<ApiResponse<PageResult<Item>>> itemPage(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("name") String name, @Query("status") Boolean status, @Query("checkStatus") String checkStatus, @Query("categoryId") Integer categoryId, @Query("userId") Integer userId, @Query("loginUserId") Integer loginUserId);

    @POST("items/add")
    Call<ApiResponse<Item>> addItem(@Body Map<String, Object> body);

    @DELETE("items/delete/{id}")
    Call<ApiResponse<Object>> deleteItem(@Path("id") int id);

    @PUT("items/updateStatus")
    Call<ApiResponse<Object>> updateItemStatus(@Body Map<String, Object> body);

    @GET("category/selectAll")
    Call<ApiResponse<java.util.List<Category>>> categories();

    @POST("likes/add")
    Call<ApiResponse<Object>> like(@Body Map<String, Object> body);

    @DELETE("likes/delete/{id}")
    Call<ApiResponse<Object>> unlike(@Path("id") int id);

    @POST("collect/add")
    Call<ApiResponse<Object>> collect(@Body Map<String, Object> body);

    @DELETE("collect/delete/{id}")
    Call<ApiResponse<Object>> uncollect(@Path("id") int id);

    @GET("comments/selectPage")
    Call<ApiResponse<PageResult<Comment>>> comments(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("articleId") int articleId);

    @POST("comments/add")
    Call<ApiResponse<Object>> addComment(@Body Map<String, Object> body);

    @DELETE("comments/delete/{id}")
    Call<ApiResponse<Object>> deleteComment(@Path("id") int id);

    @GET("charge/selectPage")
    Call<ApiResponse<PageResult<Charge>>> chargePage(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("userId") Integer userId, @Query("itemUserid") Integer itemUserid, @Query("status") String status);

    @POST("charge/add")
    Call<ApiResponse<Object>> addCharge(@Body Map<String, Object> body);

    @PUT("charge/update")
    Call<ApiResponse<Object>> updateCharge(@Body Map<String, Object> body);

    @GET("notice/selectPage")
    Call<ApiResponse<PageResult<Notice>>> notices(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @GET("notification/selectPage")
    Call<ApiResponse<PageResult<AppNotification>>> notifications(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("userId") int userId);

    @PUT("notification/read/{id}")
    Call<ApiResponse<Object>> readNotification(@Path("id") int id);

    @PUT("notification/readAll")
    Call<ApiResponse<Object>> readAll(@Query("userId") int userId);

    @Multipart
    @POST("files/upload")
    Call<ApiResponse<String>> upload(@Part MultipartBody.Part file);
}
