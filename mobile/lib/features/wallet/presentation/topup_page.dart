import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/format/money_format.dart';
import '../../../core/format/msisdn.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/pay/action_bar.dart';
import '../../profile/presentation/profile_providers.dart';
import '../domain/mobile_operator.dart';
import '../domain/operator_hint.dart';
import 'topup_flow_controller.dart';
import 'topup_flow_state.dart';
import 'topup_flow_view.dart';
import 'topup_form.dart';
import 'wallet_providers.dart';

class TopupPage extends ConsumerStatefulWidget {
  const TopupPage({super.key});

  @override
  ConsumerState<TopupPage> createState() => _TopupPageState();
}

class _TopupPageState extends ConsumerState<TopupPage> {
  final _formKey = GlobalKey<FormState>();
  final _amount = TextEditingController();
  late final _phone = TextEditingController(
    text: ref.read(profileProvider).asData?.value.phone ?? '',
  );
  MobileOperator _operator = MobileOperator.airtel;
  late String _attempt = _newAttempt();

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  @override
  void dispose() {
    _amount.dispose();
    _phone.dispose();
    super.dispose();
  }

  void _pay() {
    if (!_formKey.currentState!.validate()) return;
    ref
        .read(topupFlowProvider.notifier)
        .pay(
          amountMinor: AmountInput.parse(_amount.text) ?? 0,
          msisdn: Msisdn.normalize(_phone.text),
          operator: OperatorHint.resolve(_phone.text, _operator),
          idempotencyKey: _attempt,
        );
  }

  void _retry() {
    final error = ref.read(topupFlowProvider).error;
    if (error != null && !error.isUncertain) _attempt = _newAttempt();
    ref.read(topupFlowProvider.notifier).reset();
  }

  @override
  Widget build(BuildContext context) {
    final flow = ref.watch(topupFlowProvider);
    final wallet = ref.watch(walletProvider).value;
    final currency = wallet?.currency ?? '';
    final idle = flow.phase == TopupPhase.idle;
    return Scaffold(
      appBar: AppBar(title: Text(WalletText.topUpTitle)),
      body: idle
          ? TopupForm(
              formKey: _formKey,
              wallet: wallet,
              currency: currency,
              amount: _amount,
              phone: _phone,
              operator: OperatorHint.resolve(_phone.text, _operator),
              onOperator: (value) => setState(() => _operator = value),
              onAmountChanged: () => setState(() {}),
            )
          : TopupFlowView(
              flow: flow,
              currency: currency,
              operator: OperatorHint.resolve(_phone.text, _operator),
              phone: _phone.text.trim(),
              onRetry: _retry,
              onClose: () => context.pop(),
            ),
      bottomNavigationBar: idle
          ? ActionBar(
              label: PayText.payAmount(
                MoneyFormat.format(
                  AmountInput.parse(_amount.text) ?? 0,
                  currency,
                  0,
                ),
              ),
              onPressed: _pay,
            )
          : null,
    );
  }
}
