package theindustrial.app.data.model

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
    @SerializedName("active") val active: Int? = null
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

// --- Comment Models ---

data class CommentResponse(
    @SerializedName("ResponseHeader") val responseHeader: Int? = null,
    @SerializedName("Total") val total: Int? = null,
    @SerializedName("ReseponseDetails") val responseDetails: List<CommentDetail>? = null
)

data class CommentDetail(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("entity_type") val entityType: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("status") val status: Int? = null
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
