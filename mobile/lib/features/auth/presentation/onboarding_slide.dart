import '../../../core/l10n/strings.dart';

class OnboardingSlide {
  const OnboardingSlide({
    required this.image,
    required this.title,
    required this.body,
  });

  final String image;
  final String title;
  final String body;

  static List<OnboardingSlide> get all => [
    OnboardingSlide(
      image: 'assets/images/onboarding1.png',
      title: AuthText.slide1Title,
      body: AuthText.slide1Body,
    ),
    OnboardingSlide(
      image: 'assets/images/onboarding2.png',
      title: AuthText.slide2Title,
      body: AuthText.slide2Body,
    ),
    OnboardingSlide(
      image: 'assets/images/onboarding3.png',
      title: AuthText.slide3Title,
      body: AuthText.slide3Body,
    ),
  ];
}
