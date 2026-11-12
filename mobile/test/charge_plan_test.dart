import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/domain/charge_plan.dart';
import 'package:zella/features/charging/domain/session_quote.dart';
import 'package:zella/features/wallet/domain/wallet.dart';

const _wallet = Wallet(
  balanceMinor: 3000,
  reservedMinor: 500,
  availableMinor: 2500,
  currency: 'XAF',
  decimals: 0,
);

SessionQuote _quote(int amount, {int shortfall = 0, double vat = 0}) =>
    SessionQuote(
      amountMinor: amount,
      currency: 'XAF',
      decimals: 0,
      availableMinor: 2500,
      shortfallMinor: shortfall,
      minimumMinor: 1000,
      maximumMinor: 200000,
      vatPercent: vat,
      serviceFeePercent: 0,
      estimatedEnergyKwh: 12.5,
    );

const _freeWallet = Wallet(
  balanceMinor: 0,
  reservedMinor: 0,
  availableMinor: 0,
  currency: 'XAF',
  decimals: 0,
  freeCharging: true,
);

void main() {
  test('free charging never needs a payment or a top-up', () {
    final plan = ChargePlan.of(
      amountMinor: ChargePlan.maximumChargeMinor,
      wallet: _freeWallet,
    );
    expect(plan.freeCharging, isTrue);
    expect(plan.needsPayment, isFalse);
    expect(plan.needsTopup, isFalse);
  });

  test('an empty wallet without free charging still needs a top-up', () {
    final plan = ChargePlan.of(
      amountMinor: 0,
      wallet: const Wallet(
        balanceMinor: 0,
        reservedMinor: 0,
        availableMinor: 0,
        currency: 'XAF',
        decimals: 0,
      ),
    );
    expect(plan.needsTopup, isTrue);
  });

  test('the local plan covers what the wallet can and asks for the rest', () {
    final plan = ChargePlan.of(amountMinor: 4000, wallet: _wallet);
    expect(plan.fromWalletMinor, 2500);
    expect(plan.shortfallMinor, 1500);
    expect(plan.needsPayment, isTrue);
    expect(plan.payNowMinor, 1500);
  });

  test('a wallet that covers the budget needs no payment', () {
    final plan = ChargePlan.of(amountMinor: 2000, wallet: _wallet);
    expect(plan.needsPayment, isFalse);
    expect(plan.payNowMinor, 0);
  });

  test('a tiny shortfall is rounded up to the provider minimum', () {
    final plan = ChargePlan.of(amountMinor: 2600, wallet: _wallet);
    expect(plan.shortfallMinor, 100);
    expect(plan.payNowMinor, ChargePlan.minimumTopupMinor);
  });

  test('the server quote wins when it answers for the same amount', () {
    final plan = ChargePlan.of(
      amountMinor: 4000,
      wallet: _wallet,
      server: _quote(4000, shortfall: 1500, vat: 18),
    );
    expect(plan.maximumMinor, 200000);
    expect(plan.energyKwh, 12.5);
    expect(plan.hasFees, isTrue);
  });

  test('a stale server quote for another amount is ignored', () {
    final plan = ChargePlan.of(
      amountMinor: 4000,
      wallet: _wallet,
      server: _quote(2000),
    );
    expect(plan.maximumMinor, isNull);
    expect(plan.shortfallMinor, 1500);
  });

  test('without a wallet nothing is assumed to be covered', () {
    final plan = ChargePlan.of(amountMinor: 2000);
    expect(plan.needsPayment, isTrue);
    expect(plan.shortfallMinor, 2000);
  });
}
