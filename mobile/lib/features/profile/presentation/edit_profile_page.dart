import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/info_card.dart';
import '../../../shared/widgets/info_row.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/profile.dart';
import 'avatar_editor.dart';
import 'profile_providers.dart';

class EditProfilePage extends ConsumerStatefulWidget {
  const EditProfilePage({super.key});

  @override
  ConsumerState<EditProfilePage> createState() => _EditProfilePageState();
}

class _EditProfilePageState extends ConsumerState<EditProfilePage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();

  @override
  void initState() {
    super.initState();
    final loaded = ref.read(profileProvider).asData?.value;
    if (loaded != null) _fill(loaded);
  }

  void _fill(Profile profile) {
    if (_firstName.text.isNotEmpty || _lastName.text.isNotEmpty) return;
    _firstName.text = profile.firstName;
    _lastName.text = profile.lastName;
  }

  @override
  void dispose() {
    _firstName.dispose();
    _lastName.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref
          .read(profileRepositoryProvider)
          .updateName(_firstName.text.trim(), _lastName.text.trim());
      ref.invalidate(profileProvider);
      if (!mounted) return;
      AppToast.info(context, ProfileText.profileUpdated);
      context.pop();
    });
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(profileProvider, (_, next) => next.whenData(_fill));
    final profile = ref.watch(profileProvider).asData?.value;
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.editProfile)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            AvatarEditor(initials: profile?.initials ?? ''),
            const SizedBox(height: AppSpacing.lg),
            Form(
              key: _formKey,
              child: Column(
                children: [
                  AppTextField(
                    controller: _firstName,
                    label: AuthText.firstNameLabel,
                    icon: AppIcons.user,
                    validator: Validators.required,
                  ),
                  const SizedBox(height: AppSpacing.md),
                  AppTextField(
                    controller: _lastName,
                    label: AuthText.lastNameLabel,
                    icon: AppIcons.user,
                    validator: Validators.required,
                    textInputAction: TextInputAction.done,
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            if (profile != null) _contactCard(profile.email, profile.phone),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: ProfileText.saveChanges,
              loading: loading,
              onPressed: _save,
            ),
          ],
        ),
      ),
    );
  }

  Widget _contactCard(String? email, String? phone) => InfoCard(
    title: ProfileText.personalInfo,
    children: [
      if (email != null) InfoRow(label: Strings.email, value: email),
      if (phone != null) InfoRow(label: Strings.phone, value: phone),
      Text(
        ProfileText.contactReadOnly,
        style: Theme.of(
          context,
        ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
      ),
    ],
  );
}
