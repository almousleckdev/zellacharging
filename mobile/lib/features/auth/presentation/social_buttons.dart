import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/session/stay_signed_in.dart';
import '../../../core/storage/preference_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/submit_mixin.dart';
import '../domain/social_provider.dart';
import 'auth_providers.dart';
import 'social_button.dart';
import 'social_providers_for.dart';

class SocialButtons extends ConsumerStatefulWidget {
  const SocialButtons({super.key});

  @override
  ConsumerState<SocialButtons> createState() => _SocialButtonsState();
}

class _SocialButtonsState extends ConsumerState<SocialButtons>
    with SubmitMixin {
  void _signIn(SocialProvider provider) {
    submit(() async {
      final signedIn = await ref.read(signInWithSocialProvider)(provider);
      if (!signedIn) return;
      await ref.read(preferenceStoreProvider).save(stayKey, '1');
      if (mounted) context.go(Routes.welcomeBack);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Row(
          children: [
            const Expanded(child: Divider(color: AppColors.border)),
            Expanded(
              flex: 2,
              child: Center(
                child: FittedBox(
                  fit: BoxFit.scaleDown,
                  child: Text(
                    AuthText.orContinueWith,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
              ),
            ),
            const Expanded(child: Divider(color: AppColors.border)),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        Row(
          children: [
            for (final provider in socialProvidersFor(context)) ...[
              if (provider != SocialProvider.google)
                const SizedBox(width: AppSpacing.md),
              Expanded(
                child: SocialButton(
                  provider: provider,
                  onPressed: loading ? null : () => _signIn(provider),
                ),
              ),
            ],
          ],
        ),
      ],
    );
  }
}
