import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/l10n/app_language.dart';
import 'core/l10n/language_controller.dart';
import 'core/l10n/strings.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';

class NdzelaApp extends ConsumerWidget {
  const NdzelaApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final language = ref.watch(languageProvider);
    return MaterialApp.router(
      key: ValueKey(language),
      title: Strings.appName,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(),
      locale: language.locale,
      supportedLocales: [for (final item in AppLanguage.values) item.locale],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      routerConfig: ref.watch(routerProvider),
    );
  }
}
