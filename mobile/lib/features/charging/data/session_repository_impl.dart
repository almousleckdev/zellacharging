import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/charging_session.dart';
import '../domain/session_repository.dart';
import 'session_mapper.dart';

class SessionRepositoryImpl implements SessionRepository {
  SessionRepositoryImpl(this.client);

  final Dio client;

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }

  Future<ChargingSession> _session(
    Future<Response<Map<String, dynamic>>> call,
  ) => _guard(() async => SessionMapper.session((await call).data!));

  @override
  Future<ChargingSession> start(
    String stationId,
    int connectorId,
    int amountMinor,
  ) => _session(
    client.post<Map<String, dynamic>>(
      '/v1/sessions',
      data: {
        'stationId': stationId,
        'connectorId': connectorId,
        'amountMinor': amountMinor,
      },
    ),
  );

  @override
  Future<ChargingSession> stop(String sessionId) => _session(
    client.post<Map<String, dynamic>>('/v1/sessions/$sessionId/stop'),
  );

  @override
  Future<ChargingSession> get(String sessionId) =>
      _session(client.get<Map<String, dynamic>>('/v1/sessions/$sessionId'));

  @override
  Future<ChargingSession?> active() => _guard(() async {
    final response = await client.get<Map<String, dynamic>>(
      '/v1/sessions/active',
    );
    final data = response.data;
    return data == null || data.isEmpty ? null : SessionMapper.session(data);
  });

  @override
  Future<List<ChargingSession>> history() => _guard(() async {
    final response = await client.get<List<dynamic>>('/v1/sessions');
    return response.data!
        .map((s) => SessionMapper.session(s as Map<String, dynamic>))
        .toList();
  });
}
