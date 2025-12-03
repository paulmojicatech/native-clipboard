import Foundation

@objc public class NativeClipboard: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
