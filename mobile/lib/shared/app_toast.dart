import 'dart:async';

import 'package:flutter/material.dart';

import '../core/theme/app_colors.dart';
import '../core/theme/app_icons.dart';
import '../core/theme/app_spacing.dart';

abstract final class AppToast {
  static OverlayEntry? _current;

  static void error(BuildContext context, String message) =>
      _show(context, message, AppIcons.warning, AppColors.danger);

  static void info(BuildContext context, String message) =>
      _show(context, message, AppIcons.info, AppColors.textPrimary);

  static void _show(
    BuildContext context,
    String message,
    IconData icon,
    Color accent,
  ) {
    _current?.remove();
    final overlay = Overlay.of(context, rootOverlay: true);
    late final OverlayEntry entry;
    entry = OverlayEntry(
      builder: (_) => _ToastView(
        message: message,
        icon: icon,
        accent: accent,
        onDone: () {
          if (_current == entry) _current = null;
          entry.remove();
        },
      ),
    );
    _current = entry;
    overlay.insert(entry);
  }
}

class _ToastView extends StatefulWidget {
  const _ToastView({
    required this.message,
    required this.icon,
    required this.accent,
    required this.onDone,
  });

  final String message;
  final IconData icon;
  final Color accent;
  final VoidCallback onDone;

  @override
  State<_ToastView> createState() => _ToastViewState();
}

class _ToastViewState extends State<_ToastView>
    with SingleTickerProviderStateMixin {
  late final AnimationController _motion = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 260),
  );
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _motion.forward();
    _timer = Timer(const Duration(milliseconds: 3600), _dismiss);
  }

  Future<void> _dismiss() async {
    _timer?.cancel();
    if (!mounted) return;
    await _motion.reverse();
    widget.onDone();
  }

  @override
  void dispose() {
    _timer?.cancel();
    _motion.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final curved = CurvedAnimation(parent: _motion, curve: Curves.easeOutCubic);
    return Positioned(
      top: MediaQuery.paddingOf(context).top + AppSpacing.sm,
      left: AppSpacing.md,
      right: AppSpacing.md,
      child: FadeTransition(
        opacity: curved,
        child: SlideTransition(
          position: Tween(
            begin: const Offset(0, -0.6),
            end: Offset.zero,
          ).animate(curved),
          child: GestureDetector(
            onTap: _dismiss,
            onVerticalDragEnd: (_) => _dismiss(),
            child: Material(color: Colors.transparent, child: _card(context)),
          ),
        ),
      ),
    );
  }

  Widget _card(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(
      horizontal: AppSpacing.md,
      vertical: AppSpacing.md - 2,
    ),
    decoration: BoxDecoration(
      color: AppColors.surface,
      borderRadius: BorderRadius.circular(AppSpacing.radius),
      border: Border.all(color: AppColors.border),
      boxShadow: [
        BoxShadow(
          color: AppColors.textPrimary.withValues(alpha: 0.12),
          blurRadius: 24,
          offset: const Offset(0, 8),
        ),
      ],
    ),
    child: Row(
      children: [
        Icon(widget.icon, size: 22, color: widget.accent),
        const SizedBox(width: AppSpacing.sm + 2),
        Expanded(
          child: Text(
            widget.message,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    ),
  );
}
