import ExpoModulesCore
import LineSDK

struct LoginProps: Record {
  @Field
  var scopes: [String]?

  @Field
  var onlyWebLogin: Bool?

  @Field
  var botPrompt: String?
}

struct SetupProps: Record {
  @Field
  var channelID: String

  @Field
  var universalLinkURL: URL?
}

public class ExpoLineModule: Module {
  public func definition() -> ModuleDefinition {

    Name("ExpoLineModule")

    // Enables the module to be used as a native view. Definition components that are accepted as part of the
    // view definition: Prop, Events.
    View(ExpoLineModuleView.self) {
      // Defines a setter for the `name` prop.
      Prop("name") { (view: ExpoLineModuleView, prop: String) in
        print(prop)
      }
    }

    Function("setup") { (options: SetupProps) in
      return LoginManager.shared.setup(
        channelID: options.channelID, universalLinkURL: options.universalLinkURL)
    }

    AsyncFunction("login", login)
    Function("logout", logout)
    AsyncFunction("getProfile", getProfile)
    AsyncFunction("getCurrentAccessToken", getCurrentAccessToken)
    AsyncFunction("refreshToken", refreshToken)
    AsyncFunction("verifyAccessToken", verifyAccessToken)
    AsyncFunction("getBotFriendshipStatus", getBotFriendshipStatus)
  }

  private func login(options: LoginProps?, promise: Promise) {
    guard let args = options else {
      promise.reject(LoginArgsException())
      return
    }

    let scopes =
      (options?.scopes as? [String])?.map { LoginPermission(rawValue: $0) } ?? [.profile]
    let onlyWebLogin = (args.onlyWebLogin) ?? false
    var parameters: LoginManager.Parameters = LoginManager.Parameters.init()

    if onlyWebLogin { parameters.onlyWebLogin = onlyWebLogin }

    if let botPrompt = args.botPrompt {
      switch botPrompt {
      case "aggressive": parameters.botPromptStyle = LoginManager.BotPrompt(rawValue: "aggresive")
      case "normal": parameters.botPromptStyle = LoginManager.BotPrompt(rawValue: "normal")
      default: break
      }
    }

    DispatchQueue.main.async {
      LoginManager.shared.login(
        permissions: Set(scopes),
        in: nil,
        parameters: parameters
      ) { result in
        switch result {
        case .success(let value):
          self.resolver(value, promise: promise, name: "login")
        case .failure(let error):
          promise.reject(FaileddLine(error.errorDescription))
        }
      }
    }
  }

  private func logout(promise: Promise) {
    LoginManager.shared.logout { result in
      switch result {
      case .success: promise.resolve(nil)
      case .failure(let error): promise.reject(FaileddLine(error.errorDescription))

      }
    }
  }

  private func getCurrentAccessToken(promise: Promise) {
    if let token = AccessTokenStore.shared.current {
      resolver(token, promise: promise, name: "current access token")
    } else {
      promise.reject(FaileddLine("There isn't an access token available"))
    }
  }

  private func getProfile(promise: Promise) {
    API.getProfile { result in
      switch result {
      case .success(let profile):
        self.resolver(profile, promise: promise, name: "profile")
      case .failure(let error):
        promise.reject(FaileddLine(error.errorDescription))
      }
    }
  }

  private func refreshToken(promise: Promise) {
    API.Auth.refreshAccessToken { result in
      switch result {
      case .success(let token):
        self.resolver(token, promise: promise, name: "refresh token")
      case .failure(let error):
        promise.reject(FaileddLine(error.errorDescription))
      }
    }
  }

  private func verifyAccessToken(promise: Promise) {
    API.Auth.verifyAccessToken { result in
      switch result {
      case .success(let token):
        self.resolver(token, promise: promise, name: "verify access token")
      case .failure(let error):
        promise.reject(FaileddLine(error.errorDescription))
      }
    }
  }

  private func getBotFriendshipStatus(promise: Promise) {
    API.getBotFriendshipStatus { result in
      switch result {
      case .success(let value):
        self.resolver(value, promise: promise, name: "friend ship status")
      case .failure(let error):
        promise.reject(FaileddLine(error.errorDescription))
      }
    }
  }

  func toJSON<T: Encodable>(_ object: T) throws -> Any {
    let data = try JSONEncoder().encode(object)
    return try JSONSerialization.jsonObject(with: data, options: [])
  }

  func resolver<T: Encodable>(_ object: T, promise: Promise, name: String) {
    do {
      let jsonValue = try toJSON(object)
      promise.resolve(jsonValue)
    } catch {
      promise.reject(FailedParsing(name))
    }
  }

}
