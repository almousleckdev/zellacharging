import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'link_launcher.dart';
import 'url_link_launcher.dart';

final linkLauncherProvider = Provider<LinkLauncher>((ref) => UrlLinkLauncher());
