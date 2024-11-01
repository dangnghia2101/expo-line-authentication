# ![React Native LINE](/assets/github-banner.png)

<p align="center">
  Line SDK wrapper for EXPO (IOS, ANDROID) 🚀
</p>

This library includes:

- [LINE SDK v5 for iOS Swift](https://developers.line.biz/en/docs/ios-sdk/swift/overview/), wrapped with [Swift](https://developer.apple.com/swift/).
- [LINE SDK v5 for Android](https://developers.line.biz/en/docs/android-sdk/overview/), wrapped with [Kotlin](https://kotlinlang.org/).

## Installation

First, install the npm package with yarn. _Autolink_ is automatic.

```bash
  npx expo install expo-line-authentication
```

### iOS Setup

1. Follow instructions in [Integrating LINE Login with your iOS app](https://developers.line.biz/en/docs/ios-sdk/swift/integrate-line-login/).

### Android Setup

1. In your manifest add `xmlns:tools="http://schemas.android.com/tools"` in your `manifest` tag and also `tools:replace="android:allowBackup"` in your `application` tag

## API

First, require the `LineLogin` module:

```javascript
import LineLogin from 'expo-line-authentication'
```

Then, you can start using all the functions that are available:

| Function                                                 | Description                                                                                                                                                                                                                        |
| -------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `login(args?: LoginArguments): Promise<LoginResult>`     | Starts the login flow of Line's SDK (Opens the apps if it's installed and defaults to the browser otherwise). It accepts the same argumements as the LineSDK, in an object `{ key: value }`, defaults the same way as LineSDK too. |
| `getCurrentAccessToken(): Promise<AccessToken>`          | Returns the current access token for the currently logged in user.                                                                                                                                                                 |
| `getProfile(): Promise<UserProfile>`                     | Returns the profile of the currently logged in user.                                                                                                                                                                               |
| `logout(): Promise<void>`                                | Logs out the currently logged in user.                                                                                                                                                                                             |
| `refreshToken(): Promise<AccessToken>`                   | Refreshes the access token and returns it.                                                                                                                                                                                         |
| `verifyAccessToken(): Promise<AccessTokenVerifyResult>`  | Verifies the access token and returns it.                                                                                                                                                                                          |
| `getBotFriendshipStatus(): Promise<BotFriendshipStatus>` | Gets bot friendship status if [configured](https://developers.line.biz/en/docs/ios-sdk/swift/link-a-bot/).                                                                                                                         |

### Return values

The following objects are returned on the methods described above:

1. UserProfile:

```typescript
{
   /// The user ID of the current authorized user.
  userID: String

  /// The display name of the current authorized user.
  displayName: string

  /// The profile image URL of the current authorized user. `null` if the user has not set a profile
  /// image.
  pictureURL?: string

  /// The status message of the current authorized user. `null` if the user has not set a status message.
  statusMessage?: string
}
```

2. AccessToken:

```typescript
{
   /// The value of the access token.
  access_token: String
  /// The expiration time of the access token. It is calculated using `createdAt` and the validity period
  /// of the access token. This value might not be the actual expiration time because this value depends
  /// on the system time of the device when `createdAt` is determined.
  expires_in: String
  /// The raw string value of the ID token bound to the access token. The value exists only if the access token
  /// is obtained with the `.openID` permission.
  id_token?: String
}
```

3. AccessTokenVerifyResult:

```typescript
{
  // The channel ID bound to the access token.
  client_id: String

  /// The amount of time until the access token expires.
  expires_in: String

  /// Valid permissions of the access token separated by spaces
  scope: String
}
```

4. LoginResult

```typescript
{
   /// The access token obtained by the login process.
  accessToken: AccessToken
  /// The permissions bound to the `accessToken` object by the authorization process. Scope has them separated by spaces
  scope: String
  /// Contains the user profile including the user ID, display name, and so on. The value exists only when the
  /// `.profile` permission is set in the authorization request.
  userProfile?: UserProfile
  /// Indicates that the friendship status between the user and the bot changed during the login. This value is
  /// non-`null` only if the `.botPromptNormal` or `.botPromptAggressive` are specified as part of the
  /// `LoginManagerOption` object when the user logs in. For more information, see Linking a bot with your LINE
  /// Login channel at https://developers.line.me/en/docs/line-login/web/link-a-bot/.
  friendshipStatusChanged?: boolean
  /// The `nonce` value when requesting ID Token during login process. Use this value as a parameter when you
  /// verify the ID Token against the LINE server. This value is `null` if `.openID` permission is not requested.
  IDTokenNonce?: String
}
```

5. BotFriendshipStatus

```typescript
{
  friendFlag: boolean
}
```

### Arguments

1. LoginArguments

```typescript
{
  scopes?: LoginPermission[]
  onlyWebLogin?: boolean
  botPrompt?: BotPrompt
}
```

2. LoginPermission

```typescript
{
  EMAIL = 'email',
  /// The permission to get an ID token in the login response.
  OPEN_ID = 'openid',

  /// The permission to get the user's profile including the user ID, display name, and the profile image
  /// URL in the login response.
  PROFILE = 'profile',
}
```

3. BotPrompt

```typescript
{
  aggressive = 'aggressive',
  normal = 'normal',
}
```

## Usage

1. Login with default values:

```typescript
    try {
        ...
        const loginResult = await Line.login()
        ...
    } catch (error) {
        ...
    }
```

2. Login with arguments:

```typescript
    try {
        ...
        const loginResult = await Line.login({
          scopes: ['email', 'profile'],
          botPrompt: 'normal'
        })
        ...
    } catch (error) {
        ...
    }
```

3. Get user profile:

```typescript
    try {
        ...
        const profile = await Line.getProfile()
        ...
    } catch (error) {
        ...
    }
```

3. Logout

```typescript
    try {
        ...
        await Line.logout()
        ...
    } catch (error) {
        ...
    }
```

## Example

If you want to see `expo-line-authentication` in action, just move into the [example](/example) folder and run `npx expo run:ios`/`npx expo run:android`. By seeing its source code, you will have a better understanding of the library usage.


<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

## License

`expo-line-authentication` is available under the MIT license. See the LICENCE file for more info.
