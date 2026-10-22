import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/shared/widgets/adaptive_spinner.dart';

Future<void> _show(WidgetTester tester, TargetPlatform platform) =>
    tester.pumpWidget(
      MaterialApp(
        theme: ThemeData(platform: platform),
        home: const Scaffold(body: Center(child: AdaptiveSpinner())),
      ),
    );

void main() {
  testWidgets('iOS shows the Cupertino spinner only', (tester) async {
    await _show(tester, TargetPlatform.iOS);

    expect(find.byType(CupertinoActivityIndicator), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('Android shows the Material spinner only', (tester) async {
    await _show(tester, TargetPlatform.android);

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    expect(find.byType(CupertinoActivityIndicator), findsNothing);
    await tester.pumpWidget(const SizedBox());
  });
}
