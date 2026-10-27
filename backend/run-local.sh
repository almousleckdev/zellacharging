#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
while IFS= read -r line; do
  export "$line"
done < <(grep -E '^(PVIT_|ZELLA_OCPP_|NDZELA_OCPP_)' .env)
provider="${1:-sandbox}"
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run -Dspring-boot.run.arguments="--zella.payment.provider=${provider}"
