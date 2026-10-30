import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/notifications/app_badge.dart';
import 'package:zella/core/notifications/push_providers.dart';
import 'package:zella/core/router/routes.dart';
import 'package:zella/core/theme/app_theme.dart';
import 'package:zella/features/notifications/presentation/badge_sync.dart';
import 'package:zella/features/notifications/presentation/notification_bell.dart';
import 'package:zella/features/notifications/presentation/notification_providers.dart';
import 'package:zella/features/notifications/presentation/notifications_page.dart';

import 'fake_notifications.dart';
import 'test_app.dart';
import 'test_fonts.dart';

class _RecordingBadge implements AppBadge {
  final counts = <int>[];

  @override
  Future<void> set(int count) async => counts.add(count);
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('lists notifications with the unread one marked', (tester) async {
    final repo = FakeNotifications();
    await pumpApp(
      tester,
      const NotificationsPage(),
      overrides: [notificationRepositoryProvider.overrideWithValue(repo)],
    );

    expect(find.text('Recharge réussie'), findsOneWidget);
    expect(find.text('Recharge terminée'), findsOneWidget);
    expect(find.text(NotificationText.markAllRead), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('marks everything read from the app bar', (tester) async {
    final repo = FakeNotifications();
    await pumpApp(
      tester,
      const NotificationsPage(),
      overrides: [notificationRepositoryProvider.overrideWithValue(repo)],
    );

    await tester.tap(find.text(NotificationText.markAllRead));
    await settle(tester);

    expect(repo.markedAll, isTrue);
    expect(find.text(NotificationText.markAllRead), findsNothing);
    await closeApp(tester);
  });

  testWidgets('opening an unread notification marks it read and goes there', (
    tester,
  ) async {
    final repo = FakeNotifications();
    final router = GoRouter(
      routes: [
        GoRoute(path: '/', builder: (_, _) => const NotificationsPage()),
        GoRoute(path: Routes.wallet, builder: (_, _) => const Text('wallet')),
      ],
    );
    await tester.pumpWidget(
      ProviderScope(
        overrides: [notificationRepositoryProvider.overrideWithValue(repo)],
        child: MaterialApp.router(
          theme: AppTheme.light(),
          routerConfig: router,
        ),
      ),
    );
    await settle(tester);

    await tester.tap(find.text('Recharge réussie'));
    await settle(tester);

    expect(repo.markedRead, ['n1']);
    expect(find.text('wallet'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('explains an empty inbox', (tester) async {
    await pumpApp(
      tester,
      const NotificationsPage(),
      overrides: [
        notificationRepositoryProvider.overrideWithValue(
          FakeNotifications(items: []),
        ),
      ],
    );

    expect(find.text(NotificationText.empty), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the bell shows the unread count', (tester) async {
    await pumpApp(
      tester,
      const Scaffold(body: Center(child: NotificationBell())),
      overrides: [
        notificationRepositoryProvider.overrideWithValue(FakeNotifications()),
      ],
    );

    expect(find.text('1'), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the bell shows no count when everything is read', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const Scaffold(body: Center(child: NotificationBell())),
      overrides: [
        notificationRepositoryProvider.overrideWithValue(
          FakeNotifications(items: []),
        ),
      ],
    );

    expect(find.byType(Badge), findsOneWidget);
    expect(tester.widget<Badge>(find.byType(Badge)).isLabelVisible, isFalse);
    await closeApp(tester);
  });

  testWidgets('the app icon badge follows the unread count', (tester) async {
    final badge = _RecordingBadge();
    await pumpApp(
      tester,
      const BadgeSync(child: SizedBox()),
      overrides: [
        notificationRepositoryProvider.overrideWithValue(FakeNotifications()),
        appBadgeProvider.overrideWithValue(badge),
        pushMessagingProvider.overrideWithValue(NoPush()),
      ],
    );
    final container = ProviderScope.containerOf(
      tester.element(find.byType(BadgeSync)),
    );
    await container.read(unreadCountProvider.future);
    await settle(tester);

    expect(badge.counts, [1]);
    await closeApp(tester);
  });
}
