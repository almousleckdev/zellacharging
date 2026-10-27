import '../domain/session_quote.dart';

abstract final class SessionQuoteMapper {
  static SessionQuote fromJson(Map<String, dynamic> json) => SessionQuote(
    amountMinor: (json['amountMinor'] as num).toInt(),
    currency: json['currency'] as String,
    decimals: (json['decimals'] as num).toInt(),
    availableMinor: (json['availableMinor'] as num).toInt(),
    shortfallMinor: (json['shortfallMinor'] as num).toInt(),
    minimumMinor: (json['minimumMinor'] as num).toInt(),
    maximumMinor: (json['maximumMinor'] as num).toInt(),
    vatPercent: (json['vatPercent'] as num?)?.toDouble() ?? 0,
    serviceFeePercent: (json['serviceFeePercent'] as num?)?.toDouble() ?? 0,
    estimatedEnergyKwh: (json['estimatedEnergyKwh'] as num?)?.toDouble(),
  );
}
