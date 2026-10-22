class PickerOption<T> {
  const PickerOption({
    required this.label,
    required this.value,
    this.destructive = false,
  });

  final String label;
  final T value;
  final bool destructive;
}
