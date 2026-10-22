import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/network/api_error.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/app_toast.dart';
import '../../stations/presentation/station_providers.dart';

class ScanPage extends ConsumerStatefulWidget {
  const ScanPage({super.key});

  @override
  ConsumerState<ScanPage> createState() => _ScanPageState();
}

class _ScanPageState extends ConsumerState<ScanPage> {
  final _controller = MobileScannerController(
    formats: const [BarcodeFormat.qrCode],
  );
  bool _resolving = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _onDetect(BarcodeCapture capture) async {
    final content = capture.barcodes.firstOrNull?.rawValue;
    if (_resolving || content == null) return;
    setState(() {
      _resolving = true;
    });
    try {
      final resolved = await ref
          .read(stationRepositoryProvider)
          .resolveQr(content);
      if (!mounted) return;
      context.pushReplacement(
        Routes.chargeSetupPath(
          resolved.station.id,
          connectorId: resolved.connector.connectorId,
        ),
      );
    } on ApiError catch (error) {
      if (mounted) AppToast.error(context, error.message);
      await Future<void>.delayed(const Duration(seconds: 2));
      if (mounted) setState(() => _resolving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(StationText.scanTitle)),
      body: Stack(
        fit: StackFit.expand,
        children: [
          MobileScanner(
            controller: _controller,
            onDetect: _onDetect,
            errorBuilder: (_, _) => Center(
              child: Padding(
                padding: EdgeInsets.all(AppSpacing.lg),
                child: Text(
                  StationText.cameraDenied,
                  textAlign: TextAlign.center,
                ),
              ),
            ),
          ),
          Center(
            child: Container(
              width: 240,
              height: 240,
              decoration: BoxDecoration(
                border: Border.all(color: AppColors.primary, width: 3),
                borderRadius: BorderRadius.circular(AppSpacing.radius),
              ),
            ),
          ),
          Positioned(
            left: AppSpacing.md,
            right: AppSpacing.md,
            bottom: AppSpacing.xl,
            child: Text(
              StationText.scanHint,
              textAlign: TextAlign.center,
              style: TextStyle(color: AppColors.onPrimary),
            ),
          ),
        ],
      ),
    );
  }
}
