
#!/usr/bin/env python3

"""
UC18: Fundoo Notes integration smoke tests.

Requirements:
- Backend running at localhost:8080
- Gateway running at localhost:8081
- Existing registered and verified user
- Python 3 (standard library only)

This script does NOT create, modify, or delete notes.
It does NOT print credentials or JWT values.
"""

import getpass
import json
import os
import sys
import urllib.error
import urllib.request


BACKEND = os.getenv(
    "FUNDOO_BACKEND_URL",
    "http://localhost:8080"
)

GATEWAY = os.getenv(
    "FUNDOO_GATEWAY_URL",
    "http://localhost:8081"
)

passed = 0
failed = 0


def request(
        method,
        url,
        token=None,
        data=None,
        accept="application/json"
):
    """Send an HTTP request and return status + decoded body."""

    headers = {
        "Accept": accept
    }

    if token:
        headers["Authorization"] = "Bearer " + token

    body = None

    if data is not None:
        body = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"

    req = urllib.request.Request(
        url,
        data=body,
        headers=headers,
        method=method
    )

    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return resp.status, resp.read()

    except urllib.error.HTTPError as exc:
        return exc.code, exc.read()


def parse_json(raw):
    """Try to decode an API response as JSON."""

    try:
        return json.loads(raw.decode("utf-8"))
    except (ValueError, UnicodeDecodeError):
        return None


def find_token(payload):
    """Support common login response field names."""

    if isinstance(payload, dict):

        for key in (
            "token",
            "accessToken",
            "access_token",
            "jwt",
            "jwtToken"
        ):
            value = payload.get(key)

            if isinstance(value, str) and value:
                return value

        # Some APIs nest the token under "data".
        for value in payload.values():
            if isinstance(value, (dict, list)):
                found = find_token(value)
                if found:
                    return found

    if isinstance(payload, list):
        for value in payload:
            found = find_token(value)
            if found:
                return found

    return None


def check(name, condition, detail=""):
    """Print a pass/fail result."""

    global passed, failed

    if condition:
        passed += 1
        print("[PASS]", name)
    else:
        failed += 1
        print("[FAIL]", name)

        if detail:
            print("       ", detail)


def safe_get(name, url, token=None):
    """Perform GET without stopping the whole test suite."""

    try:
        status, body = request("GET", url, token)
        check(name, status == 200, f"HTTP {status}")
        return status, body

    except Exception as exc:
        check(
            name,
            False,
            "Request failed: " + type(exc).__name__
        )
        return None, b""


def main():

    print("\n===== UC18 FUNDOO NOTES SMOKE TESTS =====\n")

    email = os.getenv("FUNDOO_TEST_EMAIL")

    if not email:
        email = input("Registered email: ").strip()

    password = os.getenv("FUNDOO_TEST_PASSWORD")

    if not password:
        password = getpass.getpass("Login password: ")

    if not email or not password:
        print("Email and password are required.")
        return 1

    # TEST 1: Login through the backend.
    try:
        status, body = request(
            "POST",
            BACKEND + "/api/users/login",
            data={
                "email": email,
                "password": password
            }
        )

        login_response = parse_json(body)
        token = find_token(login_response)

        check(
            "UC2 - Login through backend",
            status == 200 and token is not None,
            f"HTTP {status}; token found={token is not None}"
        )

    except Exception as exc:
        print(
            "[FAIL] Login request failed:",
            type(exc).__name__
        )
        return 1

    if not token:
        print(
            "\nLogin succeeded without a recognizable token,"
            " or the login response was unsuccessful."
        )
        print(
            "Check the login response field names in"
            " find_token()."
        )
        return 1

    # TEST 2: Backend notes endpoint.
    backend_status, backend_body = safe_get(
        "UC5 - Read notes directly",
        BACKEND + "/api/notes",
        token
    )

    # TEST 3: Gateway notes endpoint.
    gateway_status, gateway_body = safe_get(
        "UC17 - Read notes through Gateway",
        GATEWAY + "/api/notes",
        token
    )

    # TEST 4: Responses should match.
    backend_data = parse_json(backend_body)
    gateway_data = parse_json(gateway_body)

    responses_match = (
        backend_status == 200
        and gateway_status == 200
        and backend_data is not None
        and backend_data == gateway_data
    )

    check(
        "UC17 - Backend and Gateway responses match",
        responses_match
    )

    # TEST 5: No JWT must be rejected.
    try:
        status, _ = request(
            "GET",
            GATEWAY + "/api/notes"
        )

        check(
            "Security - No JWT rejected",
            status in (401, 403),
            f"HTTP {status}"
        )

    except Exception as exc:
        check(
            "Security - No JWT rejected",
            False,
            type(exc).__name__
        )

    # TEST 6: Invalid JWT must be rejected.
    try:
        status, _ = request(
            "GET",
            GATEWAY + "/api/notes",
            "invalid.jwt.token"
        )

        check(
            "Security - Invalid JWT rejected",
            status in (401, 403),
            f"HTTP {status}"
        )

    except Exception as exc:
        check(
            "Security - Invalid JWT rejected",
            False,
            type(exc).__name__
        )

    # TEST 7: Labels.
    safe_get(
        "UC8 - Get user labels",
        GATEWAY + "/api/labels",
        token
    )

    # TEST 8: Upcoming reminders.
    safe_get(
        "UC9 - Get upcoming reminders",
        GATEWAY + "/api/notes/reminders",
        token
    )

    # TEST 9: Search.
    safe_get(
        "UC7 - Search notes",
        GATEWAY + "/api/notes/search?keyword=spring",
        token
    )

    # TEST 10: CSV export.
    try:
        status, csv_body = request(
            "GET",
            GATEWAY + "/api/notes/export",
            token,
            accept="text/csv"
        )

        csv_text = csv_body.decode("utf-8-sig")

        check(
            "UC16 - Export notes CSV",
            status == 200
            and csv_text.startswith(
                "id,title,description,pinned,"
            ),
            f"HTTP {status}"
        )

    except Exception as exc:
        check(
            "UC16 - Export notes CSV",
            False,
            type(exc).__name__
        )

    # TEST 11: Gateway health.
    safe_get(
        "UC17 - Gateway health",
        GATEWAY + "/actuator/health"
    )

    print("\n========== RESULTS ==========")
    print("Passed:", passed)
    print("Failed:", failed)
    print("Total: ", passed + failed)
    print("=============================\n")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
