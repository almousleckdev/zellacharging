import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/app_logo.dart';
import '../../../shared/widgets/auth_backdrop.dart';
import '../../../shared/widgets/glass_button.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../../core/theme/app_colors.dart';

class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: AuthBackdrop(
        child: SafeArea(
          child: LayoutBuilder(
            builder: (context, constraints) => SingleChildScrollView(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: ConstrainedBox(
                constraints: BoxConstraints(
                  minHeight: constraints.maxHeight - AppSpacing.lg * 2,
                ),
                child: IntrinsicHeight(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const AppLogo(size: 60, showName: false),
                      const Spacer(),
                      Text(
                        AuthText.welcomeTitle,
                        style: theme.textTheme.displaySmall?.copyWith(
                          color: AppColors.onPrimary,
                          fontSize: 40,
                          height: 1.1,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      const SizedBox(height: AppSpacing.md),
                      Text(
                        AuthText.welcomeBody,
                        style: theme.textTheme.bodyLarge?.copyWith(
                          color: AppColors.onPrimary.withValues(alpha: 0.85),
                          height: 1.45,
                        ),
                      ),
                      const SizedBox(height: AppSpacing.xl),
                      PrimaryButton(
                        label: Strings.getStarted,
                        onPressed: () => context.push(Routes.onboarding),
                      ),
                      const SizedBox(height: AppSpacing.md),
                      GlassButton(
                        label: AuthText.signIn,
                        onPressed: () => context.go(Routes.login),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
