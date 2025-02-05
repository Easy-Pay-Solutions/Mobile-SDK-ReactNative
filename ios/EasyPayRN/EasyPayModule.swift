//
//  EasyPayModule.swift
//  
//
//  
//

import Foundation
import EasyPay

@objc(EasyPayModule) class EasyPayModule: NSObject, CardSelectionDelegate, CardPaymentDelegate {
  
  //MARK: - React Native
  static func requiresMainQueueSetup() -> Bool {
      return true
    }
  
  private func getWindow() -> UIWindow? {
    if #available(iOS 15.0, *) {
      return (UIApplication.shared.connectedScenes.first as? UIWindowScene)?.keyWindow
    } else if #available(iOS 13.0, *) {
      return (UIApplication.shared.connectedScenes.first as? UIWindowScene)?.windows.first
    } else  {
      return UIApplication.shared.keyWindow
    }
  }
  
  //MARK: - Public methods
  @objc func configureSecrets(_ apiKey: String,
                              hmacSecret: String,
                              sentryKey: String?,
                              isProduction: Bool) {
    EasyPay.shared.configureSecrets(apiKey: apiKey,
                                    hmacSecret: hmacSecret,
                                    sentryKey: sentryKey,
                                    isProduction: isProduction)
  }
  
  @objc func getCertificateStatus(_ callback: RCTResponseSenderBlock) {
    let certificateStatus = EasyPay.shared.certificateStatus
    switch certificateStatus {
    case .success:
      callback(["success"]);
    case .loading:
      callback(["loading"]);
    case .failed:
      callback(["failed"]);
    case nil:
      callback(["unknown"]);
    }
  }
  
  @objc func loadCertificate() {
    EasyPay.shared.loadCertificate { _ in
        // do nothing , callback will be supported in next version of native SDK
    }
  }
  
  @objc func manageAndSelect(_ config: [String: String],
                             user: [String: String],
                             payment: [String: Any],
                             address: [String: String],
                             resolve: @escaping RCTPromiseResolveBlock,
                             reject: @escaping RCTPromiseRejectBlock) {
    let rpguid = config["rpguid"]
    let customerReferenceId = config["customerReferenceId"]
    let merchantId = config["merchantId"]
    
    let endCustomerFirstName = user["endCustomerFirstName"]
    let endCustomerLastName = user["endCustomerLastName"]
    
    let cardId = payment["cardId"] as? Int
    let limitPerCharge = payment["limitPerCharge"] as! String
    let limitLifetime = payment["limitLifetime"] as! String
    
    let endCustomerAddress1 = address["endCustomerAddress1"]
    let endCustomerAddress2 = address["endCustomerAddress2"]
    let endCustomerCity = address["endCustomerCity"]
    let endCustomerState = address["endCustomerState"]
    let endCustomerZip = address["endCustomerZip"]
    
    let vm = AddAnnualConsentWidgetModel.init(merchantId: merchantId!,
                                              endCustomerFirstName: endCustomerFirstName,
                                              endCustomerLastName: endCustomerLastName,
                                              endCustomerAddress1: endCustomerAddress1,
                                              endCustomerAddress2: endCustomerAddress2,
                                              endCustomerCity: endCustomerCity,
                                              endCustomerState: endCustomerState,
                                              endCustomerZip: endCustomerZip,
                                              customerReferenceId: customerReferenceId,
                                              rpguid: rpguid,
                                              limitPerCharge: limitPerCharge,
                                              limitLifetime: limitLifetime)
    DispatchQueue.main.async {
      guard let vc = try? CardSelectionViewController(selectionDelegate: self,
                                                      preselectedCardId: cardId,
                                                      paymentDetails: vm) else {
        reject("failure", "failed to instantiate view controller", nil)
        return
      }
      guard let w = self.getWindow() else {
        reject("failure", "window is nil", nil)
        return
      }
      
      w.rootViewController?.present(vc,
                                    animated: true,
                                    completion: nil)
      resolve(1)
    }
  }
  @objc func pay(_ config: [String: String],
                 user: [String: String],
                 payment: [String: Any],
                 address: [String: String],
                 resolve: @escaping RCTPromiseResolveBlock,
                 reject: @escaping RCTPromiseRejectBlock) {
    let rpguid = config["rpguid"]
    let customerReferenceId = config["customerReferenceId"]
    let merchantId = config["merchantId"]
    
    let endCustomerFirstName = user["endCustomerFirstName"]
    let endCustomerLastName = user["endCustomerLastName"]
    
    let cardId = payment["cardId"] as? Int
    let amount = payment["amount"] as! String
    let limitPerCharge = payment["limitPerCharge"] as! String
    let limitLifetime = payment["limitLifetime"] as! String
    
    let endCustomerAddress1 = address["endCustomerAddress1"]
    let endCustomerAddress2 = address["endCustomerAddress2"]
    let endCustomerCity = address["endCustomerCity"]
    let endCustomerState = address["endCustomerState"]
    let endCustomerZip = address["endCustomerZip"]
    
    let vm = AddAnnualConsentWidgetModel.init(merchantId: merchantId!,
                                              endCustomerFirstName: endCustomerFirstName,
                                              endCustomerLastName: endCustomerLastName,
                                              endCustomerAddress1: endCustomerAddress1,
                                              endCustomerAddress2: endCustomerAddress2,
                                              endCustomerCity: endCustomerCity,
                                              endCustomerState: endCustomerState,
                                              endCustomerZip: endCustomerZip,
                                              customerReferenceId: customerReferenceId,
                                              rpguid: rpguid,
                                              limitPerCharge: limitPerCharge,
                                              limitLifetime: limitLifetime)
    DispatchQueue.main.async {
      guard let vc = try? CardSelectionViewController(amount: amount,
                                                      paymentDelegate: self,
                                                      preselectedCardId: cardId,
                                                      paymentDetails: vm) else {
        reject("failure", "failed to instantiate view controller", nil)
        return
      }
      guard let w = self.getWindow() else {
        reject("failure", "window is nil", nil)
        return
      }
      
      w.rootViewController?.present(vc,
                                    animated: true,
                                    completion: nil)
      resolve(1)
    }
  }
  
  //MARK: - CardSelectionDelegate
  
  func didSelectCard(consentId: String) {
    RNEventEmitter.emitter.sendEvent(withName: "onCardSelected",
                                     body: ["consentId": consentId])
    
  }
  
  func didDeleteCard(consentId: Int, success: Bool) {
    if success {
      RNEventEmitter.emitter.sendEvent(withName: "onCardDeleted",
                                       body: ["consentId": consentId])
    }
  }
  
  func didSaveCard(consentId: Int?,
                   expMonth: Int?,
                   expYear: Int?,
                   last4digits: String?,
                   success: Bool) {
    if !success {
      return
    }
    guard let cid = consentId else { return }
    var body: [String: Any] = ["consentId": cid]
    
    if let month = expMonth {
      body["expMonth"] = month
    }
    if let year = expYear {
      body["expYear"] = year
    }
    if let digits = last4digits {
      body["last4digits"] = digits
    }
    
    RNEventEmitter.emitter.sendEvent(withName: "onCardSaved",
                                         body: body)
  }
  
  //MARK: - CardPaymentDelegate
  
  func didPayWithCard(consentId: Int?,
                      paymentData: PaymentData?,
                      success: Bool) {
    if !success {
      return
    }
    
    //guard let cid = consentId else { return }
    var cid = 0
    if (consentId != nil) {
      cid = consentId ?? 0
    }
    
    let body: [String: Any?] = ["consentId": cid,
                               "functionOk": paymentData?.functionOk == true,
                               "txApproved": paymentData?.txApproved == true,
                               "responseMessage": paymentData?.responseMessage,
                               "errorMessage": paymentData?.errorMessage,
                               "errorCode": paymentData?.errorCode,
                               "txnCode": paymentData?.txnCode,
                               "avsResult": paymentData?.avsResult,
                               "cvvResult": paymentData?.cvvResult,
                               "acquirerResponseEMV": paymentData?.acquirerResponseEMV,
                               "txId": paymentData?.txId,
                               "requiresVoiceAuth": paymentData?.requiresVoiceAuth == true,
                               "isPartialApproval": paymentData?.isPartialApproval == true,
                               "responseAuthorizedAmount": paymentData?.responseAuthorizedAmount,
                               "responseApprovedAmount": paymentData?.responseApprovedAmount
    ]
    
    RNEventEmitter.emitter.sendEvent(withName: "onCardPaid",
                                         body: body.compactMapValues({ $0 }))
    
  }
//  func didDeleteCard(consentId: Int, success: Bool) {}
}
