import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/wallet/presentation/wallet_entry_page.dart';
import 'package:zella/features/wallet/presentation/wallet_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the detail page shows the number, operator and references', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const WalletEntryPage(entryId: '1'),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 500));

    expect(find.text('+24162820745'), findsOneWidget);
    expect(find.text('Moov Money'), findsOneWidget);
    expect(find.text('GAA2FPSPEONV7JHFFMGU'), findsOneWidget);
    expect(find.text('MP26102600123'), findsOneWidget);
    expect(find.text(WalletHistoryText.balanceAfter), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('deleting asks for confirmation and then hides the entry', (
    tester,
  ) async {
    final wallet = FakeWallet();
    await pumpApp(
      tester,
      const WalletEntryPage(entryId: '1'),
      overrides: overridesWith(wallet: wallet),
    );
    await tester.pump(const Duration(milliseconds: 500));
    await tester.tap(find.byTooltip(WalletHistoryText.delete));
    await tester.pump(const Duration(milliseconds: 400));

    expect(find.text(WalletHistoryText.deleteTitle), findsOneWidget);
    expect(wallet.hidden, isEmpty);
    await tester.tap(find.text(WalletHistoryText.delete).last);
    await tester.pump(const Duration(milliseconds: 500));

    expect(wallet.hidden, ['1']);
    await closeApp(tester);
  });

  testWidgets('cancelling the confirmation keeps the entry', (tester) async {
    final wallet = FakeWallet();
    await pumpApp(
      tester,
      const WalletEntryPage(entryId: '1'),
      overrides: overridesWith(wallet: wallet),
    );
    await tester.pump(const Duration(milliseconds: 500));
    await tester.tap(find.byTooltip(WalletHistoryText.delete));
    await tester.pump(const Duration(milliseconds: 400));
    await tester.tap(find.text(Strings.cancel));
    await tester.pump(const Duration(milliseconds: 400));

    expect(wallet.hidden, isEmpty);
    await closeApp(tester);
  });

  testWidgets('the history is grouped by day with Today and Yesterday', (
    tester,
  ) async {
    await pumpApp(tester, const WalletPage(), overrides: overrides());
    await tester.pump(const Duration(milliseconds: 500));
    expect(find.text(WithdrawText.entryLabel('WITHDRAWAL')), findsOneWidget);
    expect(find.text(WithdrawText.entryLabel('CAPTURE')), findsOneWidget);
    await closeApp(tester);
  });
}
