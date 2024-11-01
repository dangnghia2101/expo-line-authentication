import ExpoModulesCore

internal final class FailedParsing: GenericException<String> {
  override var reason: String {
    "There was an error when parsing \(param)"
  }
}

internal final class FaileddLine: GenericException<String?> {
  override var reason: String {
    param ?? "There was an error with LineSDK"
  }
}

internal class LoginArgsException: Exception {
  override var reason: String {
    "Passed props cannot be nil"
  }
}
