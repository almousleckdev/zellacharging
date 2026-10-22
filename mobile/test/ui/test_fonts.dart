import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

Future<void> loadTestFonts() async {
  final root = Platform.environment['FLUTTER_ROOT']!;
  final dir = '$root/bin/cache/artifacts/material_fonts';
  Future<void> load(String family, List<String> files) async {
    final loader = FontLoader(family);
    for (final file in files) {
      final bytes = File('$dir/$file').readAsBytesSync();
      loader.addFont(Future.value(ByteData.sublistView(bytes)));
    }
    await loader.load();
  }

  await load('Roboto', [
    'Roboto-Regular.ttf',
    'Roboto-Medium.ttf',
    'Roboto-Bold.ttf',
  ]);
  await load('MaterialIcons', ['MaterialIcons-Regular.otf']);

  await _loadPhosphor();
}

Future<void> _loadPhosphor() async {
  final config =
      jsonDecode(File('.dart_tool/package_config.json').readAsStringSync())
          as Map<String, dynamic>;
  final package = (config['packages'] as List).firstWhere(
    (entry) => entry['name'] == 'phosphor_icons',
  );
  final base = Uri.parse(package['rootUri'] as String).toFilePath();
  final root = base.endsWith('/') ? base : '$base/';
  const fonts = {
    'PhosphorRegular': 'Phosphor.ttf',
    'PhosphorFill': 'Phosphor-Fill.ttf',
    'PhosphorBold': 'Phosphor-Bold.ttf',
    'PhosphorDuotone': 'Phosphor-Duotone.ttf',
  };
  for (final entry in fonts.entries) {
    final loader = FontLoader('packages/phosphor_icons/${entry.key}');
    final bytes = File('${root}lib/fonts/${entry.value}').readAsBytesSync();
    loader.addFont(Future.value(ByteData.sublistView(bytes)));
    await loader.load();
  }
}
