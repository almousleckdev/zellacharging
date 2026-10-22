import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'preference_store.dart';

class SecurePreferenceStore implements PreferenceStore {
  SecurePreferenceStore([FlutterSecureStorage? storage])
    : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  @override
  Future<String?> read(String key) => _storage.read(key: key);

  @override
  Future<void> save(String key, String value) =>
      _storage.write(key: key, value: value);
}
