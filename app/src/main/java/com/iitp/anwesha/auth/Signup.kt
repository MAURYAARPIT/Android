package com.iitp.anwesha.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iitp.anwesha.MyDialog
import com.iitp.anwesha.R
import com.iitp.anwesha.checkValue
import com.iitp.anwesha.databinding.FragmentSignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Signup : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var email: String
    private lateinit var phone: String
    private lateinit var password: String
    private lateinit var cPassword: String
    private lateinit var name: String
    private lateinit var college: String
    private lateinit var userType: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.signinbutton.setOnClickListener {
            loadFragment(SignIn())
        }

        val myDialog = MyDialog(requireContext())

        college = "IIT Patna"
        userType = "iitp_student"
        binding.iitStudentBtn.setOnClickListener {
            if (binding.iitStudentBtn.isChecked){
                binding.collegeName.visibility = View.GONE
                binding.anweshaCollege.visibility = View.GONE
                binding.anweshaUserType.visibility = View.GONE
                binding.userType.visibility = View.GONE

            }
            else{
                binding.collegeName.visibility = View.VISIBLE
                binding.anweshaCollege.visibility = View.VISIBLE
                binding.anweshaUserType.visibility = View.VISIBLE
                binding.userType.visibility = View.VISIBLE
            }
        }



            binding.SignupButton.setOnClickListener {
                if(binding.acceptTermButton.isChecked) {
                    name = checkValue(binding.anweshaFullName)?.trimEnd() ?: return@setOnClickListener
                    email = checkValue(binding.anweshaEmailId)?.trimEnd() ?: return@setOnClickListener
                    if(binding.iitStudentBtn.isChecked){
                        if (!isValidIITPEmail(email)){
                            Snackbar.make(view, "Please enter institute email address", Snackbar.LENGTH_LONG)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                .show()
                            return@setOnClickListener
                        }
                    }
                    else{
                        if(!isValidEmail(email)){
                            Snackbar.make(view, "Please enter valid email address", Snackbar.LENGTH_LONG)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                .show()
                        }
                    }

                    phone = checkValue(binding.anweshaPhoneNum) ?: return@setOnClickListener
                    password = checkValue(binding.AnweshaPassword) ?: return@setOnClickListener
                    cPassword = checkValue(binding.ConfirmPassword) ?: return@setOnClickListener


                    if (!binding.iitStudentBtn.isChecked){
                        college = checkValue(binding.anweshaCollege)?.trimEnd() ?: return@setOnClickListener
                        userType = checkSpinnerValue(binding.anweshaUserType) ?: return@setOnClickListener
                    }

                    if (password != cPassword) {
                        Snackbar.make(view, "Password doesn't match", Snackbar.LENGTH_LONG)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                            .show()
                        return@setOnClickListener
                    }
                    binding.ConfirmPassword.error = null
                    myDialog.showProgressDialog(this@Signup)

                    CoroutineScope(Dispatchers.Main).launch {
                        val registerUser =
                            UserRegisterInfo(email, password, name, phone, college, userType)
                        try {
                            val response =
                                UserAuthApi(requireContext()).userAuthApi.userRegister(registerUser)

                            if (response.isSuccessful) {
                                Snackbar.make(
                                    view,
                                    "You have successfully Registered!",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                    .show()
                                loadFragment(SignIn())
                            } else {
                                Snackbar.make(view, "Could not register!", Snackbar.LENGTH_LONG)
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                    .show()
                            }
                        } catch (e: Exception) {
                            myDialog.showErrorAlertDialog("Oops! It seems like an error... ${e.message}")
                        }
                        myDialog.dismissProgressDialog()
                    }
                }
                else{
                    Snackbar.make(view, "Please select Terms and Conditions", Snackbar.LENGTH_LONG)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show()
                }
            }

            val text = "I accept the Terms and conditions."
            val spannableString = SpannableString(text)

            spannableString.setSpan(UnderlineSpan(), 13, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://anwesha.live/terms"))
                    view.context.startActivity(intent)
                }
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = requireContext().resources.getColor(R.color.white)
                    textPaint.isUnderlineText = true
                }
            }

        spannableString.setSpan(clickableSpan, 13, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAcceptTerms.text = spannableString
        binding.tvAcceptTerms.movementMethod = LinkMovementMethod.getInstance()

        return view
    }

    private fun loadFragment(fragment: Fragment){
        val fragmentTransaction = activity?.supportFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.login_container, fragment)
        fragmentTransaction.commit()
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    fun isValidIITPEmail(email: String): Boolean {
        val emailRegex = Regex("^\\w+@iitp\\.ac\\.in\$")
        return emailRegex.matches(email)
    }



    private fun checkSpinnerValue(spinner: Spinner) : String? {
        var value = spinner.selectedItem.toString()
        if(value.isBlank()) {
            Toast.makeText(context, "Please Enter the user type", Toast.LENGTH_SHORT).show()
            return null
        }
        value = when(value) {
            "Student" -> "student"
            "Not a Student" -> "non-student"
            "Alumni" -> "alumni"
            "Faculty" -> "faculty"
            else -> "non-student"
        }
        return value

    }
}
