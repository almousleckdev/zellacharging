import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'suspension_notice.dart';

class SuspendedOnSignOut extends Notifier<SuspensionNotice?> {
  @override
  SuspensionNotice? build() => null;

  void mark(int? owedMinor) => state = SuspensionNotice(owedMinor);

  void acknowledge() => state = null;
}

final suspensionNoticeProvider =
    NotifierProvider<SuspendedOnSignOut, SuspensionNotice?>(
      SuspendedOnSignOut.new,
    );
