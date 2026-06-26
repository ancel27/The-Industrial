package theindustrial.app.data.remote

import theindustrial.app.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("timobile/platform/config")
    suspend fun getConfig(
        @Header("APPKEY") appKey: String,
        @Query("APPKEY") appKeyQuery: String // Sending as both to be safe
    ): Response<ConfigResponse>

    @GET("timobile/user/login")
    suspend fun login(
        @Header("APPKEY") appKey: String,
        @Header("EML") email: String,
        @Header("SECC") secc: String
    ): Response<UserResponse>

    @POST("timobile/user/send-verification")
    suspend fun sendVerification(
        @Header("APPKEY") appKey: String,
        @Header("EML") email: String
    ): Response<VerificationResponse>

    @POST("timobile/user/verify-email")
    suspend fun verifyEmail(
        @Header("APPKEY") appKey: String,
        @Header("EML") email: String,
        @Header("OTP") otp: String
    ): Response<VerifyEmailResponse>

    @POST("timobile/user/signup")
    suspend fun signup(
        @Header("APPKEY") appKey: String,
        @Header("NME") name: String,
        @Header("EML") email: String,
        @Header("MOB") mobile: String,
        @Header("SECC") secc: String,
        @Header("DOB") dob: String,
        @Header("TKN") token: String? = null
    ): Response<SignupResponse>

    @GET("timobile/content/news")
    suspend fun getNews(
        @Header("APPKEY") appKey: String
    ): Response<NewsResponse>

    @GET("timobile/content/{id}")
    suspend fun getNewsDetail(
        @Path("id") id: Int,
        @Header("APPKEY") appKey: String
    ): Response<NewsDetailResponse>

    @GET("timobile/likes/view")
    suspend fun viewLikes(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int
    ): Response<NewsResponse>

    @GET("timobile/bookmarks/view")
    suspend fun viewBookmarks(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int
    ): Response<NewsResponse>

    @POST("timobile/comments/add")
    suspend fun addComment(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Header("COMMENT") comment: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String,
        @Query("COMMENT") commentQ: String
    ): Response<CommentResponse>

    // --- Like / Unlike ---
    @POST("timobile/likes/like")
    suspend fun like(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String
    ): Response<StatusToggleResponse>

    @POST("timobile/likes/unlike")
    suspend fun unlike(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String
    ): Response<StatusToggleResponse>

    // --- Bookmark / Unbookmark ---
    @POST("timobile/bookmarks/bookmark")
    suspend fun bookmark(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String
    ): Response<StatusToggleResponse>

    @POST("timobile/bookmarks/unbookmark")
    suspend fun unbookmark(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String
    ): Response<StatusToggleResponse>
}
