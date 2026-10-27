abstract final class PhoneMask {
  static String of(String? phone) {
    final value = (phone ?? '').replaceAll(RegExp(r'\s'), '');
    if (value.length < 8) return value;
    final hidden = '•' * (value.length - 8);
    return '${value.substring(0, 6)}$hidden${value.substring(value.length - 2)}';
  }
}
