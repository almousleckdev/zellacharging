import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/history/presentation/history_skeleton.dart';
import 'package:zella/features/profile/presentation/profile_skeleton.dart';
import 'package:zella/features/stations/presentation/station_list_skeleton.dart';
import 'package:zella/shared/widgets/adaptive_spinner.dart';
import 'package:zella/shared/widgets/async_body.dart';
import 'package:zella/shared/widgets/shimmer.dart';

Widget _host(Widget child) => MaterialApp(home: Scaffold(body: child));

void main() {
  testWidgets('station skeleton shimmers and animates', (tester) async {
    await tester.pumpWidget(_host(const StationListSkeleton(count: 2)));
    expect(find.byType(Shimmer), findsNWidgets(2));
    await tester.pump(const Duration(milliseconds: 700));
    await tester.pump(const Duration(milliseconds: 700));
  });

  testWidgets('history and profile skeletons build', (tester) async {
    await tester.pumpWidget(_host(const HistorySkeleton()));
    expect(find.byType(Shimmer), findsWidgets);
    await tester.pumpWidget(_host(const ProfileSkeleton()));
    expect(find.byType(Shimmer), findsWidgets);
  });

  testWidgets('AsyncBody shows the given skeleton while loading', (
    tester,
  ) async {
    await tester.pumpWidget(
      _host(
        AsyncBody<int>(
          value: const AsyncLoading<int>(),
          onRetry: () {},
          loading: const StationListSkeleton(),
          builder: (_) => const SizedBox(),
        ),
      ),
    );
    expect(find.byType(StationListSkeleton), findsOneWidget);
    expect(find.byType(AdaptiveSpinner), findsNothing);
  });

  testWidgets('AsyncBody keeps the spinner without a skeleton', (tester) async {
    await tester.pumpWidget(
      _host(
        AsyncBody<int>(
          value: const AsyncLoading<int>(),
          onRetry: () {},
          builder: (_) => const SizedBox(),
        ),
      ),
    );
    expect(find.byType(AdaptiveSpinner), findsOneWidget);
  });
}
