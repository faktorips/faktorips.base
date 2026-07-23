#!/usr/bin/env bash
# Creates Jira issues for upcoming Eclipse SimRel releases that do not yet have
# a corresponding M2 issue in the FIPS project.
#
# Run this script periodically (e.g. monthly) to pick up newly announced
# Eclipse releases. For each release a set of three tasks (M2, RC1, GA) with
# their respective sub-tasks is created.
#
# Requirements: jira CLI (https://github.com/ankitpokhrel/jira-cli), curl, grep, sed, awk
#               Note: sed is used instead of grep -P for portability (POSIX-compatible)
set -euo pipefail

command -v jira curl grep awk sed > /dev/null || { echo "Required tools missing: jira curl grep awk sed" >&2; exit 1; }

JIRA_PROJECT="FIPS"
BOARD_ID=434
SIMREL_API="https://api.github.com/repos/eclipse-simrel/.github/contents/wiki/SimRel"
SIMREL_RAW="https://raw.githubusercontent.com/eclipse-simrel/.github/main/wiki/SimRel"
[[ -f ~/.config/.jira/.config.yml ]] || { echo "Jira config not found: ~/.config/.jira/.config.yml" >&2; exit 1; }
JIRA_SERVER=$(grep '^server:' ~/.config/.jira/.config.yml | awk '{print $2}')
# JIRA_API_TOKEN must be set as environment variable

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Extract a date value from the flat _dates.json by key (M2, RC1, GA, …).
# The file is a single-line JSON object, so we match key + value in one regex.
extract_date() {
    local json="$1" key="$2"
    printf '%s\n' "$json" | sed -En "s/.*\"${key}\"[[:space:]]*:[[:space:]]*\"([0-9]{4}-[0-9]{2}-[0-9]{2})\".*/\1/p"
}

# Format an ISO date (YYYY-MM-DD) as German "D.M.YYYY" without leading zeros
format_date_de() {
    local iso="$1"
    local year month day
    year="${iso:0:4}"
    month="${iso:5:2}"
    day="${iso:8:2}"
    # strip leading zeros
    month="${month#0}"
    day="${day#0}"
    echo "${day}.${month}.${year}"
}

# Calculate the Faktor-IPS release version that follows a given Eclipse release.
# Eclipse YYYY-03 / YYYY-06  → YY.7  (same year July)
# Eclipse YYYY-09 / YYYY-12  → (YY+1).1  (next year January)
fips_version_for_eclipse() {
    local eclipse_release="$1"  # e.g. 2026-12
    local year="${eclipse_release:0:4}"
    local month="${eclipse_release:5:2}"
    local short_year="${year:2:2}"
    if [[ "$month" == "09" || "$month" == "12" ]]; then
        echo "$((short_year + 1)).1"
    else
        echo "${short_year}.7"
    fi
}

# Check whether a Jira issue with the given summary already exists (returns key or empty).
# Uses summary ~ (contains) because JQL "=" does not support text with special chars.
# The release tag (e.g. "Eclipse 2026-12 M2") is unique enough to avoid false positives.
issue_key_by_summary() {
    local summary="$1"
    local result
    result=$(jira issue list --project "$JIRA_PROJECT" \
        --jql "project = ${JIRA_PROJECT} AND summary ~ \"${summary}\"" \
        --plain --no-headers --columns KEY 2>/dev/null || true)
    echo "$result" | awk 'NR==1{print $1}'
}

# Find the epic key for a given Faktor-IPS release version (e.g. 27.1)
# Returns the key or empty string if not found.
find_epic() {
    local fips_ver="$1"
    local epic_summary="Wartung Faktor-IPS ${fips_ver}"
    local result
    result=$(jira issue list --project "$JIRA_PROJECT" \
        --jql "project = ${JIRA_PROJECT} AND issuetype = Epic AND summary ~ \"${epic_summary}\"" \
        --plain --no-headers --columns KEY 2>/dev/null || true)
    echo "$result" | awk 'NR==1{print $1}'
}

# Create the epic for a given Faktor-IPS release version if it does not exist.
# Prints the epic key.
ensure_epic() {
    local fips_ver="$1"
    local epic_name="Wartung Faktor-IPS ${fips_ver}"

    local existing
    existing=$(find_epic "$fips_ver")
    if [[ -n "$existing" ]]; then
        echo "$existing"
        return
    fi

    echo "  → Creating epic: ${epic_name}" >&2
    local key
    key=$(jira epic create \
        --project "$JIRA_PROJECT" \
        -n "$epic_name" \
        -s "$epic_name" \
        -b "$epic_name" \
        -C General \
        --custom customfield_13649=IPS \
        --custom customfield_10119=ghx-label-14 \
        --no-input \
        --raw 2>/dev/null | sed -En 's/.*"key"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/p' | head -1)
    echo "$key"
}

