package com.easypayrn

import com.easypaysolutions.EasyPay
import com.easypaysolutions.EasyPayConfiguration
import com.easypaysolutions.customer_sheet.CustomerSheet
import com.easypaysolutions.networking.rsa.RsaCertificateStatus
import com.easypaysolutions.payment_sheet.PaymentSheet
import com.easypaysolutions.repositories.annual_consent.create.ConsentCreatorParam
import com.easypaysolutions.repositories.charge_cc.AmountsParam
import com.easypaysolutions.repositories.charge_cc.EndCustomerBillingAddressParam
import com.easypaysolutions.repositories.charge_cc.EndCustomerDataParam
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import java.util.Date

class EasyPayModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "EasyPayModule"
    private var listenerCount = 0

    @ReactMethod
    fun configureSecrets(
        sessionKey: String,
        hmacSecret: String,
        sentryKey: String?
    ) {
        EasyPay.init(
            reactApplicationContext,
            sessionKey = sessionKey,
            hmacSecret = hmacSecret,
            sentryKey = sentryKey
        )
    }

    @ReactMethod
    fun getCertificateStatus(promise: Promise) {
        val status = EasyPayConfiguration.getInstance().getRsaCertificateFetchingStatus()
            ?: RsaCertificateStatus.LOADING
        val s = when (status) {
            RsaCertificateStatus.LOADING -> "loading"
            RsaCertificateStatus.FAILED -> "failed"
            RsaCertificateStatus.SUCCESS -> "success"
            else -> {
                "unknown"
            }
        }
        promise.resolve(s)
    }

    //region UI Widgets
    @ReactMethod
    fun pay(
        config: ReadableMap,
        user: ReadableMap,
        payment: ReadableMap,
        address: ReadableMap) {
        reactApplicationContext.currentActivity?.let {
            it.runOnUiThread {
                val a  = it as MainActivity
                a.reactContext = reactApplicationContext
                val paymentSheet = a.paymentSheet
                val consentCreator = ConsentCreatorParam(
                    limitLifeTime = payment.getString("limitLifetime")!!.toDouble(),
                    limitPerCharge = payment.getString("limitPerCharge")!!.toDouble(),
                    merchantId = config.getString("merchantId")!!.toInt(),
                    startDate = Date(),
                    customerReferenceId = config.getString("customerReferenceId")!!,
                    rpguid = config.getString("rpguid")!!
                )
                val billingAddress = EndCustomerBillingAddressParam (
                    address1 = address.getString("endCustomerAddress1")!!,
                    address2 = address.getString("endCustomerAddress2"),
                    city = address.getString("endCustomerCity"),
                    state = address.getString("endCustomerState"),
                    zip = address.getString("endCustomerZip") ?: ""
                )
                val endCustomerDataParam = EndCustomerDataParam(
                    firstName = user.getString("endCustomerFirstName"),
                    lastName = user.getString("endCustomerLastName"),
                    billingAddress = billingAddress,
                )

                val c = PaymentSheet.Configuration
                    .Builder()
                    .setAmounts(AmountsParam(payment.getString("amount")!!.toDouble()))
                    .setPreselectedCardId(payment.getString("cardId")?.toInt())
                    .setEndCustomer(endCustomerDataParam)
                    .setConsentCreator(consentCreator)
                    .build()

                paymentSheet.present(c)
            }
        }
    }

    @ReactMethod
    fun manageAndSelect(
        config: ReadableMap,
        user: ReadableMap,
        payment: ReadableMap,
        address: ReadableMap) {
        reactApplicationContext.currentActivity?.let {
            it.runOnUiThread {
                val a  = it as MainActivity
                a.reactContext = reactApplicationContext
                val customerSheet = a.customerSheet
                val consentCreator = ConsentCreatorParam(
                    limitLifeTime = payment.getString("limitLifetime")!!.toDouble(),
                    limitPerCharge = payment.getString("limitPerCharge")!!.toDouble(),
                    merchantId = config.getString("merchantId")!!.toInt(),
                    startDate = Date(),
                    customerReferenceId = config.getString("customerReferenceId")!!,
                    rpguid = config.getString("rpguid")!!
                )
                val billingAddress = EndCustomerBillingAddressParam (
                    address1 = address.getString("endCustomerAddress1")!!,
                    address2 = address.getString("endCustomerAddress2"),
                    city = address.getString("endCustomerCity"),
                    state = address.getString("endCustomerState"),
                    zip = address.getString("endCustomerZip") ?: ""
                )
                val endCustomerDataParam = EndCustomerDataParam(
                    firstName = user.getString("endCustomerFirstName"),
                    lastName = user.getString("endCustomerLastName"),
                    billingAddress = billingAddress,
                )

                val c = CustomerSheet.Configuration
                    .Builder()
                    .setPreselectedCardId(payment.getString("cardId")?.toInt())
                    .setEndCustomer(endCustomerDataParam)
                    .setConsentCreator(consentCreator)
                    .build()

                customerSheet.present(c)
            }
        }
    }

    //endregion
}