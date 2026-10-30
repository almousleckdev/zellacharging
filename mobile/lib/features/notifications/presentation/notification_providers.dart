import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/notification_repository_impl.dart';
import '../domain/notification_feed.dart';
import '../domain/notification_repository.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>(
  (ref) => NotificationRepositoryImpl(ref.watch(dioProvider)),
);

final notificationFeedProvider = FutureProvider.autoDispose<NotificationFeed>(
  (ref) => ref.watch(notificationRepositoryProvider).fetch(),
);

final unreadCountProvider = FutureProvider<int>(
  (ref) => ref.watch(notificationRepositoryProvider).unreadCount(),
);
