import 'dart:async';

import 'package:flutter/material.dart';

import '../../../core/l10n/charge_text.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';

class PlugInPrompt extends StatefulWidget {
  const PlugInPrompt({super.key, required this.deadline});

  final DateTime? deadline;

  @override
  State<PlugInPrompt> createState() => _PlugInPromptState();
}

class _PlugInPromptState extends State<PlugInPrompt> {
  Timer? _ticker;

  @override
  void initState() {
    super.initState();
    _ticker = Timer.periodic(
      const Duration(seconds: 1),
      (_) => setState(() {}),
    );
  }

  @override
  void dispose() {
    _ticker?.cancel();
    super.dispose();
  }

  String? get _remaining {
    final deadline = widget.deadline;
    if (deadline == null) return null;
    final left = deadline.difference(DateTime.now());
    if (left.isNegative) return null;
    final minutes = left.inMinutes.toString();
    final seconds = (left.inSeconds % 60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final remaining = _remaining;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(AppSpacing.radius),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(AppIcons.plugs, color: AppColors.primary),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: Text(
                  ChargeText.plugPromptTitle,
                  style: theme.textTheme.titleMedium,
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(ChargeText.plugPromptBody, style: theme.textTheme.bodyMedium),
          if (remaining != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(
              ChargeText.plugCountdown(remaining),
              style: theme.textTheme.labelLarge?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ],
      ),
    );
  }
}
