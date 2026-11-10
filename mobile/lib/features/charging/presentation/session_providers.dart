import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../../wallet/presentation/wallet_providers.dart';
import '../data/session_repository_impl.dart';
import '../domain/charging_session.dart';
import '../domain/quote_request.dart';
import '../domain/session_quote.dart';
import '../domain/session_repository.dart';
import '../domain/start_charge.dart';

const _pollInterval = Duration(seconds: 2);
const _quoteDebounce = Duration(milliseconds: 400);
const _settleDelay = Duration(seconds: 3);

final sessionRepositoryProvider = Provider<SessionRepository>(
  (ref) => SessionRepositoryImpl(ref.watch(dioProvider)),
);

final startChargeProvider = Provider<StartCharge>(
  (ref) => StartCharge(
    ref.watch(walletRepositoryProvider),
    ref.watch(sessionRepositoryProvider),
  ),
);

final quoteProvider = FutureProvider.autoDispose
    .family<SessionQuote?, QuoteRequest>((ref, request) async {
      var cancelled = false;
      ref.onDispose(() => cancelled = true);
      await Future<void>.delayed(_quoteDebounce);
      if (cancelled) return null;
      return ref
          .read(sessionRepositoryProvider)
          .quote(request.stationId, request.connectorId, request.amountMinor);
    });

final activeSessionProvider = FutureProvider.autoDispose<ChargingSession?>(
  (ref) => ref.watch(sessionRepositoryProvider).active(),
);

final historyProvider = FutureProvider.autoDispose<List<ChargingSession>>(
  (ref) => ref.watch(sessionRepositoryProvider).history(),
);

final sessionByIdProvider = FutureProvider.autoDispose
    .family<ChargingSession, String>(
      (ref, id) => ref.watch(sessionRepositoryProvider).get(id),
    );

final liveSessionProvider = StreamProvider.autoDispose
    .family<ChargingSession, String>((ref, id) async* {
      final repository = ref.watch(sessionRepositoryProvider);
      var session = await repository.get(id);
      yield session;
      while (session.state.isLive) {
        await Future<void>.delayed(_pollInterval);
        session = await repository.get(id);
        yield session;
      }
      if (!ref.mounted) return;
      ref.invalidate(activeSessionProvider);
      ref.invalidate(historyProvider);
      ref.invalidate(walletProvider);
      ref.invalidate(walletEntriesProvider);
      await Future<void>.delayed(_settleDelay);
      if (ref.mounted) yield await repository.get(id);
    });
