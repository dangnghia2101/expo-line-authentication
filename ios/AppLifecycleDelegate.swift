import ExpoModulesCore
import LineSDK

public class AppLifecycleDelegate: ExpoAppDelegateSubscriber {

  public func application(
    _ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]
  ) -> Bool {
    return LoginManager.shared.application(app, open: url, options: options)
  }
}
