enum SessionState {
  requested,
  startAccepted,
  active,
  stopping,
  completed,
  failed,
  abandoned;

  static SessionState parse(String value) => switch (value) {
    'REQUESTED' => requested,
    'START_ACCEPTED' => startAccepted,
    'ACTIVE' => active,
    'STOPPING' => stopping,
    'COMPLETED' => completed,
    'FAILED' => failed,
    _ => abandoned,
  };

  bool get isLive =>
      this == requested ||
      this == startAccepted ||
      this == active ||
      this == stopping;

  bool get canStop => this == active;
}
