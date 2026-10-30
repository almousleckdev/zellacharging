import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/app_notification.dart';
import '../domain/notification_feed.dart';
import '../domain/notification_repository.dart';

class NotificationRepositoryImpl implements NotificationRepository {
  NotificationRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<NotificationFeed> fetch() => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>(
      '/v1/notifications',
    );
    final json = response.data!;
    return NotificationFeed(
      unread: (json['unread'] as num).toInt(),
      items: (json['items'] as List<dynamic>)
          .map((item) => _item(item as Map<String, dynamic>))
          .toList(),
    );
  });

  @override
  Future<int> unreadCount() => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>(
      '/v1/notifications/unread-count',
    );
    return (response.data!['count'] as num).toInt();
  });

  @override
  Future<void> markRead(String id) =>
      _guard(() => _client.post<void>('/v1/notifications/$id/read'));

  @override
  Future<void> markAllRead() =>
      _guard(() => _client.post<void>('/v1/notifications/read'));

  AppNotification _item(Map<String, dynamic> json) => AppNotification(
    id: json['id'] as String,
    type: json['type'] as String,
    title: json['title'] as String,
    body: json['body'] as String,
    createdAt: DateTime.parse(json['createdAt'] as String),
    read: json['read'] as bool,
    screen: json['screen'] as String?,
    entityId: json['entityId'] as String?,
  );

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
