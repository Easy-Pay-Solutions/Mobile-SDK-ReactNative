//
//  RNEventEmitter.h
//  EasyPayRN
//
//
//

@objc(RNEventEmitter)
open class RNEventEmitter: RCTEventEmitter {

  public static var emitter: RCTEventEmitter! // global variable

  // constructor
  override init(){
    super.init()
    RNEventEmitter.emitter = self
  }

  open override func supportedEvents() -> [String] {
    ["onCardSelected", 
    "onCardDeleted", 
    "onCardSaved", 
    "onCardPaid", 
    /*fix Android  module*/
    "onManageError",
    "onManageResult",
    "onPaymentError",
    "onPaymentCancelled",
    "onPaymentResult"]  // etc.
  }
}
