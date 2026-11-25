package com.intentpay.data.detection

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import com.intentpay.BuildConfig
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.intentpay.ui.intercept.UpiInterceptActivity

class UpiAccessibilityService : AccessibilityService() {

    private val targetPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app", // PhonePe
        "net.one97.paytm", // Paytm
        "in.org.npci.upiapp" // BHIM
    )

    private var lastAmount: String? = null
    private var lastPayee: String? = null
    private var inUpiApp = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.packageNames = targetPackages.toTypedArray()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        // We only need to monitor text to scrape amount and payee
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info
        if (BuildConfig.DEBUG) Log.d("UpiIntercept", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return

        if (targetPackages.contains(packageName)) {
            inUpiApp = true
            val rootNode = rootInActiveWindow ?: return
            scrapeNodeInfo(rootNode)
        } else if (inUpiApp && !targetPackages.contains(packageName)) {
            // User left the UPI App (either payment complete or cancelled)
            inUpiApp = false
            
            // If we captured an amount, shoot up IntentPay asking for intent
            if (lastAmount != null) {
                launchInterceptActivity(lastAmount!!, lastPayee ?: "Unknown Merchant")
                // Reset
                lastAmount = null
                lastPayee = null
            }
        }
    }

    private fun scrapeNodeInfo(node: AccessibilityNodeInfo?) {
        node ?: return
        if (node.text != null) {
            val text = node.text.toString()
            // Look for amount (starts with ₹ or contains ₹)
            if (text.contains("₹") && text.length < 15) {
                // Extract numbers
                val amountStr = text.replace("[^0-9.]".toRegex(), "")
                if (amountStr.isNotBlank() && amountStr != ".") {
                    lastAmount = amountStr
                }
            }
            // Heuristics for Payee:
            // "Paying [Name]" or "To [Name]"
            if (text.startsWith("Paying ", ignoreCase = true)) {
                lastPayee = text.substring(7).trim()
            } else if (text.startsWith("To ", ignoreCase = true)) {
                lastPayee = text.substring(3).trim()
            }
        }

        for (i in 0 until node.childCount) {
            scrapeNodeInfo(node.getChild(i))
        }
    }

    private fun launchInterceptActivity(amount: String, payee: String) {
        if (BuildConfig.DEBUG) Log.d("UpiIntercept", "Launching intercept for ₹$amount to $payee")
        val intent = Intent(this, UpiInterceptActivity::class.java)
        intent.putExtra("AMOUNT", amount)
        intent.putExtra("PAYEE", payee)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onInterrupt() {
        if (BuildConfig.DEBUG) Log.d("UpiIntercept", "Accessibility Service Interrupted")
    }
}
