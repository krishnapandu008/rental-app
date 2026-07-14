import { StyleSheet } from 'react-native';
import { colors, spacing, shadows, borderRadius, typography } from '../styles/common';

export const styles = StyleSheet.create({
  container: {
    padding: spacing.lg,
    backgroundColor: colors.background,
    flexGrow: 1,
  },
  headerSection: {
    marginBottom: spacing.xl,
    marginTop: spacing.lg,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    marginBottom: spacing.md,
    color: colors.textPrimary,
  },
  description: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: spacing.lg,
    lineHeight: 20,
  },
  priceContainer: {
    backgroundColor: colors.primary,
    paddingVertical: spacing.lg,
    paddingHorizontal: spacing.lg,
    borderRadius: borderRadius.lg,
    marginBottom: spacing.lg,
    ...shadows.md,
  },
  priceLabel: {
    fontSize: 12,
    color: colors.surfaceLight,
    opacity: 0.9,
    marginBottom: spacing.xs,
  },
  priceValue: {
    fontSize: 32,
    fontWeight: '700',
    color: colors.surfaceLight,
  },
  infoCard: {
    backgroundColor: colors.surfaceLight,
    borderRadius: borderRadius.lg,
    padding: spacing.lg,
    marginBottom: spacing.xl,
    ...shadows.md,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  infoRowLast: {
    borderBottomWidth: 0,
  },
  infoIcon: {
    fontSize: 20,
    marginRight: spacing.md,
    width: 24,
  },
  infoContent: {
    flex: 1,
  },
  label: {
    fontWeight: '600',
    color: colors.textSecondary,
    marginBottom: spacing.xs,
    fontSize: 12,
  },
  value: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.textPrimary,
  },
  contactCard: {
    backgroundColor: colors.secondary,
    borderRadius: borderRadius.lg,
    padding: spacing.lg,
    marginBottom: spacing.xl,
    ...shadows.md,
  },
  contactLabel: {
    fontSize: 12,
    color: colors.surfaceLight,
    opacity: 0.9,
    marginBottom: spacing.sm,
  },
  contactNumber: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.surfaceLight,
  },
  buttonRow: {
    flexDirection: 'row',
    gap: spacing.md,
    marginTop: spacing.md,
    paddingHorizontal: spacing.sm,
  },
  callBtn: {
    flex: 1,
    backgroundColor: colors.primary,
    paddingVertical: spacing.lg,
    borderRadius: borderRadius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...shadows.md,
  },
  whatsappBtn: {
    flex: 1,
    backgroundColor: colors.whatsapp,
    paddingVertical: spacing.lg,
    borderRadius: borderRadius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...shadows.md,
  },
  btnText: {
    color: colors.surfaceLight,
    fontSize: 16,
    fontWeight: '700',
  },
});