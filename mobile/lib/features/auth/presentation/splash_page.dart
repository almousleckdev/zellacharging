import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'splash_state.dart';
import 'widgets/splash_logo.dart';
import 'widgets/splash_progress.dart';

const _scene = 'assets/images/splashcreen.png';
const _duration = Duration(milliseconds: 2600);

class SplashPage extends ConsumerStatefulWidget {
  const SplashPage({super.key});

  @override
  ConsumerState<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends ConsumerState<SplashPage>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: _duration,
  );

  @override
  void initState() {
    super.initState();
    _controller.forward().then((_) => _finish());
  }

  Future<void> _finish() async {
    await ref.read(onboardingSeenProvider.future);
    while (mounted &&
        ref.read(sessionControllerProvider) == SessionStatus.unknown) {
      await Future<void>.delayed(const Duration(milliseconds: 80));
    }
    if (mounted) ref.read(splashDoneProvider.notifier).finish();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: AppColors.splash,
      body: Stack(
        fit: StackFit.expand,
        children: [
          Align(
            alignment: Alignment.bottomCenter,
            child: ShaderMask(
              blendMode: BlendMode.dstIn,
              shaderCallback: (bounds) => const LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [Colors.transparent, Colors.black],
                stops: [0, 0.3],
              ).createShader(bounds),
              child: Image.asset(
                _scene,
                width: double.infinity,
                fit: BoxFit.fitWidth,
              ),
            ),
          ),
          SafeArea(
            child: Column(
              children: [
                const Spacer(flex: 4),
                SplashLogo(progress: _controller),
                const SizedBox(height: AppSpacing.md),
                FadeTransition(
                  opacity: CurvedAnimation(
                    parent: _controller,
                    curve: const Interval(0.35, 0.7),
                  ),
                  child: Text(
                    AuthText.tagline,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: AppColors.onPrimary,
                      fontSize: 13,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                SplashProgress(progress: _controller),
                const Spacer(flex: 5),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
