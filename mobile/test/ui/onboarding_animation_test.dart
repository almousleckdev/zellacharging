import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/features/auth/presentation/animated_illustration.dart';

const _asset = 'assets/images/onboarding_find.png';

Future<void> _show(WidgetTester tester, {bool reduceMotion = false}) =>
    tester.pumpWidget(
      MaterialApp(
        home: MediaQuery(
          data: MediaQueryData(disableAnimations: reduceMotion),
          child: const Scaffold(body: AnimatedIllustration(asset: _asset)),
        ),
      ),
    );

double _offset(WidgetTester tester) => tester
    .widget<Transform>(find.byKey(const ValueKey('illustration-float')))
    .transform
    .getTranslation()
    .y;

void main() {
  testWidgets('the illustration floats up and down', (tester) async {
    await _show(tester);
    await tester.pump(const Duration(milliseconds: 800));
    final first = _offset(tester);
    await tester.pump(const Duration(milliseconds: 800));
    final second = _offset(tester);

    expect(second, isNot(closeTo(first, 0.5)));
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('it stays still when the system asks to reduce motion', (
    tester,
  ) async {
    await _show(tester, reduceMotion: true);
    await tester.pump(const Duration(milliseconds: 800));
    final first = _offset(tester);
    await tester.pump(const Duration(milliseconds: 800));

    expect(_offset(tester), closeTo(first, 0.01));
    await tester.pumpWidget(const SizedBox());
  });
}
