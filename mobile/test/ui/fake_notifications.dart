import 'package:zella/core/notifications/push_message.dart';
import 'package:zella/core/notifications/push_messaging.dart';
import 'package:zella/features/notifications/domain/app_notification.dart';
import 'package:zella/features/notifications/domain/notification_feed.dart';
import 'package:zella/features/notifications/domain/notification_repository.dart';

class FakeNotifications implements NotificationRepository {
  FakeNotifications({List<AppNotification>? items})
    : _items = items ?? sampleNotifications();

  final List<AppNotification> _items;
  final markedRead = <String>[];
  var markedAll = false;

  int get _unread => _items.where((item) => !item.read).length;

  @override
  Future<NotificationFeed> fetch() async =>
      NotificationFeed(unread: _unread, items: List.of(_items));

  @override
  Future<int> unreadCount() async => _unread;

  @override
  Future<void> markRead(String id) async {
    markedRead.add(id);
    final index = _items.indexWhere((item) => item.id == id);
    _items[index] = _copy(_items[index]);
  }

  @override
  Future<void> markAllRead() async {
    markedAll = true;
    for (var i = 0; i < _items.length; i++) {
      _items[i] = _copy(_items[i]);
    }
  }

  AppNotification _copy(AppNotification item) => AppNotification(
    id: item.id,
    type: item.type,
    title: item.title,
    body: item.body,
    createdAt: item.createdAt,
    read: true,
    screen: item.screen,
    entityId: item.entityId,
  );
}

List<AppNotification> sampleNotifications() => [
  AppNotification(
    id: 'n1',
    type: 'TOPUP_SUCCEEDED',
    title: 'Recharge réussie',
    body: 'Votre portefeuille a été crédité.',
    createdAt: DateTime(2026, 10, 30, 9, 30),
    read: false,
    screen: 'wallet',
  ),
  AppNotification(
    id: 'n2',
    type: 'CHARGE_COMPLETED',
    title: 'Recharge terminée',
    body: 'Consultez le détail de votre recharge.',
    createdAt: DateTime(2026, 10, 29, 18, 5),
    read: true,
    screen: 'charging',
    entityId: 's1',
  ),
];

class NoPush implements PushMessaging {
  @override
  Future<bool> requestPermission() async => false;

  @override
  Future<String?> token() async => null;

  @override
  Stream<String> get tokenRefreshed => const Stream.empty();

  @override
  Stream<PushMessage> get foreground => const Stream.empty();

  @override
  Stream<PushMessage> get opened => const Stream.empty();

  @override
  Future<PushMessage?> get launchedFrom async => null;

  @override
  Future<void> forgetToken() async {}
}
