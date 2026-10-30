import '../../../core/format/date_format.dart';
import '../../../core/l10n/strings.dart';
import '../domain/app_notification.dart';

class NotificationGroup {
  const NotificationGroup(this.label, this.items);

  final String label;
  final List<AppNotification> items;
}

abstract final class NotificationGroups {
  static List<NotificationGroup> of(List<AppNotification> items, DateTime now) {
    final groups = <NotificationGroup>[];
    DateTime? current;
    for (final item in items) {
      final local = item.createdAt.toLocal();
      final day = DateTime(local.year, local.month, local.day);
      if (day != current) {
        groups.add(NotificationGroup(_label(day, now), []));
        current = day;
      }
      groups.last.items.add(item);
    }
    return groups;
  }

  static String _label(DateTime day, DateTime now) {
    final today = DateTime(now.year, now.month, now.day);
    final gap = today.difference(day).inDays;
    if (gap == 0) return NotificationText.today;
    if (gap == 1) return NotificationText.yesterday;
    return DateFormatter.date(day);
  }
}
