import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/links/link_providers.dart';
import '../../../shared/app_toast.dart';
import 'support_providers.dart';

Future<void> openAgreement(BuildContext context, WidgetRef ref) async {
  final link = (await ref.read(supportProvider.future)).agreementUrl;
  final opened =
      link != null &&
      await ref.read(linkLauncherProvider).open(Uri.parse(link));
  if (!opened && context.mounted) {
    AppToast.warning(context, ProfileText.agreementUnavailable);
  }
}
