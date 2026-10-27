import 'wallet_entry.dart';

class WalletEntryDetail {
  const WalletEntryDetail({
    required this.entry,
    required this.balanceAfterMinor,
    this.sessionId,
    this.operator,
    this.msisdn,
    this.state,
    this.reference,
    this.providerReference,
  });

  final WalletEntry entry;
  final int balanceAfterMinor;
  final String? sessionId;
  final String? operator;
  final String? msisdn;
  final String? state;
  final String? reference;
  final String? providerReference;

  bool get hasMobileMoney => operator != null;

  bool get inProgress =>
      state == 'PENDING' || state == 'REQUESTED' || state == 'PROCESSING';

  bool get needsReview => state == 'REVIEW';

  bool get failed => state == 'FAILED' || state == 'EXPIRED';
}
