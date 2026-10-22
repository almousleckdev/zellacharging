import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/app_logo.dart';
import '../../../shared/widgets/primary_button.dart';
import 'onboarding_slide.dart';
import 'onboarding_slide_view.dart';
import 'page_dots.dart';

class OnboardingPage extends StatefulWidget {
  const OnboardingPage({super.key});

  @override
  State<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  final _controller = PageController();
  int _index = 0;

  bool get _isLast => _index == OnboardingSlide.all.length - 1;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _next() {
    if (_isLast) {
      context.go(Routes.register);
      return;
    }
    _controller.nextPage(
      duration: const Duration(milliseconds: 250),
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
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const AppLogo(size: 34, showName: false),
                  TextButton(
                    onPressed: () => context.go(Routes.register),
                    child: Text(
                      AuthText.skip,
                      style: TextStyle(
                        color: AppColors.primary,
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 0.6,
                      ),
                    ),
                  ),
                ],
              ),
              Expanded(
                child: PageView(
                  controller: _controller,
                  onPageChanged: (value) => setState(() => _index = value),
                  children: [
                    for (final slide in OnboardingSlide.all)
                      OnboardingSlideView(slide: slide),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),
              PageDots(count: OnboardingSlide.all.length, index: _index),
              const SizedBox(height: AppSpacing.lg),
              PrimaryButton(
                label: _isLast ? Strings.getStarted : Strings.next,
                onPressed: _next,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
