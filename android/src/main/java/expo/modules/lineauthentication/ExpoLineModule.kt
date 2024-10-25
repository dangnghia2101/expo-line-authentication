package expo.modules.lineauthentication

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineAuthenticationConfig
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginResult
import com.linecorp.linesdk.*
import com.linecorp.linesdk.LineProfile

import android.app.Activity
import android.content.Context
import android.content.Intent
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginProps : Record {
  @Field
  val scopes: List<String>? = null

  @Field
  val onlyWebLogin: Boolean = false

  @Field
  val botPrompt: String? = null
}

class ExpoLineModule : Module() {
  private var LOGIN_REQUEST_CODE: Int = 0
  private var loginResult: Promise? = null
  private lateinit var channelId: String
  private lateinit var context: Context
  private lateinit var lineApiClient: LineApiClient
  private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

  companion object {
    private const val ERROR_MESSAGE: String = "ERROR"
  }

  private val currentActivity
    get() = appContext.currentActivity ?: throw Exceptions.MissingActivity()

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoLineModule')` in JavaScript.
    Name("ExpoLineModule")

    OnCreate {
      context = appContext.reactContext ?: throw Exceptions.ReactContextLost()
      channelId = context.getString(R.string.line_channel_id)
      lineApiClient = LineApiClientBuilder(context, channelId).build()
    }

    OnActivityResult { _, (requestCode, resultCode, data) ->
      handleActivityResult(requestCode, resultCode, data)
    }
    // Enables the module to be used as a native view. Definition components that are accepted as part of
    // the view definition: Prop, Events.
    View(ExpoLineModuleView::class) {
      // Defines a setter for the `name` prop.
      Prop("name") { view: ExpoLineModuleView, prop: String ->
        println(prop)
      }
    }

    AsyncFunction("login") {
      options: LoginProps,
      promise: Promise
       ->
      val scopesFormat = if (options.scopes.isNullOrEmpty()) listOf("profile") else options.scopes.toList()
      val onlyWebLoginFormat = options.onlyWebLogin.let { options.onlyWebLogin}
      val botPromptString = if (options.botPrompt.isNullOrEmpty()) "normal" else options.botPrompt
      login(
        scopesFormat,
        onlyWebLoginFormat,
        botPromptString,
        promise
      )
    }

    AsyncFunction("logout") {
        promise: Promise
      ->
      uiCoroutineScope.launch {
        val lineApiResponse = withContext(Dispatchers.IO) { lineApiClient.logout() }
        if (lineApiResponse.isSuccess) {
          promise.resolve(null)
        } else {
          promise.reject(
            lineApiResponse.responseCode.name,
            lineApiResponse.errorData.message,
            null
          )
        }
      }
    }

    AsyncFunction("getProfile") {
        promise: Promise
      ->
      uiCoroutineScope.launch {
        val lineApiResponse = withContext(Dispatchers.IO) { lineApiClient.profile }
        if (!lineApiResponse.isSuccess) {
          promise.reject(
            lineApiResponse.responseCode.name,
            lineApiResponse.errorData.message,
            null
          )
        } else {
          promise.resolve(parseProfile(lineApiResponse.responseData))
        }
      }
    }

    AsyncFunction("getCurrentAccessToken") {
      promise: Promise
      -> invokeLineServiceMethod(
        promise = promise,
        serviceCallable = { lineApiClient.currentAccessToken },
        parser = { parseAccessToken(it, lineIdToken = null) }
      )
    }

    AsyncFunction("getBotFriendshipStatus") {
        promise: Promise
      -> invokeLineServiceMethod(
        promise = promise,
        serviceCallable = { lineApiClient.friendshipStatus },
        parser = { parseFriendshipStatus(it) }
      )
    }

    AsyncFunction("refreshToken") {
        promise: Promise
      -> invokeLineServiceMethod(
        promise = promise,
        serviceCallable = { lineApiClient.refreshAccessToken() },
        parser = { parseAccessToken(it, lineIdToken = null) }
      )
    }

