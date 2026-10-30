import 'app_notification.dart';

class NotificationFeed {
  const NotificationFeed({required this.unread, required this.items});

  final int unread;
  final List<AppNotification> items;
}
