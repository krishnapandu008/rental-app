import { StyleSheet } from 'react-native';
import { colors, spacing, shadows, borderRadius, typography } from '../styles/common';

export const styles = StyleSheet.create({
  container: {
    padding: spacing.lg,
    paddingTop: spacing.sm,
    backgroundColor: colors.background,
    flexGrow: 1,
  },
  headerSection: {
    marginBottom: spacing.xl,
    marginTop: spacing.lg,
  },
  title: {
    fontSize: 30,
    lineHeight: 35,
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
    backgroundColor: '#102A43',
    paddingVertical: spacing.lg,
    paddingHorizontal: spacing.lg,
    borderRadius: 20,
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
    color: '#F4C95D',
  },
  infoCard: {
    backgroundColor: colors.surfaceLight,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#E9E1D7',
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
    flexShrink: 1,
  },
  contactCard: {
    backgroundColor: '#D9F3E8',
    borderRadius: 18,
    padding: spacing.lg,
    marginBottom: spacing.xl,
    ...shadows.md,
  },
  contactLabel: {
    fontSize: 12,
    color: colors.secondaryDark,
    opacity: 0.9,
    marginBottom: spacing.sm,
  },
  contactNumber: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.secondaryDark,
  },
  buttonRow: {
    flexDirection: 'row',
    gap: spacing.md,
    marginTop: spacing.md,
    paddingHorizontal: spacing.sm,
  },
  buttonRowCompact: {
    flexDirection: 'column',
  },
  callBtn: {
    flex: 1,
    backgroundColor: colors.primary,
    paddingVertical: spacing.lg,
    borderRadius: borderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    ...shadows.md,
  },
  whatsappBtn: {
    flex: 1,
    backgroundColor: colors.whatsapp,
    paddingVertical: spacing.lg,
    borderRadius: borderRadius.full,
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