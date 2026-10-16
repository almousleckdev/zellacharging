import 'profile.dart';

abstract class ProfileRepository {
  Future<Profile> fetch();
}
