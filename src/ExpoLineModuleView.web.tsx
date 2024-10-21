import * as React from 'react';

import { ExpoLineModuleViewProps } from './ExpoLineModule.types';

export default function ExpoLineModuleView(props: ExpoLineModuleViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}
