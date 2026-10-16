import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/wallet_repository_impl.dart';
import '../domain/wallet.dart';
import '../domain/wallet_repository.dart';

final walletRepositoryProvider = Provider<WalletRepository>(
  (ref) => WalletRepositoryImpl(ref.watch(dioProvider)),
);

final walletProvider = FutureProvider.autoDispose<Wallet>(
  (ref) => ref.watch(walletRepositoryProvider).fetch(),
);
