import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/l10n/strings.dart';
import '../../core/theme/app_colors.dart';

class ResendCountdown extends StatefulWidget {
  const ResendCountdown({super.key, required this.onResend, this.seconds = 60});

  final Future<void> Function() onResend;
  final int seconds;

  @override
  State<ResendCountdown> createState() => _ResendCountdownState();
}

class _ResendCountdownState extends State<ResendCountdown> {
  Timer? _timer;
  late int _left = widget.seconds;

  @override
  void initState() {
    super.initState();
    _start();
  }

  void _start() {
    _timer?.cancel();
    setState(() => _left = widget.seconds);
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_left <= 1) timer.cancel();
      setState(() => _left--);
    });
  }

  Future<void> _resend() async {
    await widget.onResend();
    if (mounted) _start();
  }

  String get _clock =>
      '${(_left ~/ 60).toString().padLeft(2, '0')}:${(_left % 60).toString().padLeft(2, '0')}';

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(
      context,
    ).textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary);
    return Center(
      child: _left > 0
          ? Text(AuthText.resendIn(_clock), style: style)
          : TextButton(
              onPressed: _resend,
              child: Text(
                AuthText.resendCode,
                style: TextStyle(
                  color: AppColors.primary,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
    );
  }
}
