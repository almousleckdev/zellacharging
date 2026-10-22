import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class AdaptiveSpinner extends StatelessWidget {
  const AdaptiveSpinner({
    super.key,
    this.color = AppColors.primary,
    this.size = 28,
  });

  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) {
    final platform = Theme.of(context).platform;
    final apple =
        platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
    return SizedBox.square(
      dimension: size,
      child: apple
          ? CupertinoActivityIndicator(color: color)
          : CircularProgressIndicator(
              strokeWidth: 2.4,
              valueColor: AlwaysStoppedAnimation(color),
            ),
    );
  }
}
