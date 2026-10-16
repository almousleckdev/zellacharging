import 'charging_session.dart';

abstract class SessionRepository {
  Future<ChargingSession> start(
    String stationId,
    int connectorId,
    int amountMinor,
  );
  Future<ChargingSession> stop(String sessionId);
  Future<ChargingSession> get(String sessionId);
  Future<ChargingSession?> active();
  Future<List<ChargingSession>> history();
}
