import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_icons.dart';
import '../../../shared/adaptive_dialog.dart';
import '../../../shared/widgets/secondary_button.dart';

class LogoutButton extends ConsumerWidget {
  const LogoutButton({super.key});

  Future<void> _logOut(BuildContext context, WidgetRef ref) async {
    final confirmed = await showConfirmDialog(
      context,
      title: ProfileText.logOutTitle,
      message: ProfileText.logOutBody,
      confirmLabel: ProfileText.logOut,
      destructive: true,
    );
    if (confirmed) ref.read(sessionControllerProvider.notifier).signOut();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SecondaryButton(
      label: ProfileText.logOut,
      icon: AppIcons.signOut,
      destructive: true,
      onPressed: () => _logOut(context, ref),
    );
  }
}