# Ensure the given fix-version exists in the FIPS project; create it if missing.
ensure_fix_version() {
    local fips_ver="$1"
    local existing
    existing=$(jira release list --project "$JIRA_PROJECT" --plain --no-headers 2>/dev/null \
        | awk '{print $1}' | grep -xF "$fips_ver" || true)
    if [[ -z "$existing" ]]; then
        echo "  → Creating fix-version ${fips_ver} in ${JIRA_PROJECT}" >&2
        if [[ -z "${JIRA_API_TOKEN:-}" ]]; then
            echo "  ⚠ JIRA_API_TOKEN not set – fix-version '${fips_ver}' must be created manually." >&2
            return
        fi
        curl -s -X POST \
            -H "Authorization: Bearer ${JIRA_API_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "{\"name\":\"${fips_ver}\",\"project\":\"${JIRA_PROJECT}\"}" \
            "${JIRA_SERVER}/rest/api/2/version" > /dev/null
    fi
}

# Create a sub-task under a parent issue. Prints nothing (errors go to stderr).
create_subtask() {
    local parent="$1" summary="$2" body="${3:-}"
    local args=( issue create
        --project "$JIRA_PROJECT"
        -t Sub-task
        -P "$parent"
        -s "$summary"
        --no-input
    )
    if [[ -n "$body" ]]; then
        args+=( -b "$body" )
    fi
    jira "${args[@]}" > /dev/null
}

# Sets the Sponsor field (option-with-child) via REST API because the jira CLI
# does not support the option-with-child datatype for issue create/edit.
# Requires JIRA_API_TOKEN to be set as an environment variable.
set_sponsor() {
    local issue_key="$1"
    if [[ -z "${JIRA_API_TOKEN:-}" ]]; then
        echo "  ⚠ JIRA_API_TOKEN not set – sponsor field must be set manually on ${issue_key}." >&2
        return
    fi
    curl -s -X PUT \
        -H "Authorization: Bearer ${JIRA_API_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"fields":{"customfield_13649":{"id":"13486","child":{"id":"16991"}}}}' \
        "${JIRA_SERVER}/rest/api/2/issue/${issue_key}" > /dev/null
}

# ---------------------------------------------------------------------------
# Sprint helpers
# ---------------------------------------------------------------------------

# Returns the sprint ID whose date window (start..end) contains target_date.
# Only considers active/future sprints that have an actual date range.
find_sprint_for_date() {
    local target_date="$1"   # YYYY-MM-DD
    jira sprint list --table --plain --no-headers --state active,future 2>/dev/null \
        | grep -E '[0-9]{4}-[0-9]{2}-[0-9]{2}' \
        | awk -F'\t' -v target="$target_date" '{
            id=$1; start=""; end=""
            for (i=3; i<=NF; i++) {
                if ($i ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}/) {
                    d=substr($i,1,10)
                    if (start=="") start=d
                    else if (end=="") { end=d; break }
                }
            }
            if (start != "" && end != "" && target >= start && target <= end) {
                print id; exit
            }
        }' || true
}

# Returns the sprint ID of the "Backlog <fips_ver>" sprint, creating it if needed.
ensure_backlog_sprint() {
    local fips_ver="$1"
    local sprint_name="Backlog ${fips_ver}"
    local id
    id=$(jira sprint list --table --plain --no-headers --state active,future,closed 2>/dev/null \
        | grep -F "$sprint_name" | awk -F'\t' '{print $1}' | head -1 || true)
    if [[ -n "$id" ]]; then
        echo "$id"
        return
    fi
    echo "  → Creating backlog sprint: ${sprint_name}" >&2
    if [[ -z "${JIRA_API_TOKEN:-}" ]]; then
        echo "  ⚠ JIRA_API_TOKEN not set – sprint '${sprint_name}' must be created manually." >&2
        echo ""
        return
    fi
    id=$(curl -s -X POST \
        -H "Authorization: Bearer ${JIRA_API_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"${sprint_name}\",\"originBoardId\":${BOARD_ID}}" \
        "${JIRA_SERVER}/rest/agile/1.0/sprint" \
        | sed -En 's/.*"id"[[:space:]]*:[[:space:]]*([0-9]+).*/\1/p' | head -1)
    echo "$id"
}

# Assigns issue_key to the sprint matching event_date, or to the backlog sprint as fallback.
assign_to_sprint() {
    local issue_key="$1" event_date="$2" fips_ver="$3"
    local sprint_id
    sprint_id=$(find_sprint_for_date "$event_date")
    if [[ -z "$sprint_id" ]]; then
        sprint_id=$(ensure_backlog_sprint "$fips_ver")
    fi
    if [[ -n "$sprint_id" ]]; then
        jira sprint add "$sprint_id" "$issue_key" > /dev/null
        echo "    → Sprint ${sprint_id}"
    fi
}

