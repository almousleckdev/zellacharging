import 'package:country_picker/country_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../core/format/phone_number.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import 'auth_field.dart';

class PhoneField extends StatelessWidget {
  const PhoneField({
    super.key,
    required this.controller,
    required this.country,
    required this.onCountryChanged,
  });

  final TextEditingController controller;
  final Country country;
  final ValueChanged<Country> onCountryChanged;

  void _pick(BuildContext context) {
    final theme = Theme.of(context);
    showCountryPicker(
      context: context,
      showPhoneCode: true,
      favorite: [PhoneNumber.defaultRegion],
      onSelect: onCountryChanged,
      countryListTheme: CountryListThemeData(
        backgroundColor: AppColors.surface,
        borderRadius: const BorderRadius.vertical(
          top: Radius.circular(AppSpacing.radiusLarge),
        ),
        bottomSheetHeight: MediaQuery.sizeOf(context).height * 0.8,
        flagSize: 24,
        textStyle: theme.textTheme.bodyLarge?.copyWith(
          color: AppColors.textPrimary,
        ),
        searchTextStyle: theme.textTheme.bodyLarge?.copyWith(
          color: AppColors.textPrimary,
        ),
        inputDecoration: InputDecoration(
          hintText: AuthText.searchCountry,
          hintStyle: const TextStyle(color: AppColors.textSecondary),
          prefixIcon: const Icon(
            AppIcons.search,
            color: AppColors.textSecondary,
          ),
          filled: true,
          fillColor: AppColors.surfaceMuted,
          contentPadding: const EdgeInsets.symmetric(vertical: 12),
          border: _border(AppColors.border),
          enabledBorder: _border(AppColors.border),
          focusedBorder: _border(AppColors.primary),
        ),
      ),
    );
  }

  static OutlineInputBorder _border(Color color) => OutlineInputBorder(
    borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
    borderSide: BorderSide(color: color),
  );

  @override
  Widget build(BuildContext context) {
    return AuthField(
      controller: controller,
      hint: AuthText.phoneNumberLabel,
      icon: AppIcons.phone,
      validator: PhoneNumber.validate,
      keyboardType: TextInputType.phone,
      autofillHints: const [AutofillHints.telephoneNumberNational],
      inputFormatters: [FilteringTextInputFormatter.allow(RegExp(r'[0-9 ]'))],
      prefix: _CountryButton(country: country, onTap: () => _pick(context)),
    );
  }
}

class _CountryButton extends StatelessWidget {
  const _CountryButton({required this.country, required this.onTap});

  final Country country;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: AppSpacing.sm),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(country.flagEmoji, style: const TextStyle(fontSize: 20)),
            const SizedBox(width: AppSpacing.xs),
            Text(
              '+${country.phoneCode}',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w600,
              ),
            ),
            const Icon(
              AppIcons.caretDown,
              size: 16,
              color: AppColors.textSecondary,
            ),
          ],
        ),
      ),
    );
  }
}
