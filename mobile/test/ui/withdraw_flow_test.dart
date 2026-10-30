import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/withdraw_money.dart';
import 'package:zella/features/wallet/domain/withdrawal_result.dart';
import 'package:zella/features/wallet/presentation/wallet_page.dart';
import 'package:zella/features/wallet/presentation/withdraw_flow_controller.dart';
import 'package:zella/features/wallet/presentation/withdraw_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _ScriptedWithdrawals extends FakeWallet {
  _ScriptedWithdrawals(this.states);

  final List<String> states;
  String? lastMsisdn;
  var polls = 0;
  var created = 0;
  String? lastPassword;

  @override
  Future<WithdrawalResult> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String msisdn,
    required String password,
    required String idempotencyKey,
  }) async {
    created++;
    lastPassword = password;
    lastMsisdn = msisdn;
    return WithdrawalResult(id: 'w', state: states.first);
  }

  @override
  Future<WithdrawalResult> withdrawalStatus(String id) async {
    polls++;
    return WithdrawalResult(
      id: id,
      state: states[polls.clamp(0, states.length - 1)],
    );
  }
}

Future<_ScriptedWithdrawals> _openWithdraw(
  WidgetTester tester,
  List<String> states,
) async {
  final wallet = _ScriptedWithdrawals(states);
  await pumpApp(
    tester,
    const WithdrawPage(),
    overrides: overridesWith(
      wallet: wallet,
      extra: [
        withdrawMoneyProvider.overrideWithValue(
          WithdrawMoney(wallet, pollInterval: Duration.zero, maxPolls: 3),
        ),
      ],
    ),
  );
  await tester.pump(const Duration(milliseconds: 500));
  await tester.enterText(find.byType(TextField).first, '2000');
  await _typePassword(tester);
  return wallet;
}

Future<void> _typePassword(WidgetTester tester) async {
  await tester.dragUntilVisible(
    find.byType(TextFormField).last,
    find.byType(ListView),
    const Offset(0, -300),
  );
  await tester.enterText(find.byType(TextFormField).last, 'MyPassword1');
}

Future<void> _tapWithdraw(WidgetTester tester) async {
  await tester.tap(find.byType(FilledButton));
  for (var i = 0; i < 6; i++) {
    await tester.pump(const Duration(milliseconds: 100));
  }
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a paid withdrawal ends on the success panel', (tester) async {
    final wallet = await _openWithdraw(tester, ['PROCESSING', 'PAID']);
    await _tapWithdraw(tester);

    expect(find.text(WithdrawText.doneTitle), findsOneWidget);
    expect(wallet.lastPassword, 'MyPassword1');
    await closeApp(tester);
  });

  testWidgets('a request waits for approval instead of polling the provider', (
    tester,
  ) async {
    final wallet = await _openWithdraw(tester, ['PENDING_APPROVAL']);
    await _tapWithdraw(tester);

    expect(find.text(WithdrawText.pendingTitle), findsOneWidget);
    expect(find.text(WithdrawText.doneTitle), findsNothing);
    expect(wallet.polls, 0);
    expect(wallet.lastMsisdn, '+24177123456');
    await closeApp(tester);
  });

  testWidgets('a number that never topped up the wallet is refused', (
    tester,
  ) async {
    final wallet = await _openWithdraw(tester, ['PAID']);
    await tester.enterText(find.byType(TextFormField).at(1), '077999999');
    await _tapWithdraw(tester);

    expect(find.text(WithdrawText.numberNotAllowed), findsOneWidget);
    expect(wallet.created, 0);
    await closeApp(tester);
  });

  testWidgets('tapping a known number fills the field', (tester) async {
    await _openWithdraw(tester, ['PAID']);
    await tester.enterText(find.byType(TextFormField).at(1), '');
    await tester.tap(find.text('+24177••••56').first);
    await tester.pump();

    expect(
      tester
          .widget<TextFormField>(find.byType(TextFormField).at(1))
          .controller
          ?.text,
      '+24177123456',
    );
    await closeApp(tester);
  });

  testWidgets('there are no preset amounts', (tester) async {
    await _openWithdraw(tester, ['PAID']);

    expect(find.text('1 000'), findsNothing);
    expect(find.text('5 000'), findsNothing);
    expect(find.text('10 000'), findsNothing);
    await closeApp(tester);
  });

  testWidgets('the smallest withdrawal is 500', (tester) async {
    final wallet = await _openWithdraw(tester, ['PAID']);
    await tester.enterText(find.byType(TextFormField).first, '499');
    await _tapWithdraw(tester);
    expect(find.text(WithdrawText.invalidAmount), findsOneWidget);
    expect(wallet.created, 0);

    await tester.enterText(find.byType(TextFormField).first, '500');
    await _tapWithdraw(tester);
    expect(wallet.created, 1);
    await closeApp(tester);
  });

  testWidgets('a failed withdrawal offers a retry and keeps one request', (
    tester,
  ) async {
    final wallet = await _openWithdraw(tester, ['FAILED']);
    await _tapWithdraw(tester);

    expect(find.text(WithdrawText.failedTitle), findsOneWidget);
    expect(find.text(PayFlowText.retry), findsOneWidget);
    expect(wallet.created, 1);
    await closeApp(tester);
  });

  testWidgets('an unconfirmed withdrawal is shown as being verified', (
    tester,
  ) async {
    await _openWithdraw(tester, ['REVIEW']);
    await _tapWithdraw(tester);

    expect(find.text(WithdrawText.verifyingTitle), findsOneWidget);
    expect(find.text(WithdrawText.failedTitle), findsNothing);
    expect(find.text(PayFlowText.retry), findsNothing);
    await closeApp(tester);
  });

  testWidgets('a withdrawal above the balance is refused before any request', (
    tester,
  ) async {
    final wallet = _ScriptedWithdrawals(['PAID']);
    await pumpApp(
      tester,
      const WithdrawPage(),
      overrides: overridesWith(wallet: wallet),
    );
    await tester.pump(const Duration(milliseconds: 500));
    await tester.enterText(find.byType(TextField).first, '5000000');
    await _typePassword(tester);
    await tester.tap(find.byType(FilledButton));
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text(WithdrawText.invalidAmount), findsOneWidget);
    expect(wallet.created, 0);
    await closeApp(tester);
  });

  testWidgets('the wallet shows Retirer next to Créditer', (tester) async {
    await pumpApp(
      tester,
      const WalletPage(),
      overrides: overridesWith(wallet: _ScriptedWithdrawals(['PAID'])),
    );
    expect(find.text(WithdrawText.action), findsOneWidget);
    expect(find.text(WithdrawText.entryLabel('CAPTURE')), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the withdraw button stays visible and explains when it is off', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const WalletPage(),
      overrides: overridesWith(wallet: _WithdrawalsOff()),
    );

    expect(find.text(WithdrawText.action), findsOneWidget);
    await tester.tap(find.text(WithdrawText.action));
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text(WithdrawText.unavailable), findsOneWidget);
    await closeApp(tester);
  });
}

class _WithdrawalsOff extends FakeWallet {
  @override
  Future<Wallet> fetch() async {
    final base = await super.fetch();
    return Wallet(
      balanceMinor: base.balanceMinor,
      reservedMinor: base.reservedMinor,
      availableMinor: base.availableMinor,
      currency: base.currency,
      decimals: base.decimals,
    );
  }
}
