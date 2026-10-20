import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/error_banner.dart';

class SetupHeader extends StatelessWidget {
  const SetupHeader({super.key, required this.title, this.errorMessage});

  final String title;
  final String? errorMessage;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: Theme.of(context).textTheme.titleLarge),
        const SizedBox(height: AppSpacing.lg),
        if (errorMessage != null) ...[
          ErrorBanner(message: errorMessage!),
          const SizedBox(height: AppSpacing.md),
        ],
      ],
    );
  }
}
