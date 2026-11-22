import 'package:dio/dio.dart';
import 'package:flutter_riverpod/misc.dart' show Override;
import 'package:zella/features/free_sunday/domain/free_sunday_offer.dart';
import 'package:zella/features/free_sunday/domain/free_sunday_window.dart';
import 'package:zella/features/free_sunday/presentation/free_sunday_providers.dart';

import 'ui_fakes.dart';

final endsAt = DateTime.utc(2026, 11, 22, 21);

const noonToTen = FreeSundayWindow(
  day: 'SUNDAY',
  from: '12:00',
  until: '22:00',
  endOfDay: false,
);

final availableOffer = FreeSundayOffer(
  available: true,
  reason: 'ELIGIBLE',
  endsAt: endsAt,
  window: noonToTen,
);

FakeFreeSunday availableFake() => FakeFreeSunday(availableOffer);

FakeFreeSunday reasonFake(String reason) =>
    FakeFreeSunday(FreeSundayOffer(available: false, reason: reason));

DioException rateLimited() => DioException(
  requestOptions: RequestOptions(path: '/v1/free-sunday'),
  response: Response(
    requestOptions: RequestOptions(path: '/v1/free-sunday'),
    statusCode: 429,
  ),
  type: DioExceptionType.badResponse,
);

List<Override> clockAt(DateTime now) => [
  freeSundayClockProvider.overrideWithValue(() => now),
];
