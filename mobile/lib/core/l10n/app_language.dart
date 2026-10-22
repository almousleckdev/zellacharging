import 'package:flutter/widgets.dart';

enum AppLanguage {
  fr('fr', 'Français'),
  en('en', 'English');

  const AppLanguage(this.code, this.label);

  final String code;
  final String label;

  static AppLanguage current = AppLanguage.fr;

  static String pick({required String fr, required String en}) =>
      current == AppLanguage.fr ? fr : en;

  static AppLanguage fromCode(String? code) => AppLanguage.values.firstWhere(
    (language) => language.code == code,
    orElse: () => AppLanguage.fr,
  );

  Locale get locale => Locale(code);
}
