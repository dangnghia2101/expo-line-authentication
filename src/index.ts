import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoLineModule.web.ts
// and on native platforms to ExpoLineModule.ts
import ExpoLineModule from './ExpoLineModule';
import ExpoLineModuleView from './ExpoLineModuleView';
import { ChangeEventPayload, ExpoLineModuleViewProps } from './ExpoLineModule.types';

// Get the native constant value.
export const PI = ExpoLineModule.PI;

export function hello(): string {
  return ExpoLineModule.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoLineModule.setValueAsync(value);
}

const emitter = new EventEmitter(ExpoLineModule ?? NativeModulesProxy.ExpoLineModule);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoLineModuleView, ExpoLineModuleViewProps, ChangeEventPayload };
