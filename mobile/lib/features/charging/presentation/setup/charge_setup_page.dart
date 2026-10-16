import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/router/routes.dart';
import '../../../../core/network/api_error.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/submit_mixin.dart';
import '../../../../shared/widgets/async_body.dart';
import '../../../../shared/widgets/error_banner.dart';
import '../../../../shared/widgets/primary_button.dart';
import '../../../../shared/widgets/section_title.dart';
import '../../../profile/presentation/profile_providers.dart';
import '../../../stations/domain/station.dart';
import '../../../stations/presentation/station_providers.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_quote.dart';
import '../../domain/charge_request.dart';
import '../../domain/session_state.dart';
import '../session_providers.dart';
import 'amount_section.dart';
import 'connector_picker.dart';
import 'payment_section.dart';

class ChargeSetupPage extends ConsumerStatefulWidget {
  const ChargeSetupPage({
    super.key,
    required this.stationId,
    this.initialConnectorId,
  });

  final String stationId;
  final int? initialConnectorId;

  @override
  ConsumerState<ChargeSetupPage> createState() => _ChargeSetupPageState();
}

class _ChargeSetupPageState extends ConsumerState<ChargeSetupPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _amount = TextEditingController(text: '2000');
  late final _phone = TextEditingController(
    text: ref.read(profileProvider).asData?.value.phone ?? '',
  );
  late int? _connector = widget.initialConnectorId;
  MobileOperator _operator = MobileOperator.airtel;
  late String _attempt = _newAttempt();

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  @override
  void dispose() {
    _amount.dispose();
    _phone.dispose();
    super.dispose();
  }

  ChargeQuote get _quote => ChargeQuote.of(
    int.tryParse(_amount.text) ?? 0,
    ref.watch(walletProvider).asData?.value,
  );

  void _start() {
    final connector = _connector;
    if (connector == null) {
      setState(() => errorMessage = Strings.selectConnector);
      return;
    }
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      try {
        final session = await ref
            .read(startChargeProvider)
            .call(
              ChargeRequest(
                stationId: widget.stationId,
                connectorId: connector,
                amountMinor: int.parse(_amount.text),
                attemptKey: _attempt,
                payment: _quote.needsPayment
                    ? ChargePayment(
                        operator: _operator,
                        msisdn: _phone.text.trim(),
                      )
                    : null,
              ),
            );
        ref.invalidate(walletProvider);
        ref.invalidate(activeSessionProvider);
        if (!mounted) return;
        if (session.state == SessionState.failed) {
          throw ApiError.fromCode(session.failureCode);
        }
        context.pushReplacement(Routes.chargingPath(session.id));
      } on ApiError {
        _attempt = _newAttempt();
        ref.invalidate(walletProvider);
        rethrow;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final station = ref.watch(stationProvider(widget.stationId));
    final currency = ref.watch(walletProvider).asData?.value.currency ?? '';
    return Scaffold(
      appBar: AppBar(title: const Text(Strings.setupTitle)),
      body: AsyncBody(
        value: station,
        onRetry: () => ref.invalidate(stationProvider(widget.stationId)),
        builder: (data) => _form(data, currency),
      ),
    );
  }

  Widget _form(Station station, String currency) {
    final quote = _quote;
    return Form(
      key: _formKey,
      child: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          Text(
            station.id,
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: AppSpacing.lg),
          if (errorMessage != null) ...[
            ErrorBanner(message: errorMessage!),
            const SizedBox(height: AppSpacing.md),
          ],
          const SectionTitle(title: Strings.setupAmountQuestion),
          const SizedBox(height: AppSpacing.md),
          AmountSection(
            controller: _amount,
            currency: currency,
            onChanged: () => setState(() {}),
          ),
          const SizedBox(height: AppSpacing.lg),
          const SectionTitle(title: Strings.chooseConnector),
          const SizedBox(height: AppSpacing.md),
          ConnectorPicker(
            connectors: station.connectors,
            selected: _connector,
            onSelected: (value) => setState(() => _connector = value),
          ),
          const SizedBox(height: AppSpacing.lg),
          const SectionTitle(title: Strings.payment),
          const SizedBox(height: AppSpacing.md),
          PaymentSection(
            quote: quote,
            currency: currency,
            operator: _operator,
            phone: _phone,
            onOperator: (value) => setState(() => _operator = value),
          ),
          const SizedBox(height: AppSpacing.xl),
          PrimaryButton(
            label: quote.needsPayment
                ? '${Strings.startAndPay} · ${MoneyFormat.format(quote.amountMinor, currency, 0)}'
                : Strings.startOnly,
            loading: loading,
            onPressed: _start,
          ),
        ],
      ),
    );
  }
}
