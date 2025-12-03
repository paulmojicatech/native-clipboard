import Foundation
import UIKit
import WebKit

@objc public class NativeClipboard: NSObject {
    private var longPressGesture: UILongPressGestureRecognizer?
    private var enabledActions: [String: Bool] = [
        "copy": true,
        "paste": true,
        "cut": true,
        "selectAll": true
    ]
    
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
    
    @objc public func setupGestureRecognizer(
        for webView: WKWebView,
        target: Any,
        action: Selector,
        enableCopy: Bool,
        enablePaste: Bool,
        enableCut: Bool,
        enableSelectAll: Bool
    ) {
        // Store enabled actions
        enabledActions["copy"] = enableCopy
        enabledActions["paste"] = enablePaste
        enabledActions["cut"] = enableCut
        enabledActions["selectAll"] = enableSelectAll
        
        // Remove existing gesture if any
        if let existingGesture = longPressGesture {
            webView.scrollView.removeGestureRecognizer(existingGesture)
        }
        
        // Create and add long press gesture
        let gesture = UILongPressGestureRecognizer(target: target, action: action)
        gesture.minimumPressDuration = 0.5
        gesture.delegate = self as? UIGestureRecognizerDelegate
        
        webView.scrollView.addGestureRecognizer(gesture)
        longPressGesture = gesture
    }
    
    @objc public func removeGestureRecognizer(from webView: WKWebView) {
        if let gesture = longPressGesture {
            webView.scrollView.removeGestureRecognizer(gesture)
            longPressGesture = nil
        }
    }
    
    @objc public func isActionEnabled(_ action: String) -> Bool {
        return enabledActions[action] ?? false
    }
}

