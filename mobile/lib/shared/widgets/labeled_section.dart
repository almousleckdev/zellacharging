import 'package:flutter/material.dart';

import '../../core/theme/app_spacing.dart';
import 'section_title.dart';

class LabeledSection extends StatelessWidget {
  const LabeledSection({super.key, required this.title, required this.child});

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionTitle(title: title),
        const SizedBox(height: AppSpacing.md),
        child,
        const SizedBox(height: AppSpacing.lg),
      ],
    );
  }
}
