import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final mapTileProvider = Provider<TileProvider>((ref) => NetworkTileProvider());
