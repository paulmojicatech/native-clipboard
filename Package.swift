// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PaulmojicatechNativeClipboard",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "PaulmojicatechNativeClipboard",
            targets: ["NativeClipboardPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "NativeClipboardPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NativeClipboardPlugin"),
        .testTarget(
            name: "NativeClipboardPluginTests",
            dependencies: ["NativeClipboardPlugin"],
            path: "ios/Tests/NativeClipboardPluginTests")
    ]
)