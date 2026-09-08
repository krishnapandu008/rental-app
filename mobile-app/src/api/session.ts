import AsyncStorage from '@react-native-async-storage/async-storage';
import type { LoginResponse } from './ownerApi';

const SESSION_KEY = '@rental-finder/owner-session';

export const saveOwnerSession = async (owner: LoginResponse) => {
  await AsyncStorage.setItem(SESSION_KEY, JSON.stringify(owner));
};

export const getOwnerSession = async () => {
  const stored = await AsyncStorage.getItem(SESSION_KEY);
  return stored ? JSON.parse(stored) as LoginResponse : null;
};

export const clearOwnerSession = async () => {
  await AsyncStorage.removeItem(SESSION_KEY);
};
