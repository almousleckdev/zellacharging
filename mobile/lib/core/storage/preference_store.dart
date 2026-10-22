abstract class PreferenceStore {
  Future<String?> read(String key);
  Future<void> save(String key, String value);
}
