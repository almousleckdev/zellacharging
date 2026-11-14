import 'dart:typed_data';

import '../../auth/domain/social_credential.dart';
import '../../../shared/otp_delivery.dart';
import 'profile.dart';

abstract class ProfileRepository {
  Future<Profile> fetch();
  Future<Profile> updateName(String firstName, String lastName);
  Future<void> addEmail(String email);
  Future<void> sendEmailCode();
  Future<Profile> confirmEmail(String code);
  Future<void> addPhone(String phone, OtpDelivery delivery);
  Future<void> sendPhoneCode(OtpDelivery delivery);
  Future<Profile> confirmPhone(String code);
  Future<Uint8List?> fetchAvatar();
  Future<void> uploadAvatar(Uint8List bytes, String contentType);
  Future<void> deleteAvatar();
  Future<void> changePassword(String currentPassword, String newPassword);
  Future<void> deleteAccount({String? password, SocialCredential? credential});
}
