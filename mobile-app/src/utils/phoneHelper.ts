import { Linking } from 'react-native';

export const callOwner = (phoneNumber: string) => {
  Linking.openURL(`tel:${phoneNumber}`);
};

export const whatsappOwner = (phoneNumber: string) => {
  Linking.openURL(`whatsapp://send?phone=${phoneNumber}`);
}; 
