import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'animated_illustration.dart';
import 'onboarding_slide.dart';

class OnboardingSlideView extends StatelessWidget {
  const OnboardingSlideView({super.key, required this.slide});

  final OnboardingSlide slide;

  String get _image => switch (slide.art) {
    OnboardingArt.find => 'assets/images/onboarding_find.png',
    OnboardingArt.scan => 'assets/images/onboarding_scan.png',
    OnboardingArt.track => 'assets/images/track_every_charge.png',
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Expanded(
          child: Center(child: AnimatedIllustration(asset: _image)),
        ),
        Text(
          slide.title,
          textAlign: TextAlign.center,
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: AppSpacing.sm + 2),
        Text(
          slide.body,
          textAlign: TextAlign.center,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.textSecondary,
            height: 1.45,
          ),
        ),
      ],
    );
  }
}
