package com.easypayrn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.easypaysolutions.customer_sheet.CustomerSheet
import com.easypaysolutions.customer_sheet.utils.CustomerSheetResult
import com.easypaysolutions.payment_sheet.PaymentSheet
import com.easypaysolutions.payment_sheet.utils.PaymentSheetResult
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.facebook.react.modules.core.DeviceEventManagerModule

class MainActivity : ReactActivity() {
    lateinit var customerSheet: CustomerSheet
    lateinit var paymentSheet: PaymentSheet
    var reactContext: ReactContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        customerSheet = CustomerSheet(this, ::onCustomerSheetResult)
    }

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun getMainComponentName(): String = "EasyPayRN"

    /**
     * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
     * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
     */
    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    //region Emitter methods
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Failed -> {
                val params = Arguments.createMap().apply {
                    putString("error", paymentSheetResult.error.localizedMessage)
                }
                sendEvent("onPaymentError", params)
            }

            is PaymentSheetResult.Canceled -> {
                sendEvent("onPaymentCancelled", null)
            }

            is PaymentSheetResult.Completed -> {
                val params = Arguments.createMap().apply {
                    val deletedConsents = Arguments.createArray()
                    for (c in paymentSheetResult.deletedConsents ?: listOf()) {
                        deletedConsents.pushInt(c)
                    }
                    val addedConsents = Arguments.createArray()
                    for (c in paymentSheetResult.addedConsents ?: listOf()) {
                        val writableMap = Arguments.createMap()
                        writableMap.putInt("consentId", c.consentId)
                        writableMap.putInt("expirationYear", c.expirationYear)
                        writableMap.putInt("expirationMonth", c.expirationMonth)
                        writableMap.putString("last4digits", c.last4digits)
                        addedConsents.pushMap(writableMap)
                    }
                    val data = Arguments.createMap()?.apply {
                        paymentSheetResult.data.apply {
                            putInt("errorCode", errorCode)
                            putString("errorMessage", errorMessage)
                            putString("responseMessage", responseMessage)
                            putBoolean("txApproved", txApproved)
                            putInt("txId", txId)
                            putString("txCode", txCode)
                            putString("avsResult", avsResult)
                            putString("acquirerResponseEmv", acquirerResponseEmv)
                            putString("cvvResult", cvvResult)
                            putBoolean("isPartialApproval", isPartialApproval)
                            putBoolean("requiresVoiceAuth", requiresVoiceAuth)
                            putDouble("responseApprovedAmount", responseApprovedAmount)
                            putDouble("responseAuthorizedAmount", responseAuthorizedAmount)
                            putDouble("responseBalanceAmount", responseBalanceAmount)
                        }
                    }

                    putMap("data", data)
                    putArray("addedConsents", addedConsents)
                    putArray("deletedConsents", deletedConsents)
                }
                sendEvent("onPaymentResult", params)
            }
        }
    }

    private fun onCustomerSheetResult(customerSheetResult: CustomerSheetResult) {
        when (customerSheetResult) {
            is CustomerSheetResult.Failed -> {
                val params = Arguments.createMap().apply {
                    putString("error", customerSheetResult.error.localizedMessage)
                }
                sendEvent("onManageError", params)
            }

            is CustomerSheetResult.Selected -> {
                val params = Arguments.createMap().apply {
                    val deletedConsents = Arguments.createArray()
                    for (c in customerSheetResult.deletedConsents) {
                        deletedConsents.pushInt(c)
                    }
                    val addedConsents = Arguments.createArray()
                    for (c in customerSheetResult.addedConsents) {
                        val writableMap = Arguments.createMap()
                        writableMap.putInt("consentId", c.consentId)
                        writableMap.putInt("expirationYear", c.expirationYear)
                        writableMap.putInt("expirationMonth", c.expirationMonth)
                        writableMap.putString("last4digits", c.last4digits)
                        addedConsents.pushMap(writableMap)
                    }
                    putInt("selectedConsentId", customerSheetResult.selectedConsentId ?: 0)
                    putArray("addedConsents", addedConsents)
                    putArray("deletedConsents", deletedConsents)
                }
                sendEvent("onManageResult", params)
            }
        }
    }

    //endregion

    //region Events emitter

    private fun sendEvent(
        eventName: String,
        params: WritableMap?
    ) {
        Handler(Looper.getMainLooper()).post {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(eventName, params)
        }
    }
    //endregion
}
