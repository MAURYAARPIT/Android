package com.iitp.anwesha.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.iitp.anwesha.R
import com.iitp.anwesha.databinding.FragmentSingleEventBinding
import com.iitp.anwesha.home.EventList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iitp.anwesha.profile.UserProfileApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SingleEventFragment : Fragment() {
    private lateinit var binding : FragmentSingleEventBinding
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView?.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleEventBinding.inflate(inflater,container,false)
        val eventID = arguments?.getString("eventID")

        CoroutineScope(Dispatchers.IO).launch {

            // user login first
            val response = SingleEventApi(requireContext()).singleEventApi.getEvent(eventID!!)
            if (response.isSuccessful) {
                event = response.body()!!

                requireActivity().runOnUiThread(Runnable {
                    Glide.with(requireContext())
                        .load(event.poster)
                        .into(binding.eventPoster)

                    binding.eventName.text = event.name

                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("EEE dd MMMM yyyy, HH:mm", Locale.getDefault())
                    val outputFormat1 = SimpleDateFormat("dd MMMM" , Locale.getDefault())
                    val outputFormat2 = SimpleDateFormat("dd", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC+5:30") // set input timezone to UTC
                    outputFormat.timeZone = TimeZone.getDefault() // set output timezone to default timezone
                    val startTimeString = outputFormat2.format(inputFormat.parse(event.start_time!!)!!) // format date object into output string
                    val endDateString = outputFormat1.format(inputFormat.parse(event.end_time!!)!!)
                    val startTimeSeparatedStrings = startTimeString.split(",")
                    val endTimeSeparatedString1 = endDateString.split(",")
                    binding.eventDate.text = startTimeSeparatedStrings[0]+" - "+endTimeSeparatedString1[0]
//            binding.eventStartTime.text = separatedStrings[1]

                    binding.eventDescription.text = event.description

                    if (event.max_team_size==event.max_team_size){
                        binding.teamSize.text = "Individual Participant"
                    }
                    else{
                        binding.teamSize.text = "${event.min_team_size}-${event.max_team_size} Peoples"
                    }

                    binding.registrationFee.text = "₹"+event.registration_fee

                    val endTime = event.registration_deadline
                    val endDate = inputFormat.parse(endTime)
                    val endTimeString = outputFormat.format(endDate!!)
                    val endTimeSeparatedString= endTimeString.split(",").map{it.trim()}
                    binding.registerDeadline.text = endTimeSeparatedString[0]

                    binding.eventLocation.text = event.venue

                    val organizerT = event.organizer.split(",")
                    var organizer = ""
                    for (string in organizerT) {
                        organizer = organizer+string+"\n"
                    }
                    binding.organizer.text = organizer

                    binding.prize.text = "Prizes worth ₹${event.prize}"

                    if(!event.is_active){
                        binding.registerBtn.visibility = View.GONE
                    }

                    binding.registerBtn.setOnClickListener {
                        if(event.is_active){
                            if(event.is_online){
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.registration_link))
                                startActivity(intent)
                            }
                            else{
                                // call solo or team api depending on event and then redirect to payu
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.registration_link))

                                val headers = Bundle()

                                val sharedPref = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

                                var cookieString: String = ""

                                for(cookie in sharedPref.getStringSet(getString(R.string.cookies), HashSet())!!) {
                                    cookieString += "$cookie; "

                                }

                                headers.putString("Set-Cookie", cookieString)

                                intent.putExtra(Browser.EXTRA_HEADERS, headers)

                                startActivity(intent)


                            }
                        }
                    }

                    if(event.video.isEmpty()){
                        binding.rulebookBtn.visibility = View.GONE
                    }
                    binding.rulebookBtn.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.video))
                        startActivity(intent)
                    }
                })

            }
            else{
                Log.d("fail", "${response.message()}")
            }
        }






        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(null, 0)
        }

        return binding.root
    }

}