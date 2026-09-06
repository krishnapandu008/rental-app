import React from 'react';
import { LogBox } from 'react-native';
import AppNavigator from './src/navigation/AppNavigator';

LogBox.ignoreLogs(['InteractionManager has been deprecated']);

export default function App() {
  return <AppNavigator />;
}