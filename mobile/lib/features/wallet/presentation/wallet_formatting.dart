import '../../../core/format/money_format.dart';
import '../domain/wallet.dart';

extension WalletFormatting on Wallet {
  String get availableText =>
      MoneyFormat.format(availableMinor, currency, decimals);

  String get reservedText =>
      MoneyFormat.format(reservedMinor, currency, decimals);
}
