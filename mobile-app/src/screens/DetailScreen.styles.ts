import { StyleSheet } from 'react-native';
import { colors, spacing } from '../styles/common';

export const styles = StyleSheet.create({
  container: {
    padding: spacing.lg,
    backgroundColor: colors.white,
    flexGrow: 1,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: spacing.xs,
  },
  description: {
    fontSize: 14,
    color: colors.gray,
    marginBottom: spacing.xl,
  },
  infoCard: {
    backgroundColor: '#f9f9f9',
    borderRadius: 12,
    padding: spacing.lg,
    marginBottom: spacing.xl,
  },
  label: {
    fontWeight: '600',
    color: colors.black,
    marginTop: spacing.md,
  },
  value: {
    fontSize: 16,
    color: colors.black,
    marginTop: spacing.xs,
  },
  buttonRow: {
    flexDirection: 'row',
    marginTop: spacing.md,
    gap: spacing.md,
  },
  callBtn: {
    flex: 1,
    backgroundColor: colors.secondary,
    paddingVertical: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
  },
  whatsappBtn: {
    flex: 1,
    backgroundColor: colors.whatsapp,
    paddingVertical: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
  },
  btnText: {
    color: colors.white,
    fontSize: 16,
    fontWeight: '600',
  },
});