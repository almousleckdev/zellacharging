import 'package:flutter/material.dart';

abstract final class DirectionsLink {
  static Uri to(double latitude, double longitude, TargetPlatform platform) {
    final apple =
        platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
    final point = '$latitude,$longitude';
    if (apple) {
      return Uri.https('maps.apple.com', '/', {'daddr': point, 'dirflg': 'd'});
    }
    return Uri.https('www.google.com', '/maps/dir/', {
      'api': '1',
      'destination': point,
    });
  }
}
