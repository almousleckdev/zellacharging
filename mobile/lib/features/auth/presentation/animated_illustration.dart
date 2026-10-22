import 'package:flutter/material.dart';

class AnimatedIllustration extends StatefulWidget {
  const AnimatedIllustration({super.key, required this.asset});

  final String asset;

  @override
  State<AnimatedIllustration> createState() => _AnimatedIllustrationState();
}

class _AnimatedIllustrationState extends State<AnimatedIllustration>
    with TickerProviderStateMixin {
  late final AnimationController _entrance = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 700),
  )..forward();
  late final AnimationController _float = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 3200),
  );

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (MediaQuery.disableAnimationsOf(context)) {
      _float.stop();
    } else if (!_float.isAnimating) {
      _float.repeat(reverse: true);
    }
  }

  @override
  void dispose() {
    _entrance.dispose();
    _float.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final enter = CurvedAnimation(
      parent: _entrance,
      curve: Curves.easeOutCubic,
    );
    final drift = CurvedAnimation(parent: _float, curve: Curves.easeInOutSine);
    return FadeTransition(
      opacity: enter,
      child: ScaleTransition(
        scale: Tween(begin: 0.92, end: 1.0).animate(enter),
        child: AnimatedBuilder(
          animation: drift,
          builder: (context, child) => Transform.translate(
            key: const ValueKey('illustration-float'),
            offset: Offset(0, -10 * drift.value + 5),
            child: child,
          ),
          child: Image.asset(widget.asset, fit: BoxFit.contain),
        ),
      ),
    );
  }
}
