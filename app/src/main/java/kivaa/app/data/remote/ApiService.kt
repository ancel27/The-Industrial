package kivaa.app.data.remote

import kivaa.app.data.model.*
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
        @Header("SECC") secc: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("EML") emailQ: String,
        @Query("SECC") seccQ: String
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

    @POST("timobile/user/forgotpassword")
    suspend fun resetPassword(
        @Header("APPKEY") appKey: String,
        @Header("EML") email: String,
        @Header("OTP") otp: String,
        @Header("SECC") newPassword: String
    ): Response<CommentResponse>

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
        @Header("APPKEY") appKey: String,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<NewsResponse>

    @GET("timobile/content/articles")
    suspend fun getArticles(
        @Header("APPKEY") appKey: String,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<NewsResponse>

    @GET("timobile/content/interviews")
    suspend fun getInterviews(
        @Header("APPKEY") appKey: String,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<NewsResponse>

    @GET("timobile/content/casestudy")
    suspend fun getCaseStudies(
        @Header("APPKEY") appKey: String,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<NewsResponse>

    @GET("timobile/content/exclusives")
    suspend fun getExclusives(
        @Header("APPKEY") appKey: String,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30,
        @Query("CTYP") contentType: String? = null
    ): Response<NewsResponse>

    @GET("timobile/content/for-you")
    suspend fun getForYouContent(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PAGE") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<NewsResponse>

    @GET("timobile/content/{id}")
    suspend fun getNewsDetail(
        @Path("id") id: Int,
        @Header("APPKEY") appKey: String
    ): Response<NewsDetailResponse>

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

    @GET("timobile/comments/public")
    suspend fun getPublicComments(
        @Header("APPKEY") appKey: String,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String,
        @Query("LIMIT") limit: Int = 30
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

    @POST("timobile/history/add")
    suspend fun addHistory(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Header("TITLE") title: String? = null,
        @Header("SLUGURL") slugUrl: String? = null,
        @Header("TYPES") types: String? = null,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String
    ): Response<CommentResponse>

    @GET("timobile/history/view")
    suspend fun viewHistory(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PG") page: Int = 1,
        @Query("LIMIT") limit: Int = 10
    ): Response<HistoryResponse>

    @GET("timobile/likes/view")
    suspend fun viewLikes(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PG") page: Int = 1,
        @Query("LIMIT") limit: Int = 10
    ): Response<NewsResponse>

    @GET("timobile/bookmarks/view")
    suspend fun viewBookmarks(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PG") page: Int = 1,
        @Query("LIMIT") limit: Int = 10
    ): Response<NewsResponse>

    @GET("timobile/comments/view")
    suspend fun viewUserComments(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PG") page: Int = 1,
        @Query("LIMIT") limit: Int = 10
    ): Response<CommentResponse>

    @GET("timobile/reviews/list")
    suspend fun getReviews(
        @Header("APPKEY") appKey: String,
        @Header("ENTITYTYPE") entityType: String,
        @Header("ENTITYID") entityId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("ENTITYTYPE") entityTypeQ: String,
        @Query("ENTITYID") entityIdQ: String,
        @Query("LIMIT") limit: Int = 30
    ): Response<CommentResponse>

    @POST("timobile/reviews/add")
    suspend fun addReview(
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

    @GET("timobile/reviews/view")
    suspend fun viewUserReviews(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("PG") page: Int = 1,
        @Query("LIMIT") limit: Int = 30
    ): Response<CommentResponse>

    // --- Preferences ---
    @GET("timobile/preferences/view")
    suspend fun viewPreferences(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int
    ): Response<PreferenceResponse>

    // --- Support ---
    @GET("timobile/support/requests")
    suspend fun getTickets(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int
    ): Response<TicketResponse>

    @POST("timobile/support/srcreate")
    suspend fun createTicket(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("DEPT") department: String,
        @Header("SUBJ") subject: String,
        @Header("MESG") message: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("DEPT") departmentQ: String,
        @Query("SUBJ") subjectQ: String,
        @Query("MESG") messageQ: String
    ): Response<TicketResponse>

    @POST("timobile/support/srreply")
    suspend fun replyToTicket(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("RQSTID") ticketId: String,
        @Header("MESG") message: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("RQSTID") ticketIdQ: String,
        @Query("MESG") messageQ: String
    ): Response<TicketResponse>

    @POST("timobile/support/srclose")
    suspend fun closeTicket(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("RQSTID") ticketId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("RQSTID") ticketIdQ: String
    ): Response<TicketResponse>

    @GET("timobile/support/conversation")
    suspend fun getTicketConversation(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("RQSTID") ticketId: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("RQSTID") ticketIdQ: String
    ): Response<ConversationResponse>

    @POST("timobile/preferences/add")
    suspend fun addPreference(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("KEYWORD") keyword: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("KEYWORD") keywordQ: String
    ): Response<PreferenceResponse>

    @POST("timobile/preferences/remove")
    suspend fun removePreference(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("PREFID") prefId: Int? = null,
        @Header("KEYWORD") keyword: String? = null,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("PREFID") prefIdQ: Int? = null,
        @Query("KEYWORD") keywordQ: String? = null
    ): Response<PreferenceResponse>

    @GET("timobile/content/search")
    suspend fun searchContent(
        @Header("APPKEY") appKey: String,
        @Header("SRCH") query: String,
        @Header("STYP") type: String = "all",
        @Query("APPKEY") appKeyQ: String,
        @Query("SRCH") queryQ: String,
        @Query("STYP") typeQ: String = "all"
    ): Response<NewsResponse>

    // --- Magazines ---
    @GET("timobile/content/magazinescollection")
    suspend fun getMagazines(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int
    ): Response<MagazineResponse>

    @GET("timobile/content/magazinedetail")
    suspend fun getMagazineDetail(
        @Header("APPKEY") appKey: String,
        @Header("MGHSH") magazineHash: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("MGHSH") magazineHashQ: String
    ): Response<MagazineResponse>

    @GET("timobile/subscribe/plans")
    suspend fun getSubscriptionPlans(
        @Header("APPKEY") appKey: String
    ): Response<SubscriptionPlanResponse>

    @POST("timobile/subscribe/order")
    suspend fun createOrder(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("PLAID") planId: Int,
        @Header("ADDID") addressId: Int,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("PLAID") planIdQ: Int,
        @Query("ADDID") addressIdQ: Int
    ): Response<OrderResponse>

    @GET("timobile/subscribe/orders")
    suspend fun getOrders(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int
    ): Response<OrderResponse>

    @GET("timobile/ask-kivaa/generate")
    suspend fun askKivaa(
        @Header("APPKEY") appKey: String,
        @Header("PROMPT") prompt: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("PROMPT") promptQ: String
    ): Response<CommentResponse>

    // --- Addresses ---
    @GET("timobile/user/address")
    suspend fun getAddresses(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int
    ): Response<AddressResponse>

    @POST("timobile/user/createaddress")
    suspend fun createAddress(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Header("NME") name: String,
        @Header("PHNO") phone: String,
        @Header("PIN") pincode: String,
        @Header("ADD1") line1: String,
        @Header("ADD2") line2: String,
        @Header("CITY") city: String,
        @Header("LMRK") landmark: String,
        @Header("STID") state: String,
        @Header("TYPE") type: String,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int,
        @Query("NME") nameQ: String,
        @Query("PHNO") phoneQ: String,
        @Query("PIN") pincodeQ: String,
        @Query("ADD1") line1Q: String,
        @Query("ADD2") line2Q: String,
        @Query("CITY") cityQ: String,
        @Query("LMRK") landmarkQ: String,
        @Query("STID") stateQ: String,
        @Query("TYPE") typeQ: String
    ): Response<AddressResponse>

    @GET("timobile/user/profile")
    suspend fun getProfile(
        @Header("APPKEY") appKey: String,
        @Header("USRID") userId: Int,
        @Query("APPKEY") appKeyQ: String,
        @Query("USRID") userIdQ: Int
    ): Response<UserResponse>
}
