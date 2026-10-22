import '../l10n/app_language.dart';

abstract final class DateFormatter {
  static String dateTime(DateTime value) {
    final local = value.toLocal();
    return '${date(local)} ${time(local)}';
  }

  static String date(DateTime value) {
    final local = value.toLocal();
    final day = _two(local.day);
    final month = _two(local.month);
    return AppLanguage.pick(
      fr: '$day/$month/${local.year}',
      en: '${local.year}-$month-$day',
    );
  }

  static String time(DateTime value) {
    final local = value.toLocal();
    return '${_two(local.hour)}:${_two(local.minute)}';
  }

  static String _two(int value) => value.toString().padLeft(2, '0');
}
