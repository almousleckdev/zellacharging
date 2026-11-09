import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/format/connector_label.dart';

void main() {
  test('connectors are named with letters', () {
    expect(ConnectorLabel.of(1), 'A');
    expect(ConnectorLabel.of(2), 'B');
    expect(ConnectorLabel.of(26), 'Z');
  });

  test('an id outside the alphabet keeps its number', () {
    expect(ConnectorLabel.of(0), '0');
    expect(ConnectorLabel.of(27), '27');
  });
}
