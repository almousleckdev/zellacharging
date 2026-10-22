import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'image_photo_picker.dart';
import 'photo_picker.dart';

final photoPickerProvider = Provider<PhotoPicker>((ref) => ImagePhotoPicker());