# Assigns issue_key to a sprint only if it has no sprint yet.
assign_if_missing() {
    local issue_key="$1" event_date="$2" fips_ver="$3"
    local has_sprint
    has_sprint=$(jira issue list --project "$JIRA_PROJECT" \
        --jql "key = ${issue_key} AND sprint is not EMPTY" \
        --plain --no-headers --columns KEY 2>/dev/null || true)
    if [[ -z "$has_sprint" ]]; then
        echo "  ${issue_key}: no sprint – assigning…"
        assign_to_sprint "$issue_key" "$event_date" "$fips_ver"
    else
        echo "  ${issue_key}: sprint already set – skipping."
    fi
}

# ---------------------------------------------------------------------------
# Create issues for one Eclipse release
# ---------------------------------------------------------------------------

create_issues_for_release() {
    local release="$1"       # e.g. 2026-12
    local dates_json="$2"    # content of _dates.json

    local m2_iso rc1_iso ga_iso
    m2_iso=$(extract_date "$dates_json" "M2")
    rc1_iso=$(extract_date "$dates_json" "RC1")
    ga_iso=$(extract_date  "$dates_json" "GA")

    if [[ -z "$m2_iso" || -z "$rc1_iso" || -z "$ga_iso" ]]; then
        echo "  ⚠ Could not extract all dates for ${release} – skipping." >&2
        return
    fi

    local m2_de rc1_de ga_de
    m2_de=$(format_date_de "$m2_iso")
    rc1_de=$(format_date_de "$rc1_iso")
    ga_de=$(format_date_de "$ga_iso")

    local fips_ver
    fips_ver=$(fips_version_for_eclipse "$release")

    # The jira CLI escapes Jira-Wiki-Markup brackets, so we pass bare URLs.
    # Jira renders bare https:// URLs as clickable links automatically.
    local simrel_url="https://github.com/eclipse-simrel/.github/blob/main/wiki/SimRel/${release}.md"

    echo "  Fix-version: ${fips_ver}"
    ensure_fix_version "$fips_ver"

    local epic_key
    epic_key=$(ensure_epic "$fips_ver")
    echo "  Epic: ${epic_key}"

    # Common flags for all three parent tasks
    local common_flags=(
        --project "$JIRA_PROJECT"
        -t Task
        -l Eclipse
        -C Buildprocess -C General -C Targets
        --fix-version "$fips_ver"
        --custom customfield_10116="$epic_key"
        --no-input
    )

    # ------------------------------------------------------------------
    # M2
    # ------------------------------------------------------------------
    local m2_summary="Eclipse ${release} M2 (ab ${m2_de})"
    local m2_body="Nach Erscheinen von Eclipse ${release} M2 (geplant für ${m2_de} ${simrel_url}) sollten wir einen Mirror und eine Target-Platform dazu anlegen und testen, ob Faktor-IPS auch damit funktioniert."

    echo "  Creating M2 task: ${m2_summary}"
    local m2_key
    m2_key=$(jira issue create "${common_flags[@]}" \
        -s "$m2_summary" -b "$m2_body" \
        --raw 2>/dev/null | sed -En 's/.*"key"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/p' | head -1)
    [[ -z "$m2_key" ]] && { echo "  ✗ Failed to create M2 issue for ${release}" >&2; return 1; }
    echo "  → ${m2_key}"
    set_sponsor "$m2_key"
    assign_to_sprint "$m2_key" "$m2_iso" "$fips_ver"

    create_subtask "$m2_key" "mirror it & manual check"
    create_subtask "$m2_key" "Add target ${release} to targets, launch configs & nightly (bom)" \
        '|Linux|() | |MacOS|()| |Windows|()|'
    create_subtask "$m2_key" "Kompatibilität in Release Notes anpassen"
    create_subtask "$m2_key" "Test IPS-Workspace with it"

    # ------------------------------------------------------------------
    # RC1
    # ------------------------------------------------------------------
    local rc1_summary="Eclipse ${release} RC1 (ab ${rc1_de})"
    local rc1_body="Nach Erscheinen von Eclipse ${release} RC1 (geplant für ${rc1_de} ${simrel_url}) sollten wir den mit dem Milestone 2 angelegten Mirror erneuern und testen, ob Faktor-IPS auch damit funktioniert."

    echo "  Creating RC1 task: ${rc1_summary}"
    local rc1_key
    rc1_key=$(jira issue create "${common_flags[@]}" \
        -s "$rc1_summary" -b "$rc1_body" \
        --raw 2>/dev/null | sed -En 's/.*"key"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/p' | head -1)
    [[ -z "$rc1_key" ]] && { echo "  ✗ Failed to create RC1 issue for ${release}" >&2; return 1; }
    echo "  → ${rc1_key}"
    set_sponsor "$rc1_key"
    assign_to_sprint "$rc1_key" "$rc1_iso" "$fips_ver"

    create_subtask "$rc1_key" "mirror it & manual check🐧🪟🍏" \
        "|| Betriebssystem || Check ||
| 🐧 Linux | () |
| 🪟 Windows | () |
| 🍏 Apple | () |"
    create_subtask "$rc1_key" "update target & launch configs"

    # ------------------------------------------------------------------
    # GA
    # ------------------------------------------------------------------
    local ga_summary="Eclipse ${release} (ab ${ga_de})"
    local ga_body="Nach Erscheinen von Eclipse ${release} (geplant für ${ga_de} ${simrel_url}) sollten wir den mit dem RC1 angelegten Mirror erneuern und testen, ob Faktor-IPS auch damit funktioniert."

    echo "  Creating GA task: ${ga_summary}"
    local ga_key
    ga_key=$(jira issue create "${common_flags[@]}" \
        -s "$ga_summary" -b "$ga_body" \
        --raw 2>/dev/null | sed -En 's/.*"key"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/p' | head -1)
    [[ -z "$ga_key" ]] && { echo "  ✗ Failed to create GA issue for ${release}" >&2; return 1; }
    echo "  → ${ga_key}"
    set_sponsor "$ga_key"
    assign_to_sprint "$ga_key" "$ga_iso" "$fips_ver"

    create_subtask "$ga_key" "mirror it & manual check🐧🪟🍏" \
        "|| Betriebssystem || Check ||
| 🐧 Linux | () |
| 🪟 Windows | () |
| 🍏 Apple | () |"
    create_subtask "$ga_key" "Targets und Launch Configs prüfen"
    create_subtask "$ga_key" "ggf. Kompatibilität in Release Notes anpassen"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

echo "Fetching SimRel release list from GitHub…"
releases_json=$(curl -sf "$SIMREL_API")

# Extract all YYYY-MM release identifiers that have a _dates.json file
releases=$(printf '%s\n' "$releases_json" | sed -En 's/.*"name"[[:space:]]*:[[:space:]]*"([0-9]{4}-[0-9]{2})_dates\.json".*/\1/p' | sort)

if [[ -z "$releases" ]]; then
    echo "No releases found. Check network connectivity or GitHub API rate limits." >&2
    exit 1
fi

echo "Found releases: $(echo "$releases" | tr '\n' ' ')"
echo ""

today=$(date +%Y-%m-%d)

for release in $releases; do
    echo "Checking ${release}…"

    dates_json=$(curl -sf "${SIMREL_RAW}/${release}_dates.json" || true)
    if [[ -z "$dates_json" ]]; then
        echo "  ⚠ Could not fetch dates for ${release} – skipping."
        continue
    fi

    # Only process releases whose GA date is today or in the future.
    ga_iso_check=$(extract_date "$dates_json" "GA")
    if [[ -z "$ga_iso_check" || "$ga_iso_check" < "$today" ]]; then
        echo "  GA date ${ga_iso_check:-unknown} is in the past – skipping."
        continue
    fi

    m2_iso=$(extract_date "$dates_json" "M2")
    rc1_iso=$(extract_date "$dates_json" "RC1")
    ga_iso=$(extract_date  "$dates_json" "GA")
    fips_ver=$(fips_version_for_eclipse "$release")

    existing=$(issue_key_by_summary "Eclipse ${release} M2")

    if [[ -n "$existing" ]]; then
        echo "  Issues exist – checking sprint assignments…"
        assign_if_missing "$existing" "$m2_iso" "$fips_ver"
        rc1_existing=$(issue_key_by_summary "Eclipse ${release} RC1")
        [[ -n "$rc1_existing" ]] && assign_if_missing "$rc1_existing" "$rc1_iso" "$fips_ver"
        ga_existing=$(jira issue list --project "$JIRA_PROJECT" \
            --jql "project = ${JIRA_PROJECT} AND summary ~ \"Eclipse ${release}\" AND summary !~ \"M1\" AND summary !~ \"M2\" AND summary !~ \"M3\" AND summary !~ \"RC1\" AND summary !~ \"RC2\" AND summary !~ \"RC3\" AND summary !~ \"RC4\"" \
            --plain --no-headers --columns KEY 2>/dev/null | awk 'NR==1{print $1}' || true)
        [[ -n "$ga_existing" ]] && assign_if_missing "$ga_existing" "$ga_iso" "$fips_ver"
        echo ""
        continue
    fi

    echo "  No M2 issue found – creating issues for ${release}…"
    create_issues_for_release "$release" "$dates_json"
    echo ""
done

echo "Done."
