import 'app_language.dart';

abstract final class NotificationText {
  static String get title =>
      AppLanguage.pick(fr: 'Notifications', en: 'Notifications');

  static String get empty => AppLanguage.pick(
    fr: 'Aucune notification pour le moment.',
    en: 'No notifications yet.',
  );

  static String get markAllRead =>
      AppLanguage.pick(fr: 'Tout marquer comme lu', en: 'Mark all as read');

  static String unread(int count) => AppLanguage.pick(
    fr: count == 1 ? '1 non lue' : '$count non lues',
    en: '$count unread',
  );
}
