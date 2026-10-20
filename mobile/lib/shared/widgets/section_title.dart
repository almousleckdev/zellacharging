import 'package:flutter/material.dart';

import '../../core/l10n/strings.dart';
import '../../core/theme/app_colors.dart';

class SectionTitle extends StatelessWidget {
  const SectionTitle({super.key, required this.title, this.onViewAll});

  final String title;
  final VoidCallback? onViewAll;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Expanded(
          child: Text(title, style: Theme.of(context).textTheme.titleMedium),
        ),
        if (onViewAll != null)
          GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: onViewAll,
            child: const Text(
              Strings.viewAll,
              style: TextStyle(color: AppColors.primary),
            ),
          ),
      ],
    );
  }
}
