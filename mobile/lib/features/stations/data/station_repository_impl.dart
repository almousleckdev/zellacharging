import 'package:dio/dio.dart';

import '../../../core/location/coordinates.dart';
import '../../../core/network/api_error.dart';
import '../domain/resolved_connector.dart';
import '../domain/station.dart';
import '../domain/station_repository.dart';
import 'station_mapper.dart';

class StationRepositoryImpl implements StationRepository {
  StationRepositoryImpl(this.client);

  static const _searchRadiusKm = 50;

  final Dio client;

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }

  @override
  Future<List<Station>> nearby(Coordinates? around) => _guard(() async {
    final response = await client.get<List<dynamic>>(
      '/v1/stations',
      queryParameters: around == null
          ? null
          : {
              'lat': around.latitude,
              'lng': around.longitude,
              'radiusKm': _searchRadiusKm,
            },
    );
    return response.data!
        .map((s) => StationMapper.station(s as Map<String, dynamic>))
        .toList();
  });

  @override
  Future<Station> byId(String id) => _guard(() async {
    final response = await client.get<Map<String, dynamic>>(
      '/v1/stations/${Uri.encodeComponent(id)}',
    );
    return StationMapper.station(response.data!);
  });

  @override
  Future<ResolvedConnector> resolveQr(String content) => _guard(() async {
    final response = await client.post<Map<String, dynamic>>(
      '/v1/qr/resolve',
      data: {'content': content},
    );
    final json = response.data!;
    return ResolvedConnector(
      station: StationMapper.station(json['station'] as Map<String, dynamic>),
      connector: StationMapper.connector(
        json['connector'] as Map<String, dynamic>,
      ),
    );
  });
}
