package com.securedocs.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securedocs.app.R
import com.securedocs.app.databinding.ActivityLoginBinding
import com.securedocs.app.utils.Prefs

/**
 * PIN login screen.
 * - First launch: asks user to set a 4-digit PIN.
 * - Subsequent launches: validates entered PIN.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val enteredPin = StringBuilder()
    private var isSettingPin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSettingPin = !Prefs.isPinSet()
        binding.tvLoginTitle.text = if (isSettingPin) "नया PIN सेट करें" else "PIN डालें"

        setupNumpad()
    }

    // ── Numpad setup ──────────────────────────────────────────────────────────

    private fun setupNumpad() {
        val digitButtons = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        )
        digitButtons.forEach { (btn, digit) ->
            btn.setOnClickListener { appendDigit(digit) }
        }
        binding.btnBackspace.setOnClickListener { removeLastDigit() }
        binding.btnClear.setOnClickListener    { clearPin() }
    }

    private fun appendDigit(digit: String) {
        if (enteredPin.length >= 4) return
        enteredPin.append(digit)
        updateDots()
        if (enteredPin.length == 4) processPin()
    }

    private fun removeLastDigit() {
        if (enteredPin.isNotEmpty()) {
            enteredPin.deleteCharAt(enteredPin.length - 1)
            updateDots()
        }
    }

    private fun clearPin() {
        enteredPin.clear()
        updateDots()
    }

    private fun updateDots() {
        val filled = "● ".repeat(enteredPin.length)
        val empty  = "○ ".repeat(4 - enteredPin.length)
        binding.tvPinDots.text = (filled + empty).trim()
    }

    // ── PIN logic ─────────────────────────────────────────────────────────────

    private fun processPin() {
        val pin = enteredPin.toString()
        if (isSettingPin) {
            Prefs.savePin(pin)
            Toast.makeText(this, "PIN set हो गया!", Toast.LENGTH_SHORT).show()
            goHome()
        } else {
            if (Prefs.verifyPin(pin)) {
                goHome()
            } else {
                Toast.makeText(this, "गलत PIN! फिर से try करें।", Toast.LENGTH_SHORT).show()
                // Flash red then reset
                binding.tvPinDots.setTextColor(getColor(R.color.error_red))
                binding.tvPinDots.postDelayed({
                    binding.tvPinDots.setTextColor(getColor(android.R.color.white))
                    clearPin()
                }, 500L)
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
