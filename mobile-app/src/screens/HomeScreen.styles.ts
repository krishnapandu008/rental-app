import { StyleSheet } from 'react-native';
import { colors, spacing } from '../styles/common';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: spacing.lg,
    backgroundColor: colors.background,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  searchInput: {
    borderWidth: 1,
    borderColor: colors.lightGray,
    borderRadius: 8,
    padding: spacing.md,
    marginBottom: spacing.md,
    backgroundColor: colors.white,
  },
  card: {
    backgroundColor: colors.white,
    borderRadius: 8,
    padding: spacing.lg,
    marginBottom: spacing.md,
    elevation: 2,
    shadowColor: colors.cardShadow,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: spacing.xs,
  },
  rent: {
    fontSize: 16,
    fontWeight: 'bold',
    color: colors.secondary,
    marginTop: spacing.xs,
  },
  buttonRow: {
    flexDirection: 'row',
    marginTop: spacing.md,
  },
  callBtn: {
    backgroundColor: colors.secondary,
    padding: spacing.md,
    borderRadius: 6,
    marginRight: spacing.md,
    flex: 1,
  },
  whatsappBtn: {
    backgroundColor: colors.whatsapp,
    padding: spacing.md,
    borderRadius: 6,
    flex: 1,
  },
  btnText: {
    color: colors.white,
    textAlign: 'center',
    fontWeight: 'bold',
  },
});