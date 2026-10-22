import '../../../core/l10n/strings.dart';

enum OnboardingArt { find, scan, track }

class OnboardingSlide {
  const OnboardingSlide({
    required this.title,
    required this.body,
    required this.art,
  });

  final String title;
  final String body;
  final OnboardingArt art;

  static final all = [
    OnboardingSlide(
      title: AuthText.slideFindTitle,
      body: AuthText.slideFindBody,
      art: OnboardingArt.find,
    ),
    OnboardingSlide(
      title: AuthText.slideScanTitle,
      body: AuthText.slideScanBody,
      art: OnboardingArt.scan,
    ),
    OnboardingSlide(
      title: AuthText.slideTrackTitle,
      body: AuthText.slideTrackBody,
      art: OnboardingArt.track,
    ),
  ];
}
