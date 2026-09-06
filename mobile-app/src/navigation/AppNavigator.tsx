import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from '../screens/HomeScreen';
import DetailScreen from '../screens/DetailScreen';
import MapScreen from '../screens/MapScreen';
import OwnerLoginScreen from '../screens/OwnerLoginScreen';
import MyPropertiesScreen from '../screens/MyPropertiesScreen';
import PropertyFormScreen from '../screens/PropertyFormScreen';
import { Property } from '../types';
import { colors } from '../styles/common';

export type RootStackParamList = {
  Home: undefined;
  Detail: { property: Property };
  Map: undefined;
  OwnerLogin: undefined;
  MyProperties: { ownerId: number };
  PropertyForm: { ownerId: number; property?: Property };
};

const Stack = createStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{
          headerStyle: { backgroundColor: colors.background },
          headerTintColor: colors.textPrimary,
          headerTitleStyle: { fontSize: 12, fontWeight: '800', letterSpacing: 1.1 },
          headerShadowVisible: false,
        }}
      >
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{ title: 'ATLAS RENTALS' }}
        />
        <Stack.Screen
          name="Detail"
          component={DetailScreen}
          options={{ title: 'HOME DETAILS' }}
        />
        <Stack.Screen
          name="Map"
          component={MapScreen}
          options={{ title: 'NEIGHBORHOOD MAP' }}
        />
        <Stack.Screen name="OwnerLogin" component={OwnerLoginScreen} options={{ title: 'OWNER LOGIN' }} />
        <Stack.Screen name="MyProperties" component={MyPropertiesScreen} options={{ title: 'MY PROPERTIES' }} />
        <Stack.Screen name="PropertyForm" component={PropertyFormScreen} options={{ title: 'PROPERTY FORM' }} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}