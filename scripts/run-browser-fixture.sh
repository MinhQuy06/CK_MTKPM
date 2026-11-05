#!/usr/bin/env bash
# Test-only H2/JWT fixtures, never the development database.
set -euo pipefail
cd "$(dirname "$0")/.."
fixture_cp_file=$(mktemp "${TMPDIR:-/tmp}/ck-browser-classpath.XXXXXX")
trap 'rm -f "$fixture_cp_file"' EXIT
bash scripts/maven.sh -q -f backend/pom.xml test-compile dependency:build-classpath -Dmdep.outputFile="$fixture_cp_file"
fixture_cp="backend/target/classes:backend/target/test-classes:$(cat "$fixture_cp_file")"
fixture_options=(-Dspring.devtools.restart.enabled=false)
if [ -n "${CK_TEST_INSURANCE_RATE:-}" ]; then fixture_options+=("-Dapp.pricing.insurance-discount-rate=$CK_TEST_INSURANCE_RATE"); fi
java "${fixture_options[@]}" -cp "$fixture_cp" com.datlichkhambenh.backend.ConsultationQuoteBrowserServer "${CK_BROWSER_PORT:-18081}"
