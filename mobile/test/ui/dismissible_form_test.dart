import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/shared/widgets/dismissible_form.dart';

void main() {
  Future<void> pumpForm(WidgetTester tester) => tester.pumpWidget(
    MaterialApp(
      home: Scaffold(
        body: DismissibleForm(
          formKey: GlobalKey<FormState>(),
          children: const [
            TextField(key: Key('field')),
            SizedBox(height: 600),
          ],
        ),
      ),
    ),
  );

  bool focused(WidgetTester tester) => tester
      .widget<EditableText>(find.byType(EditableText, skipOffstage: false))
      .focusNode
      .hasFocus;

  testWidgets('tapping outside the field closes the keyboard', (tester) async {
    await pumpForm(tester);
    await tester.tap(find.byKey(const Key('field')));
    await tester.pump();
    expect(focused(tester), isTrue);

    await tester.tapAt(const Offset(200, 500));
    await tester.pump();

    expect(focused(tester), isFalse);
  });

  testWidgets('dragging the form closes the keyboard', (tester) async {
    await pumpForm(tester);
    await tester.tap(find.byKey(const Key('field')));
    await tester.pump();

    await tester.drag(find.byType(ListView), const Offset(0, -100));
    await tester.pump();

    expect(focused(tester), isFalse);
  });
}
