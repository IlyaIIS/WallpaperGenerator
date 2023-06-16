package com.example.wallpapergenerator

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.wallpapergenerator.databinding.FragmentAuthorizationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.network.Repository
import javax.inject.Inject

class AuthorizationFragment : Fragment() {
    @Inject lateinit var repository: Repository
    private lateinit var binding: FragmentAuthorizationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as MainApplication).appComponent.inject(this)
        binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                username.isEmpty() -> binding.authErrorMessage.text = "Введите имя"
                password.isEmpty() -> binding.authErrorMessage.text = "Введите пароль"
            }
            if(username.isNotEmpty() && password.isNotEmpty()) {
                binding.authErrorMessage.text = ""
                repository.authorize(username, password)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}