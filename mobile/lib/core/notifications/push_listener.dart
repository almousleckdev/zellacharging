import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'push_message.dart';
import 'push_providers.dart';
import 'push_route.dart';
import 'push_toast.dart';

class PushListener extends ConsumerStatefulWidget {
  const PushListener({super.key, required this.child});

  final Widget child;

  @override
  ConsumerState<PushListener> createState() => _PushListenerState();
}

class _PushListenerState extends ConsumerState<PushListener> {
  final _subscriptions = <StreamSubscription<PushMessage>>[];

  @override
  void initState() {
    super.initState();
    final messaging = ref.read(pushMessagingProvider);
    unawaited(ref.read(pushRegistrationProvider).start());
    _subscriptions.add(messaging.foreground.listen(_showInApp));
    _subscriptions.add(messaging.opened.listen(_open));
    unawaited(
      messaging.launchedFrom.then((message) {
        if (message != null) _open(message);
      }),
    );
  }

  void _showInApp(PushMessage message) {
    if (!mounted) return;
    PushToast.show(context, message);
  }

  void _open(PushMessage message) {
    final route = routeFor(message);
    if (mounted && route != null) context.push(route);
  }

  @override
  void dispose() {
    for (final subscription in _subscriptions) {
      subscription.cancel();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
