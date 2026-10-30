class PushMessage {
  const PushMessage({
    required this.title,
    required this.body,
    required this.data,
  });

  final String title;
  final String body;
  final Map<String, String> data;
}
