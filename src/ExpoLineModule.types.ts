export type ChangeEventPayload = {
  value: string;
};

export type ExpoLineModuleViewProps = {
  name: string;
};

export type ExpoLineLoginProps = {
  scopes?: LoginPermission[]
  onlyWebLogin?: boolean
  botPrompt?: BotPrompt
}

export enum LoginPermission {
  EMAIL = 'email',
  /// The permission to get an ID token in the login response.
  OPEN_ID = 'openid',
  /// The permission to get the user's profile including the user ID, display name, and the profile image
  /// URL in the login response.
  PROFILE = 'profile',
}

export enum BotPrompt {
  AGGRESSIVE = 'aggressive',
  NORMAL = 'normal',
}