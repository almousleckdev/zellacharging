import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/pay/action_bar.dart';
import '../../profile/presentation/profile_providers.dart';
import '../domain/mobile_operator.dart';
import '../domain/operator_hint.dart';
import '../domain/wallet.dart';
import 'wallet_providers.dart';
import 'withdraw_flow_controller.dart';
import 'withdraw_flow_state.dart';
import 'withdraw_flow_view.dart';
import 'withdraw_form.dart';

class WithdrawPage extends ConsumerStatefulWidget {
  const WithdrawPage({super.key});

  @override
  ConsumerState<WithdrawPage> createState() => _WithdrawPageState();
}

class _WithdrawPageState extends ConsumerState<WithdrawPage> {
  final _formKey = GlobalKey<FormState>();
  final _amount = TextEditingController();
  final _password = TextEditingController();
  MobileOperator? _operator;
  late String _attempt = _newAttempt();

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  @override
  void dispose() {
    _amount.dispose();
    _password.dispose();
    super.dispose();
  }

  MobileOperator _chosen(String phone) =>
      OperatorHint.resolve(phone, _operator ?? MobileOperator.airtel);

  void _submit(String phone) {
    if (!_formKey.currentState!.validate()) return;
    ref
        .read(withdrawFlowProvider.notifier)
        .withdraw(
          amountMinor: AmountInput.parse(_amount.text) ?? 0,
          operator: _chosen(phone),
          password: _password.text,
          idempotencyKey: _attempt,
        );
  }

  void _retry() {
    final error = ref.read(withdrawFlowProvider).error;
    if (error != null && !error.isUncertain) _attempt = _newAttempt();
    ref.read(withdrawFlowProvider.notifier).reset();
  }

  @override
  Widget build(BuildContext context) {
    final flow = ref.watch(withdrawFlowProvider);
    final wallet = ref.watch(walletProvider);
    final phone = ref.watch(profileProvider).asData?.value.phone ?? '';
    final idle = flow.phase == WithdrawPhase.idle;
    return Scaffold(
      appBar: AppBar(title: Text(WithdrawText.title)),
      body: idle
          ? AsyncBody(
              value: wallet,
              onRetry: () => ref.invalidate(walletProvider),
              builder: (data) => _form(data, phone),
            )
          : WithdrawFlowView(
              flow: flow,
              currency: wallet.asData?.value.currency ?? '',
              operator: _chosen(phone),
              phone: phone,
              onRetry: _retry,
              onClose: () => context.pop(),
            ),
      bottomNavigationBar: idle
          ? ActionBar(
              label: WithdrawText.withdrawAmount(
                MoneyFormat.format(
                  AmountInput.parse(_amount.text) ?? 0,
                  wallet.asData?.value.currency ?? '',
                  0,
                ),
              ),
              onPressed: () => _submit(phone),
            )
          : null,
    );
  }

  Widget _form(Wallet wallet, String phone) => WithdrawForm(
    formKey: _formKey,
    wallet: wallet,
    amount: _amount,
    password: _password,
    phone: phone,
    operator: _chosen(phone),
    onOperator: (value) => setState(() => _operator = value),
    onAmountChanged: () => setState(() {}),
  );
}
