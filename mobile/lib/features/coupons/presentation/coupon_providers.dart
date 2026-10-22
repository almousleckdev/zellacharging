import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/coupon_repository_impl.dart';
import '../domain/coupon.dart';
import '../domain/coupon_repository.dart';

final couponRepositoryProvider = Provider<CouponRepository>(
  (ref) => CouponRepositoryImpl(ref.watch(dioProvider)),
);

final couponsProvider = FutureProvider.autoDispose<List<Coupon>>(
  (ref) => ref.watch(couponRepositoryProvider).mine(),
);
