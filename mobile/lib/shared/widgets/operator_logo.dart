import 'package:flutter/material.dart';

import '../../features/wallet/domain/mobile_operator.dart';

class OperatorLogo extends StatelessWidget {
  const OperatorLogo({super.key, required this.operator, this.size = 40});

  final MobileOperator operator;
  final double size;

  @override
  Widget build(BuildContext context) {
    return Image.asset(
      operator.logo,
      width: size,
      height: size,
      fit: BoxFit.contain,
      semanticLabel: operator.label,
    );
  }
}
