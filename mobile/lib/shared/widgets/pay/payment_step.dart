enum StepStatus { done, active, pending }

class PaymentStep {
  const PaymentStep(this.label, this.state);

  final String label;
  final StepStatus state;
}
