import 'coupon.dart';

abstract class CouponRepository {
  Future<List<Coupon>> mine();
}
