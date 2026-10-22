import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_spacing.dart';

abstract final class AppTheme {
  static ThemeData light() {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.primary,
      primary: AppColors.primary,
      surface: AppColors.surface,
    ).copyWith(surfaceTint: Colors.transparent);
    final textTheme = _compactText(
      Typography.blackMountainView.apply(
        bodyColor: AppColors.textPrimary,
        displayColor: AppColors.textPrimary,
      ),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: AppColors.background,
      canvasColor: AppColors.background,
      textTheme: textTheme,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.background,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: true,
        foregroundColor: AppColors.textPrimary,
        titleTextStyle: textTheme.titleLarge?.copyWith(fontSize: 17),
      ),
      inputDecorationTheme: _inputs(),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.primary,
          minimumSize: const Size(0, 40),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
          ),
        ),
      ),
      switchTheme: SwitchThemeData(
        thumbColor: const WidgetStatePropertyAll(AppColors.onPrimary),
        trackColor: WidgetStateProperty.resolveWith(
          (states) => states.contains(WidgetState.selected)
              ? AppColors.primary
              : AppColors.border,
        ),
        trackOutlineColor: const WidgetStatePropertyAll(Colors.transparent),
      ),
      checkboxTheme: CheckboxThemeData(
        fillColor: WidgetStateProperty.resolveWith(
          (states) => states.contains(WidgetState.selected)
              ? AppColors.primary
              : Colors.transparent,
        ),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: AppColors.primary,
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.border,
        space: 1,
        thickness: 1,
      ),
    );
  }

  static TextTheme _compactText(TextTheme base) {
    TextStyle? size(TextStyle? style, double value) =>
        style?.copyWith(fontSize: value, fontWeight: FontWeight.w400);
    return base.copyWith(
      displayLarge: size(base.displayLarge, 30),
      displayMedium: size(base.displayMedium, 28),
      displaySmall: size(base.displaySmall, 26),
      headlineLarge: size(base.headlineLarge, 24),
      headlineMedium: size(base.headlineMedium, 22),
      headlineSmall: size(base.headlineSmall, 20),
      titleLarge: size(base.titleLarge, 18),
      titleMedium: size(base.titleMedium, 15),
      titleSmall: size(base.titleSmall, 14),
      bodyLarge: size(base.bodyLarge, 15),
      bodyMedium: size(base.bodyMedium, 14),
      bodySmall: size(base.bodySmall, 12),
      labelLarge: size(base.labelLarge, 14),
      labelMedium: size(base.labelMedium, 12),
      labelSmall: size(base.labelSmall, 11),
    );
  }

  static InputDecorationTheme _inputs() {
    OutlineInputBorder border(Color color, [double width = 1]) =>
        OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
          borderSide: BorderSide(color: color, width: width),
        );
    return InputDecorationTheme(
      filled: true,
      fillColor: AppColors.surface,
      hintStyle: const TextStyle(color: AppColors.neutral),
      border: border(AppColors.border),
      enabledBorder: border(AppColors.border),
      focusedBorder: border(AppColors.primary, 1.5),
      errorBorder: border(AppColors.danger),
      focusedErrorBorder: border(AppColors.danger, 1.5),
      contentPadding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: 12,
      ),
    );
  }
}
