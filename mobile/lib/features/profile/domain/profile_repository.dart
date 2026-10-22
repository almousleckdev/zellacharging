import 'dart:typed_data';

import 'profile.dart';

abstract class ProfileRepository {
  Future<Profile> fetch();
  Future<Profile> updateName(String firstName, String lastName);
  Future<Uint8List?> fetchAvatar();
  Future<void> uploadAvatar(Uint8List bytes, String contentType);
  Future<void> deleteAvatar();
  Future<void> changePassword(String currentPassword, String newPassword);
  Future<void> deleteAccount(String password);
}
