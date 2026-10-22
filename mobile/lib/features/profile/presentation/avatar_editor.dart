import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/media/media_providers.dart';
import '../../../core/media/photo_source.dart';
import '../../../core/network/api_error.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/adaptive_picker.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/picker_option.dart';
import 'profile_providers.dart';
import 'user_avatar.dart';

enum _PhotoChoice { camera, gallery, remove }

class AvatarEditor extends ConsumerWidget {
  const AvatarEditor({super.key, required this.initials});

  final String initials;

  Future<void> _change(BuildContext context, WidgetRef ref) async {
    final hasPhoto = ref.read(avatarProvider).asData?.value != null;
    final choice = await showOptionPicker<_PhotoChoice>(
      context,
      title: ProfileText.changePhoto,
      options: [
        PickerOption(label: ProfileText.takePhoto, value: _PhotoChoice.camera),
        PickerOption(
          label: ProfileText.chooseFromGallery,
          value: _PhotoChoice.gallery,
        ),
        if (hasPhoto)
          PickerOption(
            label: ProfileText.removePhoto,
            value: _PhotoChoice.remove,
            destructive: true,
          ),
      ],
    );
    if (choice == null || !context.mounted) return;
    try {
      final message = await _apply(ref, choice);
      if (message != null && context.mounted) AppToast.info(context, message);
    } on ApiError catch (error) {
      if (context.mounted) AppToast.error(context, error.message);
    } catch (_) {
      if (context.mounted) AppToast.error(context, Strings.unknownError);
    }
  }

  Future<String?> _apply(WidgetRef ref, _PhotoChoice choice) async {
    final repository = ref.read(profileRepositoryProvider);
    if (choice == _PhotoChoice.remove) {
      await repository.deleteAvatar();
      ref.invalidate(avatarProvider);
      return ProfileText.photoRemoved;
    }
    final photo = await ref
        .read(photoPickerProvider)
        .pick(
          choice == _PhotoChoice.camera
              ? PhotoSource.camera
              : PhotoSource.gallery,
        );
    if (photo == null) return null;
    await repository.uploadAvatar(photo.bytes, photo.contentType);
    ref.invalidate(avatarProvider);
    return ProfileText.photoUpdated;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      children: [
        GestureDetector(
          onTap: () => _change(context, ref),
          child: Stack(
            children: [
              UserAvatar(initials: initials, size: 104),
              Positioned(
                right: 0,
                bottom: 0,
                child: Container(
                  padding: const EdgeInsets.all(AppSpacing.sm),
                  decoration: BoxDecoration(
                    color: AppColors.primary,
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.surface, width: 3),
                  ),
                  child: Icon(
                    AppIcons.camera,
                    size: 18,
                    color: AppColors.onPrimary,
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        TextButton(
          onPressed: () => _change(context, ref),
          child: Text(ProfileText.changePhoto),
        ),
      ],
    );
  }
}
