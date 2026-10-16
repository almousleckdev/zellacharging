import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../../wallet/presentation/wallet_providers.dart';
import '../data/session_repository_impl.dart';
import '../domain/charging_session.dart';
import '../domain/session_repository.dart';
import '../domain/start_charge.dart';

const _pollInterval = Duration(seconds: 5);

final sessionRepositoryProvider = Provider<SessionRepository>(
  (ref) => SessionRepositoryImpl(ref.watch(dioProvider)),
);

final startChargeProvider = Provider<StartCharge>(
  (ref) => StartCharge(
    ref.watch(walletRepositoryProvider),
    ref.watch(sessionRepositoryProvider),
  ),
);

final activeSessionProvider = FutureProvider.autoDispose<ChargingSession?>(
  (ref) => ref.watch(sessionRepositoryProvider).active(),
);

final historyProvider = FutureProvider.autoDispose<List<ChargingSession>>(
  (ref) => ref.watch(sessionRepositoryProvider).history(),
);

final liveSessionProvider = StreamProvider.autoDispose
    .family<ChargingSession, String>((ref, id) async* {
      final repository = ref.watch(sessionRepositoryProvider);
      while (true) {
        yield await repository.get(id);
        await Future<void>.delayed(_pollInterval);
      }
    });
