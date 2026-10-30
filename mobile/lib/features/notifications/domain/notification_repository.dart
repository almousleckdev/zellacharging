import 'notification_feed.dart';

abstract class NotificationRepository {
  Future<NotificationFeed> fetch();

  Future<int> unreadCount();

  Future<void> markRead(String id);

  Future<void> markAllRead();
}
