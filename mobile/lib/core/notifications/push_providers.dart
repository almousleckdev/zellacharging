import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/network_providers.dart';
import 'device_registry.dart';
import 'firebase_push_messaging.dart';
import 'push_messaging.dart';
import 'push_registration.dart';

final pushMessagingProvider = Provider<PushMessaging>(
  (ref) => FirebasePushMessaging(FirebaseMessaging.instance),
);

final pushRegistrationProvider = Provider<PushRegistration>(
  (ref) => PushRegistration(
    ref.watch(pushMessagingProvider),
    DeviceRegistry(ref.watch(dioProvider)),
  ),
);
