import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../../wallet/presentation/wallet_providers.dart';
import '../data/session_repository_impl.dart';
import '../domain/charging_session.dart';
import '../domain/quote_request.dart';
import '../domain/session_quote.dart';
import '../domain/session_repository.dart';
import '../domain/start_charge.dart';
import 'live_session_poller.dart';

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
      yield* LiveSessionPoller(repository).watch(id, alive: () => ref.mounted);
      if (!ref.mounted) return;
      ref.invalidate(activeSessionProvider);
      ref.invalidate(historyProvider);
      ref.invalidate(walletProvider);
      ref.invalidate(walletEntriesProvider);
      await Future<void>.delayed(_settleDelay);
      if (ref.mounted) yield await repository.get(id);
    });
