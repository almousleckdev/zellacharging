import 'support_info.dart';

abstract class SupportRepository {
  Future<SupportInfo> fetch();
}
