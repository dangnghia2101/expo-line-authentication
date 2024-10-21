import { StyleSheet, Text, View } from 'react-native';

import * as ExpoLineModule from 'expo-line-authentication';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{ExpoLineModule.hello()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
