import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/media/photo_source.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/profile/presentation/edit_profile_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the name fields start with the current name', (tester) async {
    await pumpApp(tester, const EditProfilePage(), overrides: overrides());

    expect(find.text('Alexandrine-Marguerite'), findsOneWidget);
    expect(find.text('Montgomery-Dubois'), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the name can be updated', (tester) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overrides(SessionState.active, profile),
    );

    await tester.enterText(find.byType(TextFormField).first, 'Marie');
    await tester.tap(find.text(ProfileText.saveChanges));
    await settle(tester);

    expect(profile.renamed, ['Marie Montgomery-Dubois']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('a photo can be taken from the gallery and is uploaded', (
    tester,
  ) async {
    final profile = FakeProfile();
    final picker = FakePhotoPicker();
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overrides(SessionState.active, profile, picker),
    );

    await tester.tap(find.text(ProfileText.changePhoto));
    await settle(tester);
    expect(find.text(ProfileText.takePhoto), findsOneWidget);
    expect(find.text(ProfileText.chooseFromGallery), findsOneWidget);
    expect(find.text(ProfileText.removePhoto), findsNothing);

    await tester.tap(find.text(ProfileText.chooseFromGallery));
    await settle(tester);

    expect(picker.requested, [PhotoSource.gallery]);
    expect(profile.uploads.single.contentType, 'image/jpeg');
    expect(profile.uploads.single.bytes, tinyPng);
    expect(find.text(ProfileText.photoUpdated), findsOneWidget);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('the camera is offered too', (tester) async {
    final picker = FakePhotoPicker();
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overrides(SessionState.active, null, picker),
    );

    await tester.tap(find.text(ProfileText.changePhoto));
    await settle(tester);
    await tester.tap(find.text(ProfileText.takePhoto));
    await settle(tester);

    expect(picker.requested, [PhotoSource.camera]);
    await closeApp(tester);
  });

  testWidgets('an existing photo can be removed', (tester) async {
    final profile = FakeProfile()..avatar = tinyPng;
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overrides(SessionState.active, profile),
    );

    await tester.tap(find.text(ProfileText.changePhoto));
    await settle(tester);
    await tester.tap(find.text(ProfileText.removePhoto));
    await settle(tester);

    expect(profile.avatarRemoved, 1);
    expect(profile.avatar, isNull);
    expect(find.text(ProfileText.photoRemoved), findsOneWidget);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('iOS offers the photo choices in a native action sheet', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overrides(),
      platform: TargetPlatform.iOS,
    );

    await tester.tap(find.text(ProfileText.changePhoto));
    await settle(tester);

    expect(find.byType(CupertinoActionSheet), findsOneWidget);
    expect(find.text(Strings.cancel), findsOneWidget);
    await closeApp(tester);
  });
}
