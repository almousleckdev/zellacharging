import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';

class OnboardingSlide {
  const OnboardingSlide({
    required this.title,
    required this.body,
    this.icon,
    this.image,
  });

  final String title;
  final String body;
  final IconData? icon;
  final String? image;

  static const all = [
    OnboardingSlide(
      title: Strings.slideFindTitle,
      body: Strings.slideFindBody,
      icon: Icons.ev_station_rounded,
    ),
    OnboardingSlide(
      title: Strings.slideScanTitle,
      body: Strings.slideScanBody,
      icon: Icons.qr_code_scanner_rounded,
    ),
    OnboardingSlide(
      title: Strings.slideTrackTitle,
      body: Strings.slideTrackBody,
      image: 'assets/images/track_every_charge.png',
    ),
  ];
}
