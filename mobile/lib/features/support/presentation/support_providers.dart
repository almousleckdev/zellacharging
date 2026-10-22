import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/support_repository_impl.dart';
import '../domain/support_info.dart';
import '../domain/support_repository.dart';

final supportRepositoryProvider = Provider<SupportRepository>(
  (ref) => SupportRepositoryImpl(ref.watch(dioProvider)),
);

final supportProvider = FutureProvider.autoDispose<SupportInfo>(
  (ref) => ref.watch(supportRepositoryProvider).fetch(),
);
