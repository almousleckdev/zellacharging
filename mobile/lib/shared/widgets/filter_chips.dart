import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';

class FilterChips extends StatelessWidget {
  const FilterChips({
    super.key,
    required this.labels,
    required this.selected,
    required this.onSelected,
  });

  final List<String> labels;
  final int selected;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
        itemCount: labels.length,
        separatorBuilder: (_, _) => const SizedBox(width: AppSpacing.sm),
        itemBuilder: (_, index) => ChoiceChip(
          label: Text(labels[index]),
          selected: index == selected,
          onSelected: (_) => onSelected(index),
          selectedColor: AppColors.primary,
          labelStyle: TextStyle(
            color: index == selected ? Colors.white : AppColors.textPrimary,
          ),
          showCheckmark: false,
          side: BorderSide(
            color: index == selected ? AppColors.primary : AppColors.border,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
          ),
          backgroundColor: AppColors.surface,
        ),
      ),
    );
  }
}
