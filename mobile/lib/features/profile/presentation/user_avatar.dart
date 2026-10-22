import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../shared/widgets/profile_avatar.dart';
import 'profile_providers.dart';

class UserAvatar extends ConsumerWidget {
  const UserAvatar({super.key, required this.initials, this.size = 44});

  final String initials;
  final double size;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final image = ref.watch(avatarProvider).asData?.value;
    return ProfileAvatar(initials: initials, size: size, image: image);
  }
}
