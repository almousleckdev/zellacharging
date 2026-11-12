import '../format/money_format.dart';
import 'app_language.dart';
import 'error_text.dart';

abstract final class SuspensionText {
  static const _currency = 'XAF';
  static const _decimals = 0;

  static String signedOut(int? owedMinor) => owedMinor == null
      ? ErrorText.byCode['ACCOUNT_SUSPENDED']!
      : owing(owedMinor);

  static String owing(int owedMinor) {
    final amount = MoneyFormat.format(owedMinor, _currency, _decimals);
    return AppLanguage.pick(
      fr:
          'Votre compte est suspendu : $amount de loyer NDZela sont impayés. '
          'Réglez-les pour réactiver votre compte.',
      en:
          'Your account is suspended: $amount of NDZela rent is unpaid. '
          'Pay it to reactivate your account.',
    );
  }
}
