import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

class BottomSystemInset extends StatelessWidget {
  const BottomSystemInset({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform != TargetPlatform.android) return child;
    return SafeArea(top: false, left: false, right: false, child: child);
  }
}
