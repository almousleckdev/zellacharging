import '../../../core/location/coordinates.dart';
import 'resolved_connector.dart';
import 'station.dart';

abstract class StationRepository {
  Future<List<Station>> nearby(Coordinates? around);
  Future<Station> byId(String id);
  Future<ResolvedConnector> resolveQr(String content);
}
