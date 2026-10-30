class WithdrawalResult {
  const WithdrawalResult({
    required this.id,
    required this.state,
    this.failureCode,
  });

  final String id;
  final String state;
  final String? failureCode;

  bool get awaitingApproval => state == 'PENDING_APPROVAL';

  bool get paid => state == 'PAID';

  bool get failed => state == 'FAILED';

  bool get underReview => state == 'REVIEW';

  bool get settled => paid || failed || underReview;
}
