import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/coupon.dart';
import '../domain/coupon_repository.dart';

class CouponRepositoryImpl implements CouponRepository {
  CouponRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<List<Coupon>> mine() async {
    try {
      final response = await _client.get<List<dynamic>>('/v1/coupons');
      return [
        for (final item in response.data!)
          _coupon(item as Map<String, dynamic>),
      ];
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }

  Coupon _coupon(Map<String, dynamic> json) {
    final expires = json['expiresAt'] as String?;
    return Coupon(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String?,
      expiresAt: expires == null ? null : DateTime.parse(expires),
    );
  }
}
