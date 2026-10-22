import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';

class SetupHeader extends StatelessWidget {
  const SetupHeader({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: Theme.of(context).textTheme.titleLarge),
        const SizedBox(height: AppSpacing.lg),
      ],
    );
  }
}
