import 'dart:typed_data';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/profile_repository_impl.dart';
import '../domain/profile.dart';
import '../domain/profile_repository.dart';

final profileRepositoryProvider = Provider<ProfileRepository>(
  (ref) => ProfileRepositoryImpl(ref.watch(dioProvider)),
);

final profileProvider = FutureProvider<Profile>(
  (ref) => ref.watch(profileRepositoryProvider).fetch(),
);

final avatarProvider = FutureProvider<Uint8List?>(
  (ref) => ref.watch(profileRepositoryProvider).fetchAvatar(),
);
