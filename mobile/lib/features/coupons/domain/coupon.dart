class Coupon {
  const Coupon({
    required this.id,
    required this.title,
    this.description,
    this.expiresAt,
  });

  final String id;
  final String title;
  final String? description;
  final DateTime? expiresAt;
}
