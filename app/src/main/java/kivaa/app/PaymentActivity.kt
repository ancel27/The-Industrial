package kivaa.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : Activity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        val name = intent.getStringExtra("NAME") ?: "Industrial Subscription"
        val description = intent.getStringExtra("DESCRIPTION") ?: "Plan Upgrade"
        val email = intent.getStringExtra("EMAIL") ?: ""
        val contact = intent.getStringExtra("CONTACT") ?: ""
        val logoUrl = intent.getStringExtra("LOGO_URL") ?: "https://kivaa.io.in/assets/logo.png"
        val themeColor = intent.getStringExtra("THEME_COLOR") ?: "#003366"
        val razorpayKey = BuildConfig.RAZORPAY_KEY

        startPayment(amount, name, description, email, contact, logoUrl, themeColor, razorpayKey)
    }

    private fun startPayment(
        amount: Double, 
        name: String, 
        description: String, 
        email: String, 
        contact: String,
        logo: String,
        color: String,
        key: String
    ) {
        val checkout = Checkout()
        checkout.setKeyID(key)

        try {
            val options = JSONObject()
            options.put("name", name)
            options.put("description", description)
            options.put("image", logo)
            options.put("theme.color", color)
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // Amount in paise

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", email)
            prefill.put("contact", contact)
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            finishWithResult(false, "Initialization error")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        finishWithResult(true, razorpayPaymentId ?: "Success")
    }

    override fun onPaymentError(code: Int, response: String?) {
        finishWithResult(false, response ?: "Payment cancelled")
    }

    private fun finishWithResult(success: Boolean, message: String) {
        val intent = Intent()
        intent.putExtra("SUCCESS", success)
        intent.putExtra("MESSAGE", message)
        setResult(RESULT_OK, intent)
        finish()
    }
}
