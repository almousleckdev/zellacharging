import 'wallet_entry.dart';

class EntryDayGroup {
  const EntryDayGroup(this.day, this.entries);

  final DateTime day;
  final List<WalletEntry> entries;

  static List<EntryDayGroup> of(List<WalletEntry> entries) {
    final groups = <EntryDayGroup>[];
    final newestFirst = entries.where((e) => e.visible).toList()
      ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
    for (final entry in newestFirst) {
      final local = entry.createdAt.toLocal();
      final day = DateTime(local.year, local.month, local.day);
      if (groups.isNotEmpty && groups.last.day == day) {
        groups.last.entries.add(entry);
      } else {
        groups.add(EntryDayGroup(day, [entry]));
      }
    }
    return groups;
  }
}
