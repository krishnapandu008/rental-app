import { StyleSheet } from 'react-native';
import { colors, spacing, borderRadius } from '../styles/common';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  mapStage: {
    flex: 1,
    minHeight: 240,
  },
  map: {
    flex: 1,
  },
  bottomSheet: {
    minHeight: 142,
    maxHeight: 188,
    paddingTop: spacing.sm,
    paddingBottom: spacing.sm,
    backgroundColor: colors.background,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
  sheetTitle: {
    color: colors.textPrimary,
    fontSize: 14,
    fontWeight: '800',
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.xs,
  },
  mapSummary: {
    color: colors.textTertiary,
    fontSize: 11,
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.sm,
  },
  loading: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
    backgroundColor: colors.background,
  },
  loadingText: {
    marginTop: spacing.md,
    color: colors.textSecondary,
  },
  errorText: {
    color: colors.textSecondary,
    textAlign: 'center',
    marginBottom: spacing.md,
  },
  retryButton: {
    backgroundColor: colors.primary,
    borderRadius: borderRadius.full,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  retryButtonText: {
    color: colors.surfaceLight,
    fontWeight: '700',
  },
  emptyOverlay: {
    position: 'absolute',
    top: spacing.lg,
    left: spacing.lg,
    right: spacing.lg,
    padding: spacing.md,
    borderRadius: 18,
    backgroundColor: 'rgba(252, 250, 246, 0.96)',
  },
  emptyText: {
    color: colors.textSecondary,
    textAlign: 'center',
  },
  statusOverlay: {
    position: 'absolute',
    top: spacing.md,
    left: spacing.md,
    right: spacing.md,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: borderRadius.full,
    backgroundColor: 'rgba(16, 42, 67, 0.92)',
  },
  statusTitle: {
    color: colors.surfaceLight,
    textAlign: 'center',
    fontSize: 12,
    fontWeight: '700',
  },
  listOverlay: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xs,
  },
  listCard: {
    width: 190,
    padding: spacing.md,
    borderRadius: 16,
    backgroundColor: 'rgba(252, 250, 246, 0.96)',
    borderWidth: 1,
    borderColor: '#E9E1D7',
  },
  listCardTitle: {
    color: colors.textPrimary,
    fontWeight: '700',
    fontSize: 12,
  },
  listCardLocation: {
    color: colors.textSecondary,
    fontSize: 11,
    marginTop: 4,
  },
  listCardPrice: {
    color: colors.primaryDark,
    fontWeight: '800',
    fontSize: 12,
    marginTop: 6,
  },
  callout: {
    minWidth: 180,
    padding: spacing.xs,
  },
  calloutTitle: {
    color: colors.textPrimary,
    fontWeight: '700',
    marginBottom: 4,
  },
  calloutLocation: {
    color: colors.textSecondary,
    fontSize: 12,
    marginBottom: 4,
  },
  calloutPrice: {
    color: colors.primaryDark,
    fontWeight: '700',
  },
  calloutHint: {
    color: colors.textTertiary,
    fontSize: 11,
    marginTop: spacing.xs,
  },
});