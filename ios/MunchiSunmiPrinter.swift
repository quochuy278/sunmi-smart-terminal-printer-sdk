import Foundation

#if canImport(React)
import React
#elseif canImport(React_Core)
import React_Core
#endif

@objc(MunchiSunmiPrinter)
class MunchiSunmiPrinter: NSObject {
  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }

  @objc(initialize:rejecter:)
  func initialize(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }

  @objc(prepare:rejecter:)
  func prepare(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }

  @objc(print:resolver:rejecter:)
  func print(
    _ job: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }

  @objc(getDeviceInfo:rejecter:)
  func getDeviceInfo(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    resolve([
      "manufacturer": "apple",
      "brand": "apple",
      "model": "ios",
      "sdkInt": 0
    ])
  }

  @objc(getPrinterStatus:rejecter:)
  func getPrinterStatus(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    resolve([
      "online": false,
      "paperEmpty": false,
      "coverOpen": false,
      "errorCode": "UNSUPPORTED_PLATFORM",
      "errorMessage": "The SUNMI printer bridge is not supported on this platform.",
      "nativeStatusCode": NSNull()
    ])
  }

  @objc(printText:options:resolver:rejecter:)
  func printText(
    _ text: String,
    options: NSDictionary?,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }

  @objc(cutPaper:rejecter:)
  func cutPaper(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }

  @objc(lineWrap:resolver:rejecter:)
  func lineWrap(
    _ lines: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    reject("IOS_NOT_SUPPORTED", "SUNMI printer is not supported on iOS", nil)
  }
}
