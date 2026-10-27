class SummaryLine {
  const SummaryLine(this.label, this.value, {this.emphasis = false});

  final String label;
  final String value;
  final bool emphasis;
}
