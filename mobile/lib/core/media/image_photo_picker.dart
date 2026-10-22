import 'package:image_picker/image_picker.dart';

import 'photo_picker.dart';
import 'photo_source.dart';
import 'picked_photo.dart';

class ImagePhotoPicker implements PhotoPicker {
  ImagePhotoPicker([ImagePicker? picker]) : _picker = picker ?? ImagePicker();

  static const _side = 640.0;
  static const _quality = 85;

  final ImagePicker _picker;

  @override
  Future<PickedPhoto?> pick(PhotoSource source) async {
    final file = await _picker.pickImage(
      source: source == PhotoSource.camera
          ? ImageSource.camera
          : ImageSource.gallery,
      maxWidth: _side,
      maxHeight: _side,
      imageQuality: _quality,
    );
    if (file == null) return null;
    return PickedPhoto(
      bytes: await file.readAsBytes(),
      contentType: file.mimeType ?? _typeOf(file.path),
    );
  }

  String _typeOf(String path) {
    final lower = path.toLowerCase();
    if (lower.endsWith('.png')) return 'image/png';
    if (lower.endsWith('.webp')) return 'image/webp';
    return 'image/jpeg';
  }
}
