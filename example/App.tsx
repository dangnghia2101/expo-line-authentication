import { Button, StyleSheet, Text, View } from 'react-native';

import * as ExpoLineModule from 'expo-line-authentication';
import { LoginPermission } from 'expo-line-authentication/ExpoLineModule.types';
import { useEffect } from 'react';

export default function App() {
  useEffect(() => {
    ExpoLineModule.setup({channelID: '2006494257'})
  }, [])

  const login = async () => {
    const result = await ExpoLineModule.login({
      scopes: ['email', 'openid', 'profile'] as LoginPermission[]
    })
    console.log(result)
  }

  const logout = () => {
    ExpoLineModule.logout()
  }

  const getBotFriendshipStatus = async() => {
   const resp = await ExpoLineModule.getBotFriendshipStatus()
   console.log(resp)
  }

  const getCurrentAccessToken = async() => {
    const resp = await ExpoLineModule.getCurrentAccessToken()
    console.log(resp)
  }

  const getProfile = async() => {
    const resp = await  ExpoLineModule.getProfile()
    console.log(resp)
  } 
  
  const refreshToken = async() => {
    const resp = await ExpoLineModule.refreshToken()
    console.log(resp)
  }

  const verifyAccessToken = async() => {
    const resp = await ExpoLineModule.verifyAccessToken()
    console.log(resp)
  }

  return (
    <View style={styles.container}>
      <Button title='login' onPress={login}  />
      <Button title='getBotFriendshipStatus' onPress={getBotFriendshipStatus} />
      <Button title='getCurrentAccessToken' onPress={getCurrentAccessToken} />
      <Button title='getProfile' onPress={getProfile} />
      <Button title='logout' onPress={logout} />
      <Button title='refreshToken' onPress={refreshToken} />
      <Button title='verifyAccessToken' onPress={verifyAccessToken} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12
  },
});
