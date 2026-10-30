import 'package:flutter/material.dart';

class ZellaLogo extends StatelessWidget {
  const ZellaLogo({super.key, this.height = 44});

  final double height;

  static const asset = 'assets/images/logo.png';

  @override
  Widget build(BuildContext context) =>
      Image.asset(asset, height: height, fit: BoxFit.contain);
}
