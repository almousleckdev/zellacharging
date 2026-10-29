import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/charging/presentation/session_providers.dart';
import '../../features/wallet/presentation/wallet_providers.dart';

const _interval = Duration(seconds: 15);

class LiveDataRefresher extends ConsumerStatefulWidget {
  const LiveDataRefresher({super.key, required this.child});

  final Widget child;

  @override
  ConsumerState<LiveDataRefresher> createState() => _LiveDataRefresherState();
}

class _LiveDataRefresherState extends ConsumerState<LiveDataRefresher>
    with WidgetsBindingObserver {
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _start();
  }

  void _start() {
    _timer?.cancel();
    _timer = Timer.periodic(_interval, (_) => _refreshLive());
  }

  void _refreshLive() {
    ref.invalidate(activeSessionProvider);
    ref.invalidate(walletProvider);
    ref.invalidate(walletEntriesProvider);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _refreshLive();
      ref.invalidate(historyProvider);
      _start();
    } else if (state == AppLifecycleState.paused) {
      _timer?.cancel();
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
