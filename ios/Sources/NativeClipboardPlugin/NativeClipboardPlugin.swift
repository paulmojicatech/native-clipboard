import Foundation
import Capacitor
import UIKit
import WebKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(NativeClipboardPlugin)
public class NativeClipboardPlugin: CAPPlugin, CAPBridgedPlugin, UIGestureRecognizerDelegate {
    public let identifier = "NativeClipboardPlugin"
    public let jsName = "NativeClipboard"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "enableContextMenu", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disableContextMenu", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = NativeClipboard()
    private var isContextMenuEnabled = false

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func enableContextMenu(_ call: CAPPluginCall) {
        let enableCopy = call.getBool("enableCopy") ?? true
        let enablePaste = call.getBool("enablePaste") ?? true
        let enableCut = call.getBool("enableCut") ?? true
        let enableSelectAll = call.getBool("enableSelectAll") ?? true
        
        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                  let webView = self.bridge?.webView else {
                call.reject("WebView not available")
                return
            }
            
            self.implementation.setupGestureRecognizer(
                for: webView,
                target: self,
                action: #selector(self.handleLongPress(_:)),
                enableCopy: enableCopy,
                enablePaste: enablePaste,
                enableCut: enableCut,
                enableSelectAll: enableSelectAll
            )
            
            self.isContextMenuEnabled = true
            call.resolve()
        }
    }
    
    @objc func disableContextMenu(_ call: CAPPluginCall) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                  let webView = self.bridge?.webView else {
                call.reject("WebView not available")
                return
            }
            
            self.implementation.removeGestureRecognizer(from: webView)
            self.isContextMenuEnabled = false
            call.resolve()
        }
    }
    
    @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
        guard gesture.state == .began,
              let webView = bridge?.webView else {
            return
        }
        
        let point = gesture.location(in: webView)
        
        // Get selected text from WebView
        let js = "window.getSelection().toString();"
        webView.evaluateJavaScript(js) { [weak self] result, error in
            guard let self = self else { return }
            
            let selectedText = result as? String ?? ""
            
            DispatchQueue.main.async {
                self.showNativeContextMenu(at: point, in: webView, selectedText: selectedText)
            }
        }
    }
    
    private func showNativeContextMenu(at point: CGPoint, in view: UIView, selectedText: String) {
        let hasSelection = !selectedText.isEmpty
        
        var actions: [UIAction] = []
        
        // Copy action
        if hasSelection && implementation.isActionEnabled("copy") {
            let copyAction = UIAction(title: "Copy", image: UIImage(systemName: "doc.on.doc")) { [weak self] _ in
                UIPasteboard.general.string = selectedText
                self?.notifyListeners("clipboardMenuAction", data: [
                    "action": "copy",
                    "selectedText": selectedText
                ])
            }
            actions.append(copyAction)
        }
        
        // Cut action
        if hasSelection && implementation.isActionEnabled("cut") {
            let cutAction = UIAction(title: "Cut", image: UIImage(systemName: "scissors")) { [weak self] _ in
                UIPasteboard.general.string = selectedText
                self?.notifyListeners("clipboardMenuAction", data: [
                    "action": "cut",
                    "selectedText": selectedText
                ])
            }
            actions.append(cutAction)
        }
        
        // Paste action
        if implementation.isActionEnabled("paste"), let clipboardText = UIPasteboard.general.string {
            let pasteAction = UIAction(title: "Paste", image: UIImage(systemName: "doc.on.clipboard")) { [weak self] _ in
                self?.notifyListeners("clipboardMenuAction", data: [
                    "action": "paste",
                    "text": clipboardText
                ])
            }
            actions.append(pasteAction)
        }
        
        // Select All action
        if implementation.isActionEnabled("selectAll") {
            let selectAllAction = UIAction(title: "Select All", image: UIImage(systemName: "selection.pin.in.out")) { [weak self] _ in
                self?.bridge?.webView?.evaluateJavaScript("document.execCommand('selectAll');", completionHandler: nil)
                self?.notifyListeners("clipboardMenuAction", data: [
                    "action": "selectAll"
                ])
            }
            actions.append(selectAllAction)
        }
        
        guard !actions.isEmpty else { return }
        
        let menu = UIMenu(children: actions)
        
        if #available(iOS 16.0, *) {
            let config = UIEditMenuConfiguration(identifier: nil, sourcePoint: point)
            UIEditMenuInteraction.shared(for: view).presentEditMenu(with: config)
        } else if #available(iOS 13.0, *) {
            // Fallback for iOS 13-15
            let menuController = UIMenuController.shared
            menuController.showMenu(from: view, rect: CGRect(origin: point, size: .zero))
        }
    }
    
    // Allow gesture to work alongside other gestures
    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}

