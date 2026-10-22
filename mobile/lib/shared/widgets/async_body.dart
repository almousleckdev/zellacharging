import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/strings.dart';
import '../../core/network/api_error.dart';
import '../../core/theme/app_spacing.dart';
import 'adaptive_spinner.dart';
import 'empty_state.dart';
import '../../core/theme/app_icons.dart';

class AsyncBody<T> extends StatelessWidget {
  const AsyncBody({
    super.key,
    required this.value,
    required this.onRetry,
    required this.builder,
    this.shrinkWrap = false,
  });

  final AsyncValue<T> value;
  final VoidCallback onRetry;
  final Widget Function(T data) builder;
  final bool shrinkWrap;

  @override
  Widget build(BuildContext context) {
    return value.when(
      skipLoadingOnReload: value.hasValue,
      data: builder,
      loading: () => const Padding(
        padding: EdgeInsets.all(AppSpacing.lg),
        child: Center(child: AdaptiveSpinner()),
      ),
      error: (error, _) => Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            EmptyState(
              icon: AppIcons.cloudOff,
              message: error is ApiError ? error.message : Strings.unknownError,
            ),
            const SizedBox(height: AppSpacing.sm),
            TextButton(onPressed: onRetry, child: Text(Strings.retry)),
          ],
        ),
      ),
    );
  }
}
