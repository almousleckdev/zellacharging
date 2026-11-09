class ConnectorLabel {
  const ConnectorLabel._();

  static const _alphabetLength = 26;

  static String of(int connectorId) {
    if (connectorId < 1 || connectorId > _alphabetLength) return '$connectorId';
    return String.fromCharCode('A'.codeUnitAt(0) + connectorId - 1);
  }
}
