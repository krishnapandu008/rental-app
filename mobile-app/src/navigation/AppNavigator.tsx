import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from '../screens/HomeScreen';
import DetailScreen from '../screens/DetailScreen';
import { Property } from '../types';

export type RootStackParamList = {
  Home: undefined;
  Detail: { property: Property };
};

const Stack = createStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen name="Home" component={HomeScreen} options={{ title: 'Rental Finder' }} />
        <Stack.Screen name="Detail" component={DetailScreen} options={{ title: 'Property Details' }} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}