import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from '../screens/HomeScreen';
import DetailScreen from '../screens/DetailScreen';
import MapScreen from '../screens/MapScreen';
import OwnerLoginScreen from '../screens/OwnerLoginScreen';
import OwnerRegisterScreen from '../screens/OwnerRegisterScreen';
import MyPropertiesScreen from '../screens/MyPropertiesScreen';
import PropertyFormScreen from '../screens/PropertyFormScreen';
import OwnerProfileScreen from '../screens/OwnerProfileScreen';
import OwnerFavoritesScreen from '../screens/OwnerFavoritesScreen';
import OwnerInquiriesScreen from '../screens/OwnerInquiriesScreen';
import AdminPanelScreen from '../screens/AdminPanelScreen';
import NotificationsScreen from '../screens/NotificationsScreen';
import { Property } from '../types';
import { colors } from '../styles/common';

export type RootStackParamList = {
  Home: undefined;
  Detail: { property: Property; ownerId?: number };
  Map: undefined;
  OwnerLogin: undefined;
  OwnerRegister: undefined;
  MyProperties: { ownerId: number };
  PropertyForm: { ownerId: number; property?: Property };
  OwnerProfile: undefined;
  OwnerFavorites: undefined;
  OwnerInquiries: undefined;
  AdminPanel: undefined;
  Notifications: undefined;
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
        <Stack.Screen name="OwnerRegister" component={OwnerRegisterScreen} options={{ title: 'OWNER REGISTRATION' }} />
        <Stack.Screen name="MyProperties" component={MyPropertiesScreen} options={{ title: 'MY PROPERTIES' }} />
        <Stack.Screen name="PropertyForm" component={PropertyFormScreen} options={{ title: 'PROPERTY FORM' }} />
        <Stack.Screen name="OwnerProfile" component={OwnerProfileScreen} options={{ title: 'PROFILE' }} />
        <Stack.Screen name="OwnerFavorites" component={OwnerFavoritesScreen} options={{ title: 'FAVORITES' }} />
        <Stack.Screen name="OwnerInquiries" component={OwnerInquiriesScreen} options={{ title: 'INQUIRIES' }} />
        <Stack.Screen name="AdminPanel" component={AdminPanelScreen} options={{ title: 'ADMIN PANEL' }} />
        <Stack.Screen name="Notifications" component={NotificationsScreen} options={{ title: 'NOTIFICATIONS' }} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}