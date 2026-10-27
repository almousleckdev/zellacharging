import 'charging_session.dart';
import 'session_quote.dart';

abstract class SessionRepository {
  Future<ChargingSession> start(
    String stationId,
    int connectorId,
    int amountMinor, {
    required String idempotencyKey,
  });
  Future<SessionQuote> quote(
    String stationId,
    int connectorId,
    int amountMinor,
  );
  Future<ChargingSession> stop(String sessionId);
  Future<ChargingSession> get(String sessionId);
  Future<ChargingSession?> active();
  Future<List<ChargingSession>> history();
  Future<void> delete(String sessionId);
}
