package kivaa.app.data.model

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<PlatformConfig>? = null
)

data class PlatformConfig(
    @SerializedName("platform_id") val platformId: Int? = null,
    @SerializedName("platformname") val platformName: String? = null,
    @SerializedName("sitetagline") val siteTagline: String? = null,
    @SerializedName("baseurl") val baseUrl: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("favicon_url") val faviconUrl: String? = null,
    @SerializedName("cdn_url") val cdnUrl: String? = null,
    @SerializedName("theme") val theme: ThemeConfig? = null,
    @SerializedName("about") val about: String? = null,
    @SerializedName("contactemail") val contactEmail: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class ThemeConfig(
    @SerializedName("primary") val primary: String? = null,
    @SerializedName("secondary") val secondary: String? = null,
    @SerializedName("accent") val accent: String? = null,
    @SerializedName("background") val background: String? = null,
    @SerializedName("text") val text: String? = null
)

data class UserResponse(
    @SerializedName("UserHeader") val userHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("UserDetails") val userDetails: List<UserDetail>? = null
)

data class UserDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("dateofbirth") val dateOfBirth: String? = null,
    @SerializedName("otp_done") val otpDone: String? = null,
    @SerializedName("active") val active: Int? = null,
    @SerializedName("primaryaddress") val primaryAddress: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SignupResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<UserDetail>? = null
)

// --- News Models ---

data class NewsResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<NewsItem>? = null
)

data class NewsItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("iahsh") val hash: String? = null,
    @SerializedName("types") val types: String? = null,
    @SerializedName("product_name") val title: String? = null,
    @SerializedName("Startdate") val startDate: String? = null,
    @SerializedName("brief_intro") val briefIntro: String? = null,
    @SerializedName("coverimage") val coverImage: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("link") val link: String? = null
)

// --- News Detail Models ---

data class NewsDetailResponse(
    @SerializedName("trendcontentheader") val header: Int? = null,
    @SerializedName("trendcontenttotal") val total: Int? = null,
    @SerializedName("trendcontent1") val details: List<NewsDetailItem>? = null
)

data class NewsDetailItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("iahsh") val hash: String? = null,
    @SerializedName("product_name") val title: String? = null,
    @SerializedName("Startdate") val startDate: String? = null,
    @SerializedName("brief_intro") val briefIntro: String? = null,
    @SerializedName("proj_description") val fullDescription: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("link") val link: String? = null
)

// --- Signup & Verification Models ---

data class VerificationResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<VerificationDetail>? = null
)

data class VerificationDetail(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("sent") val sent: Boolean? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null
)

data class VerifyEmailResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<VerifyEmailDetail>? = null
)

data class VerifyEmailDetail(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("verified") val verified: Boolean? = null
)

// --- Comment Models ---

data class CommentResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<CommentDetail>? = null
)

data class CommentDetail(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("entity_type") val entityType: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("text") val aiText: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("content") val content: NewsItem? = null
)

// --- Toggle Status Models ---

data class StatusToggleResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<ToggleDetail>? = null
)

data class ToggleDetail(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("entity_type") val entityType: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("liked") val liked: Boolean? = null,
    @SerializedName("bookmarked") val bookmarked: Boolean? = null
)

// --- History Models ---

data class HistoryResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<HistoryItem>? = null
)

data class HistoryItem(
    @SerializedName("entity_type") val entityType: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("content") val content: NewsItem? = null
)

// --- Preference Models ---

data class PreferenceResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<PreferenceItem>? = null
)

data class PreferenceItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("keyword") val keyword: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Subscription Models ---

data class SubscriptionPlanResponse(
    @SerializedName("ProvHeader") val header: Int? = null,
    @SerializedName("ProvTotal") val total: Int? = null,
    @SerializedName("ProvDetails") val plans: List<SubscriptionPlan>? = null
)

data class SubscriptionPlan(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("planname") val name: String? = null,
    @SerializedName("plandetails") val details: String? = null,
    @SerializedName("planterm") val term: Int? = null,
    @SerializedName("planamt") val amount: String? = null,
    @SerializedName("plangst") val gst: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("is_featured") val isFeatured: Int? = null
)

// --- Magazine Models ---

data class MagazineResponse(
    @SerializedName("MagHeader") val header: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("MagDetails") val magazines: List<MagazineItem>? = null
)

data class MagazineItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("maghash") val hash: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("intro") val intro: String? = null,
    @SerializedName("start_at") val date: String? = null,
    @SerializedName("image_path") val image: String? = null,
    @SerializedName("urltag") val urlTag: String? = null,
    @SerializedName("access") val hasAccess: String? = null,
    @SerializedName("publishedby") val publishedBy: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("frequency") val frequency: String? = null,
    @SerializedName("magazineurl") val magazineUrl: String? = null,
    @SerializedName("readerurl") val readerUrl: String? = null
)

// --- Support Models ---

data class TicketResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<TicketDetail>? = null
)

data class TicketDetail(
    @SerializedName("rqsttoken") val token: String? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Conversation Models ---

data class ConversationResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<MessageDetail>? = null
)

data class MessageDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("sender_type") val senderType: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Order Models ---

data class OrderResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<OrderDetail>? = null
)

data class OrderDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("subordno") val orderNo: String? = null,
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("planname") val planName: String? = null,
    @SerializedName("planamt") val amount: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Address Models ---

data class AddressResponse(
    @SerializedName("AddressHeader") val header: Int? = null,
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("AddressDetails") val addressDetails: List<AddressDetail>? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<AddressDetail>? = null
)

data class AddressDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address_line1") val line1: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("pincode") val pincode: String? = null,
    @SerializedName("id_default") val isDefault: Int? = null,
    @SerializedName("type") val type: String? = null
)
