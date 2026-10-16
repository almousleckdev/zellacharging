import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import 'bottom_bar_item.dart';
import 'charge_bar_button.dart';

class ShellPage extends StatelessWidget {
  const ShellPage({super.key, required this.navigationShell});

  final StatefulNavigationShell navigationShell;

  void _go(int branch) => navigationShell.goBranch(
    branch,
    initialLocation: branch == navigationShell.currentIndex,
  );

  BottomBarItem _item(int branch, IconData icon, String label) => BottomBarItem(
    icon: icon,
    label: label,
    selected: navigationShell.currentIndex == branch,
    onTap: () => _go(branch),
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: DecoratedBox(
        decoration: const BoxDecoration(
          color: AppColors.surface,
          border: Border(top: BorderSide(color: AppColors.border)),
        ),
        child: SafeArea(
          child: Row(
            children: [
              _item(0, Icons.home_outlined, Strings.home),
              _item(1, Icons.location_on_outlined, Strings.locations),
              ChargeBarButton(onTap: () => context.push(Routes.scan)),
              _item(2, Icons.bar_chart_rounded, Strings.history),
              _item(3, Icons.settings_outlined, Strings.profile),
            ],
          ),
        ),
      ),
    );
  }
}
