import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/storage/preference_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/primary_button.dart';
import 'onboarding_slide.dart';
import 'onboarding_slide_view.dart';
import 'splash_state.dart';
import 'widgets/auth_page.dart';
import 'widgets/onboarding_progress.dart';

class OnboardingPage extends ConsumerStatefulWidget {
  const OnboardingPage({super.key});

  @override
  ConsumerState<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends ConsumerState<OnboardingPage> {
  final _controller = PageController();
  final _slides = OnboardingSlide.all;
  int _index = 0;

  bool get _isLast => _index == _slides.length - 1;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _finish() async {
    await ref.read(preferenceStoreProvider).save(onboardingSeenKey, '1');
    ref.invalidate(onboardingSeenProvider);
    if (mounted) context.go(Routes.register);
  }

  void _next() {
    if (_isLast) {
      _finish();
      return;
    }
    _controller.nextPage(
      duration: const Duration(milliseconds: 300),
      curve: Curves.easeOut,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            children: [
              OnboardingProgress(index: _index, count: _slides.length),
              const SizedBox(height: AppSpacing.lg),
              Expanded(
                child: PageView(
                  controller: _controller,
                  onPageChanged: (value) => setState(() => _index = value),
                  children: [
                    for (final slide in _slides)
                      OnboardingSlideView(slide: slide),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: authContentWidth),
                child: PrimaryButton(
                  height: authButtonHeight,
                  label: _isLast ? Strings.getStarted : AuthText.next,
                  onPressed: _next,
                ),
              ),
              SizedBox(
                height: 44,
                child: _isLast
                    ? null
                    : TextButton(
                        onPressed: _finish,
                        child: Text(
                          AuthText.skipLabel,
                          style: const TextStyle(
                            color: AppColors.primary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
