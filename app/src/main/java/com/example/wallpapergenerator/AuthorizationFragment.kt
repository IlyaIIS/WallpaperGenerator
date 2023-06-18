package com.example.wallpapergenerator

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.wallpapergenerator.databinding.FragmentAuthorizationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.network.NetRepository
import javax.inject.Inject

class AuthorizationFragment : Fragment() {
    @Inject lateinit var netRepository: NetRepository
    private lateinit var binding: FragmentAuthorizationBinding
    private val _authMessage = MutableLiveData<String?>()
    private val authMessage: LiveData<String?> = _authMessage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as MainApplication).appComponent.inject(this)
        binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        authMessage.observe(viewLifecycleOwner, Observer<String?>() {
            if(it.isNullOrBlank()){
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                binding.authErrorMessage.text = it
            }
        })
        super.onViewCreated(view, savedInstanceState)

        binding.toRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.toBackButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.authorizationButton.setOnClickListener {
            val username : String = binding.authEditTextTextUserName.text.toString()
            val password : String = binding.authEditTextTextPassword.text.toString()
            when{
                username.isEmpty() -> binding.authErrorMessage.text = getString(R.string.auth_input_name)
                password.isEmpty() -> binding.authErrorMessage.text = getString(R.string.auth_input_password)
            }
            if(username.isNotEmpty() && password.isNotEmpty()) {
                binding.authErrorMessage.text = ""
                netRepository.authorize(username, password, _authMessage)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}