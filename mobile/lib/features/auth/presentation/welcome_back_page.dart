import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/adaptive_spinner.dart';
import 'widgets/zella_logo.dart';

class WelcomeBackPage extends ConsumerStatefulWidget {
  const WelcomeBackPage({super.key});

  @override
  ConsumerState<WelcomeBackPage> createState() => _WelcomeBackPageState();
}

class _WelcomeBackPageState extends ConsumerState<WelcomeBackPage> {
  @override
  void initState() {
    super.initState();
    Future<void>.delayed(const Duration(milliseconds: 1800), () {
      if (mounted) ref.read(sessionControllerProvider.notifier).signedIn();
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.lg),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const ZellaLogo(height: 44),
                const SizedBox(height: AppSpacing.xl),
                Text(
                  AuthText.welcomeBackTitle,
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: AppSpacing.sm),
                Text(
                  AuthText.welcomeBackBody,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                const AdaptiveSpinner(size: 28),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
