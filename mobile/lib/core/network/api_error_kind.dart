enum ApiErrorKind {
  recoverable,
  uncertain,
  terminal;

  static const _uncertain = {
    'PAYMENT_VERIFYING',
    'TOPUP_PENDING',
    'WITHDRAWAL_VERIFYING',
  };

  static const _recoverable = {
    'NETWORK',
    'TOO_MANY_REQUESTS',
    'CATALOG_UNAVAILABLE',
    'CHARGING_UNAVAILABLE',
    'SERVICE_UNAVAILABLE',
    'CHARGER_TIMEOUT',
    'CHARGER_UNREACHABLE',
    'PROVIDER_UNAVAILABLE',
    'STOP_REFUSED',
  };

  static ApiErrorKind of(String code, int? status) {
    if (_uncertain.contains(code)) return uncertain;
    if (_recoverable.contains(code) || (status != null && status >= 500)) {
      return recoverable;
    }
    return terminal;
  }
}
