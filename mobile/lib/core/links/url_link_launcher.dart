import 'package:url_launcher/url_launcher.dart';

import 'link_launcher.dart';

class UrlLinkLauncher implements LinkLauncher {
  @override
  Future<bool> open(Uri link) =>
      launchUrl(link, mode: LaunchMode.externalApplication);
}
