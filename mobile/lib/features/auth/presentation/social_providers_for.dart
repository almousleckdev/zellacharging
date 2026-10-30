import 'package:flutter/material.dart';

import '../domain/social_provider.dart';

List<SocialProvider> socialProvidersFor(BuildContext context) {
  final platform = Theme.of(context).platform;
  final onApple =
      platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
  return [SocialProvider.google, if (onApple) SocialProvider.apple];
}
