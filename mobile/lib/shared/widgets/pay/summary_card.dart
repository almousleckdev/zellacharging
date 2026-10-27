import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'pay_card.dart';
import 'summary_line.dart';

class SummaryCard extends StatelessWidget {
  const SummaryCard({
    super.key,
    required this.title,
    required this.lines,
    this.footnote,
  });

  final String title;
  final List<SummaryLine> lines;
  final String? footnote;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return PayCard(
      title: title,
      child: Column(
        children: [
          for (var index = 0; index < lines.length; index++) ...[
            if (index > 0) const Divider(height: AppSpacing.md * 2),
            _Row(line: lines[index]),
          ],
          if (footnote != null) ...[
            const SizedBox(height: AppSpacing.md),
            Text(
              footnote!,
              style: theme.textTheme.bodySmall?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _Row extends StatelessWidget {
  const _Row({required this.line});

  final SummaryLine line;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final weight = line.emphasis ? FontWeight.w800 : FontWeight.w500;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Text(
            line.label,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: line.emphasis
                  ? AppColors.textPrimary
                  : AppColors.textSecondary,
              fontWeight: weight,
            ),
          ),
        ),
        const SizedBox(width: AppSpacing.md),
        Flexible(
          child: Text(
            line.value,
            textAlign: TextAlign.end,
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: weight,
              color: line.emphasis ? AppColors.primary : AppColors.textPrimary,
            ),
          ),
        ),
      ],
    );
  }
}
