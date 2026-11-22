import 'free_sunday_offer.dart';

abstract class FreeSundayRepository {
  Future<FreeSundayOffer> current();
}
