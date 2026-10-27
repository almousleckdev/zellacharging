import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/wallet_repository_impl.dart';
import '../domain/wallet.dart';
import '../domain/wallet_entry.dart';
import '../domain/wallet_entry_detail.dart';
import '../domain/wallet_repository.dart';

final walletRepositoryProvider = Provider<WalletRepository>(
  (ref) => WalletRepositoryImpl(ref.watch(dioProvider)),
);

final walletProvider = FutureProvider.autoDispose<Wallet>(
  (ref) => ref.watch(walletRepositoryProvider).fetch(),
);

final walletEntryDetailProvider = FutureProvider.autoDispose
    .family<WalletEntryDetail, String>(
      (ref, id) => ref.watch(walletRepositoryProvider).entryDetail(id),
    );

final walletEntriesProvider = FutureProvider.autoDispose<List<WalletEntry>>(
  (ref) => ref.watch(walletRepositoryProvider).entries(),
);
