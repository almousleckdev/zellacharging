import '../router/routes.dart';
import 'push_message.dart';

String? routeFor(PushMessage message) {
  final id = message.data['id'];
  return switch (message.data['screen']) {
    'wallet' => Routes.wallet,
    'charging' when id != null && id.isNotEmpty => Routes.chargingPath(id),
    'settings' => Routes.settings,
    _ => null,
  };
}
