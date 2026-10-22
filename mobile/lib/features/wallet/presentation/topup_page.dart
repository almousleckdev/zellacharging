import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../profile/presentation/profile_providers.dart';
import '../domain/mobile_operator.dart';
import 'amount_picker.dart';
import 'operator_selector.dart';
import 'wallet_providers.dart';
import '../../../core/theme/app_icons.dart';

class TopupPage extends ConsumerStatefulWidget {
  const TopupPage({super.key});

  @override
  ConsumerState<TopupPage> createState() => _TopupPageState();
}

class _TopupPageState extends ConsumerState<TopupPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _phone = TextEditingController(
    text: ref.read(profileProvider).asData?.value.phone ?? '',
  );
  int _amount = WalletText.topUpAmounts[1];
  MobileOperator _operator = MobileOperator.airtel;
  late String _attempt = _newAttempt();

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  @override
  void dispose() {
    _phone.dispose();
    super.dispose();
  }

  void _pay() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      final result = await ref
          .read(walletRepositoryProvider)
          .topUp(
            amountMinor: _amount,
            msisdn: _phone.text.trim(),
            operator: _operator,
            idempotencyKey: _attempt,
          );
      if (result.failed) {
        _attempt = _newAttempt();
        showError(WalletText.topUpFailed);
        return;
      }
      ref.invalidate(walletProvider);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            result.succeeded ? WalletText.topUpDone : WalletText.topUpPending,
          ),
        ),
      );
      context.pop();
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currency = ref.watch(walletProvider).asData?.value.currency ?? '';
    return Scaffold(
      appBar: AppBar(title: Text(WalletText.topUpTitle)),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          Text(
            WalletText.topUpBody,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          _heading(WalletText.chooseAmount),
          AmountPicker(
            amounts: WalletText.topUpAmounts,
            selected: _amount,
            currency: currency,
            onSelected: (value) => setState(() => _amount = value),
          ),
          const SizedBox(height: AppSpacing.lg),
          _heading(WalletText.chooseOperator),
          OperatorSelector(
            selected: _operator,
            onSelected: (value) => setState(() => _operator = value),
          ),
          const SizedBox(height: AppSpacing.lg),
          Form(
            key: _formKey,
            child: AppTextField(
              controller: _phone,
              label: WalletText.mobileMoneyNumberLabel,
              hint: WalletText.mobileMoneyNumberHint,
              icon: AppIcons.phone,
              validator: Validators.mobileMoneyNumber,
              keyboardType: TextInputType.phone,
              textInputAction: TextInputAction.done,
            ),
          ),
          const SizedBox(height: AppSpacing.xl),
          PrimaryButton(
            label: WalletText.payNow,
            loading: loading,
            onPressed: _pay,
          ),
        ],
      ),
    );
  }

  Widget _heading(String text) => Padding(
    padding: const EdgeInsets.only(bottom: AppSpacing.sm),
    child: Text(text, style: Theme.of(context).textTheme.titleSmall),
  );
}
