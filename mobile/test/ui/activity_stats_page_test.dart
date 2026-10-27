import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/presentation/session_history_card.dart';
import 'package:zella/features/history/domain/stat_metric.dart';
import 'package:zella/features/history/presentation/activity_stats_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the All period lists every charge and totals the spending', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const ActivityStatsPage(metric: StatMetric.spent),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 500));
    await tester.tap(find.text(StatText.periods.last));
    await tester.pump(const Duration(milliseconds: 500));

    expect(find.text(StatText.spentTitle), findsOneWidget);
    expect(find.text(StatText.periodCaptions.last), findsOneWidget);
    await tester.scrollUntilVisible(
      find.byType(SessionHistoryCard).first,
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.byType(SessionHistoryCard), findsWidgets);
    await closeApp(tester);
  });

  testWidgets('switching the period updates the caption', (tester) async {
    await pumpApp(
      tester,
      const ActivityStatsPage(metric: StatMetric.energy),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 500));
    await tester.tap(find.text(StatText.periods[0]));
    await tester.pump(const Duration(milliseconds: 300));
    expect(find.text(StatText.periodCaptions[0]), findsOneWidget);
    await closeApp(tester);
  });
}
