package theindustrial.`in`

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var newsBtn: ImageButton
    private lateinit var magsBtn: ImageButton
    private lateinit var excluBtn: ImageButton
    private lateinit var cartBtn: ImageButton

    private  lateinit var menuBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        window.statusBarColor = Color.WHITE

        newsBtn = findViewById(R.id.news)
        magsBtn = findViewById(R.id.mags)
        excluBtn = findViewById(R.id.exclu)
        cartBtn = findViewById(R.id.cart)
        menuBtn = findViewById(R.id.menu)

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(News())
            highlightButton(newsBtn)
        }

        newsBtn.setOnClickListener {
            loadFragment(News())
            highlightButton(newsBtn)
        }

        magsBtn.setOnClickListener {
            loadFragment(magzine())
            highlightButton(magsBtn)
        }

        excluBtn.setOnClickListener {
            loadFragment(exclusive())
            highlightButton(excluBtn)
        }

        cartBtn.setOnClickListener {
            loadFragment(cart())
            highlightButton(cartBtn)
        }

        menuBtn.setOnClickListener {
            val sheet = MenuBottomSheet()
            sheet.show(supportFragmentManager, "menu")
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    private fun highlightButton(selected: ImageButton) {

        val buttons = listOf(
            newsBtn,
            magsBtn,
            excluBtn,
            cartBtn,
            menuBtn
        )

        // Reset all buttons to gray
        buttons.forEach {
            it.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }

        // Selected button becomes red
        selected.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.red))
    }
}