package app.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SubscriptionPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubscriptionPage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var backButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var subscribedInLabel: TextView
    private var isSubscribed = false // Initial subscription status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_subscription_page, container, false)
        var view = inflater.inflate(R.layout.fragment_subscription_page, container, false)
        backButton = view.findViewById(R.id.goToSettingsUser)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }
        subscribeButton = view.findViewById(R.id.buttonSub)
        subscribeButton.setOnClickListener {
            openGooglePlayStore()
            isSubscribed = true // Simulate subscription status change
        }

        subscribedInLabel = view.findViewById(R.id.subscribed_in_label)

        // Set initial subscription status
        updateSubscriptionStatus(isSubscribed)
        return view
    }
    override fun onResume() {
        super.onResume()
        // Update subscription status when returning to the app
        updateSubscriptionStatus(isSubscribed)
    }
    private fun openGooglePlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://play.google.com/store")
        startActivity(intent)
    }
    private fun updateSubscriptionStatus(isSubscribed: Boolean) {
        if (isSubscribed) {
            subscribedInLabel.text = "Subscribed"
            subscribedInLabel.setBackgroundResource(R.color.green)
        } else {
            subscribedInLabel.text = "Not subscribed"
            subscribedInLabel.setBackgroundResource(R.color.red)
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SubscriptionPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SubscriptionPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}