    AsyncFunction("verifyAccessToken") {
        promise: Promise
      -> invokeLineServiceMethod(
      promise = promise,
      serviceCallable = { lineApiClient.verifyToken() },
      parser = { parseVerifyAccessToken(it) }
    )
    }
  }

    private fun <T> invokeLineServiceMethod(
      promise: Promise,
      serviceCallable: () -> LineApiResponse<T>,
      parser: (T) ->  Map<String, Any?>
    ) {
      uiCoroutineScope.launch {
        val lineApiResponse = withContext(Dispatchers.IO) { serviceCallable.invoke() }
        if (lineApiResponse.isSuccess) {
          promise.resolve(parser.invoke(lineApiResponse.responseData))
        } else {
          promise.reject(
            lineApiResponse.responseCode.name,
            lineApiResponse.errorData.message,
            null
          )
        }
      }
    }

    // Helpers
    private fun createLineAuthenticationConfig(
      channelId: String,
      onlyWebLogin: Boolean
    ): LineAuthenticationConfig? {
        return createConfig(
                channelId,
                onlyWebLogin
        )
    }

    private fun createConfig(
      channelId: String,
      isLineAppAuthDisabled: Boolean
    ): LineAuthenticationConfig {
      val configBuilder = LineAuthenticationConfig.Builder(channelId)

      if (isLineAppAuthDisabled) {
          configBuilder.disableLineAppAuthentication()
      }

      return configBuilder.build()
    }

    private fun login(
      scopes: List<String>,
      onlyWebLogin: Boolean,
      botPromptString: String,
      promise: Promise
    ) {
      val lineAuthenticationParams = LineAuthenticationParams.Builder()
              .scopes(Scope.convertToScopeList(scopes))
              .apply {
                  botPrompt(LineAuthenticationParams.BotPrompt.valueOf(botPromptString))
              }
              .build()

      val lineAuthenticationConfig: LineAuthenticationConfig? =
              createLineAuthenticationConfig(channelId, onlyWebLogin)

      val activity: Activity = currentActivity

      val loginIntent =
              when {
                  lineAuthenticationConfig != null -> LineLoginApi.getLoginIntent(
                          activity,
                          lineAuthenticationConfig,
                          lineAuthenticationParams
                  )
                  onlyWebLogin -> LineLoginApi.getLoginIntentWithoutLineAppAuth(
                          activity, channelId, lineAuthenticationParams)
                  else -> LineLoginApi.getLoginIntent(activity, channelId, lineAuthenticationParams)
              }

      activity.startActivityForResult(loginIntent, LOGIN_REQUEST_CODE)
      this.loginResult = promise
    }

  private fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    if (requestCode != LOGIN_REQUEST_CODE) return

    if (resultCode != Activity.RESULT_OK || intent == null) {
      loginResult?.reject(
        resultCode.toString(),
        ERROR_MESSAGE,
        null
      )
    }

    val result = LineLoginApi.getLoginResultFromIntent(intent)

    when (result.responseCode) {
      LineApiResponseCode.SUCCESS -> {
        loginResult?.resolve(parseLoginResult(result))
        loginResult = null
      }
      LineApiResponseCode.CANCEL -> {
        loginResult?.reject(
          result.responseCode.name,
          result.errorData.message,
          null
        )
      }
      else -> {
        loginResult?.reject(
          result.responseCode.name,
          result.errorData.message,
          null
        )
      }
    }

    loginResult = null
  }

  // Parsers
  private fun parseAccessToken(accessToken: LineAccessToken, lineIdToken: LineIdToken?): Map<String, Any?> =
    mapOf(
      "access_token" to accessToken.tokenString,
      "expires_in" to accessToken.expiresInMillis,
      "id_token" to lineIdToken?.rawString
    )

  private fun parseFriendshipStatus(friendshipStatus: LineFriendshipStatus): Map<String, Any?> =
    mapOf(
      "friendFlag" to friendshipStatus.isFriend
    )

  private fun parseProfile(profile: LineProfile): Map<String, Any?> =
    mapOf(
      "displayName" to profile.displayName,
      "userID" to profile.userId,
      "statusMessage" to profile.statusMessage,
      "pictureURL" to profile.pictureUrl?.toString()
    )

  private fun parseLoginResult(loginResult: LineLoginResult): Map<String, Any?> =
    mapOf(
      "userProfile" to parseProfile(loginResult.lineProfile!!),
      "accessToken" to parseAccessToken(loginResult.lineCredential!!.accessToken, loginResult.lineIdToken),
      "scope" to loginResult.lineCredential?.scopes?.let {
        Scope.join(it)
      },
      "friendshipStatusChanged" to loginResult.friendshipStatusChanged,
      "IDTokenNonce" to loginResult.lineIdToken?.nonce
    )

  private fun parseVerifyAccessToken( verifyAccessToken: LineCredential):  Map<String, Any?> =
    mapOf(
      "client_id" to channelId,
      "scope" to Scope.join(verifyAccessToken.scopes),
      "expires_in" to verifyAccessToken.accessToken.expiresInMillis
    )
}
