class TopupResult {
  const TopupResult({required this.id, required this.state, this.failureCode});

  final String id;
  final String state;
  final String? failureCode;

  bool get succeeded => state == 'SUCCEEDED';

  bool get failed => state == 'FAILED';
}
