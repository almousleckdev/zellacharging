import 'photo_source.dart';
import 'picked_photo.dart';

abstract class PhotoPicker {
  Future<PickedPhoto?> pick(PhotoSource source);
}
