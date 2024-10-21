import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { ExpoLineModuleViewProps } from './ExpoLineModule.types';

const NativeView: React.ComponentType<ExpoLineModuleViewProps> =
  requireNativeViewManager('ExpoLineModule');

export default function ExpoLineModuleView(props: ExpoLineModuleViewProps) {
  return <NativeView {...props} />;
}
