import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/notifications/push_providers.dart';
import 'notification_providers.dart';

class BadgeSync extends ConsumerStatefulWidget {
  const BadgeSync({super.key, required this.child});

  final Widget child;

  @override
  ConsumerState<BadgeSync> createState() => _BadgeSyncState();
}

class _BadgeSyncState extends ConsumerState<BadgeSync> {
  StreamSubscription<Object?>? _foreground;

  @override
  void initState() {
    super.initState();
    _foreground = ref
        .read(pushMessagingProvider)
        .foreground
        .listen((_) => ref.invalidate(unreadCountProvider));
  }

  @override
  void dispose() {
    _foreground?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(unreadCountProvider, (_, next) {
      final count = next.value;
      if (count != null) ref.read(appBadgeProvider).set(count);
    });
    return widget.child;
  }
}
