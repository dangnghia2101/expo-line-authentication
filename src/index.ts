import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoLineModule.web.ts
// and on native platforms to ExpoLineModule.ts
import ExpoLineModule from './ExpoLineModule';
import ExpoLineModuleView from './ExpoLineModuleView';
import { ChangeEventPayload, LineLoginProps, ExpoLineModuleViewProps, SetupLineLoginProps } from './ExpoLineModule.types';


export async function login(args: LineLoginProps = {}) {
  return await ExpoLineModule.login(args);
}

export function logout() {
  return ExpoLineModule.logout();
}

export async function refreshToken() {
  return await ExpoLineModule.refreshToken();
}

export async function getBotFriendshipStatus() {
  return await ExpoLineModule.getBotFriendshipStatus();
}

export async function getCurrentAccessToken() {
  return await ExpoLineModule.getCurrentAccessToken();
}

export async function getProfile() {
  return await ExpoLineModule.getProfile();
}

export async function verifyAccessToken() {
  return await ExpoLineModule.verifyAccessToken();
}

export async function setup(args: SetupLineLoginProps) {
  return await ExpoLineModule.setup(args);
}

const emitter = new EventEmitter(ExpoLineModule ?? NativeModulesProxy.ExpoLineModule);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoLineModuleView, ExpoLineModuleViewProps, ChangeEventPayload, LineLoginProps};
