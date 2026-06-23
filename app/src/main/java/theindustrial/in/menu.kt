package theindustrial.`in`

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnFavorites = view.findViewById<Button>(R.id.btnFavorites)
        val btnUsefulLinks = view.findViewById<Button>(R.id.btnUsefulLinks)
        val contentContainer =
            view.findViewById<FrameLayout>(R.id.contentContainer)

        // Load Useful Links by default
        loadLayout(contentContainer, R.layout.layout_useful_links)

        btnFavorites.setOnClickListener {

            loadLayout(contentContainer, R.layout.layout_favorites)

            btnFavorites.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.red)
                )

            btnUsefulLinks.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray)
                )
        }

        btnUsefulLinks.setOnClickListener {

            loadLayout(contentContainer, R.layout.layout_useful_links)

            btnUsefulLinks.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.red)
                )

            btnFavorites.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray)
                )
        }
    }

    private fun loadLayout(
        container: FrameLayout,
        layoutRes: Int
    ) {
        container.removeAllViews()

        val content = layoutInflater.inflate(
            layoutRes,
            container,
            false
        )

        container.addView(content)
    }
}