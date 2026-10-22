import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class ProfileAvatar extends StatelessWidget {
  const ProfileAvatar({
    super.key,
    required this.initials,
    this.size = 44,
    this.image,
  });

  final String initials;
  final double size;
  final Uint8List? image;

  @override
  Widget build(BuildContext context) {
    final picture = image;
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppColors.primary, AppColors.primaryLight],
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withValues(alpha: 0.25),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: picture == null
          ? _initials()
          : Image.memory(
              picture,
              width: size,
              height: size,
              fit: BoxFit.cover,
              gaplessPlayback: true,
              errorBuilder: (_, _, _) => _initials(),
            ),
    );
  }

  Widget _initials() => Text(
    initials,
    style: TextStyle(
      color: AppColors.onPrimary,
      fontSize: size * 0.36,
      fontWeight: FontWeight.w700,
    ),
  );
}
