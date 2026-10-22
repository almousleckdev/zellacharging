import 'dart:typed_data';

class PickedPhoto {
  const PickedPhoto({required this.bytes, required this.contentType});

  final Uint8List bytes;
  final String contentType;
}
