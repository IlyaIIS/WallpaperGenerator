package com.example.wallpapergenerator

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.wallpapergenerator.databinding.FragmentRegistrationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.network.Repository
import javax.inject.Inject

class RegistrationFragment : Fragment() {
    @Inject lateinit var repository: Repository
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as MainApplication).appComponent.inject(this)
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toAuthorizeButton.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.toBackButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            val username : String = binding.regEditTextTextUserName.text.toString()
            val email : String = binding.regEditTextTextEmailAddress.text.toString()
            val password : String = binding.regEditTextTextPassword.text.toString()
            val confirmPassword : String = binding.regEditTextTextConfirmPassword.text.toString()

            when{
                username.isEmpty() -> binding.regErrorMessage.text = "Введите имя"
                email.isEmpty() -> binding.regErrorMessage.text = "Введите почту"
                password.isEmpty() -> binding.regErrorMessage.text = "Введите пароль"
                confirmPassword.isEmpty() -> binding.regErrorMessage.text = "Подтвердите пароль"
                password != confirmPassword -> binding.regErrorMessage.text = "Пароли не совпадают"
            }
            if(password == confirmPassword
                && username.isNotEmpty()
                && email.isNotEmpty()
                && password.isNotEmpty()
                && confirmPassword.isNotEmpty()) {
                binding.regErrorMessage.text = ""
                repository.register(username, email, password)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}