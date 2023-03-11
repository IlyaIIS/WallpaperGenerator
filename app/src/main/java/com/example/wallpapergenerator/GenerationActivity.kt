package com.example.wallpapergenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding

class GenerationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

/*    fun onClickMenu(view: View) {
        val fragmentMenu = PopupMenuFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (isMenuOpened) {
            fragmentTransaction.remove(fragmentMenu)
        } else {
            //fragmentTransaction.add(fragmentMenu, "menuFragment")
            fragmentTransaction.replace(binding.menuContainer.id, fragmentMenu)
        }
        fragmentTransaction.commit()
    }*/
    fun onClickMenu(view: View) {
        if (binding.menuContainer.isVisible) {
            binding.menuContainer.visibility = View.GONE
        } else {
            binding.menuContainer.visibility = View.VISIBLE
        }
    }
}