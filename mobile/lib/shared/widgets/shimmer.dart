import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class Shimmer extends StatefulWidget {
  const Shimmer({super.key, required this.child});

  final Widget child;

  @override
  State<Shimmer> createState() => _ShimmerState();
}

class _ShimmerState extends State<Shimmer> with SingleTickerProviderStateMixin {
  static const _sweep = Duration(milliseconds: 1400);

  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: _sweep,
  )..repeat();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (MediaQuery.disableAnimationsOf(context)) return widget.child;
    return AnimatedBuilder(
      animation: _controller,
      child: widget.child,
      builder: (context, child) => ShaderMask(
        blendMode: BlendMode.srcATop,
        shaderCallback: (bounds) => LinearGradient(
          colors: const [
            AppColors.border,
            AppColors.surfaceMuted,
            AppColors.border,
          ],
          stops: const [0.3, 0.5, 0.7],
          transform: _SlidingGradient(_controller.value),
        ).createShader(bounds),
        child: child,
      ),
    );
  }
}

class _SlidingGradient extends GradientTransform {
  const _SlidingGradient(this.progress);

  final double progress;

  @override
  Matrix4 transform(Rect bounds, {TextDirection? textDirection}) {
    final direction = textDirection == TextDirection.rtl ? -1 : 1;
    return Matrix4.translationValues(
      direction * bounds.width * (progress * 2 - 1),
      0,
      0,
    );
  }
}
