import '../../../core/l10n/strings.dart';
import '../domain/contact_kind.dart';
import '../domain/profile.dart';

enum ContactStep {
  addPhone(ContactKind.phone, false),
  verifyPhone(ContactKind.phone, true),
  addEmail(ContactKind.email, false),
  verifyEmail(ContactKind.email, true);

  const ContactStep(this.kind, this.existing);

  final ContactKind kind;
  final bool existing;

  static ContactStep? next(Profile profile) {
    if (profile.phone == null) return addPhone;
    if (!profile.phoneVerified) return verifyPhone;
    if (profile.email == null) return addEmail;
    if (!profile.emailVerified) return verifyEmail;
    return null;
  }

  String get label => switch (this) {
    addPhone => ContactGateText.addPhone,
    verifyPhone => ContactGateText.verifyPhone,
    addEmail => ContactGateText.addEmail,
    verifyEmail => ContactGateText.verifyEmail,
  };
}
