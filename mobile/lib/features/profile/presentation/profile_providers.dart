import 'dart:typed_data';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../../../core/session/session_controller.dart';
import '../data/profile_repository_impl.dart';
import '../domain/profile.dart';
import '../domain/profile_repository.dart';

final profileRepositoryProvider = Provider<ProfileRepository>(
  (ref) => ProfileRepositoryImpl(ref.watch(dioProvider)),
);

final profileProvider = FutureProvider<Profile>((ref) {
  ref.watch(sessionControllerProvider);
  return ref.watch(profileRepositoryProvider).fetch();
});

final contactsCompleteProvider = Provider<bool?>((ref) {
  final signedIn =
      ref.watch(sessionControllerProvider) == SessionStatus.authenticated;
  if (!signedIn) return null;
  return ref.watch(profileProvider).whenOrNull(data: (p) => p.contactsComplete);
});

final avatarProvider = FutureProvider<Uint8List?>(
  (ref) => ref.watch(profileRepositoryProvider).fetchAvatar(),
);
