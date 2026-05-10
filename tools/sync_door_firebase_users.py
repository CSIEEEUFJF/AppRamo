#!/usr/bin/env python3
"""
Syncs the door controller user database with Firebase.

The script downloads the persisted users.json from the door controller admin UI,
matches those profiles to Firebase users, and can publish a sanitized
doorProfiles collection for the apps. Full RFID card UIDs are never written to
Firestore by this tool.
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
import unicodedata
from dataclasses import dataclass
from difflib import SequenceMatcher
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlparse
from urllib.request import HTTPCookieProcessor, Request, build_opener


DEFAULT_DOOR_URL = "http://192.168.11.2"
DEFAULT_USER_COLLECTION = "users"
DEFAULT_DOOR_COLLECTION = "doorProfiles"
MAX_DOOR_USERS = 50


@dataclass(frozen=True)
class DoorProfile:
    index: int
    name: str
    role: str
    chapter: str
    photo_id: str
    cards: tuple[str, ...]
    is_admin: bool


@dataclass(frozen=True)
class FirebaseUser:
    uid: str
    name: str
    email: str
    profile_picture_url: str
    chapter_roles: dict[str, str]
    cards: tuple[str, ...]


@dataclass(frozen=True)
class MatchResult:
    firebase_user: FirebaseUser
    door_profile: DoorProfile
    method: str
    score: float


def normalize_text(value: Any) -> str:
    text = unicodedata.normalize("NFKD", str(value or ""))
    text = "".join(ch for ch in text if not unicodedata.combining(ch))
    return re.sub(r"[^a-z0-9]+", " ", text.lower()).strip()


def normalize_card(value: Any) -> str:
    return re.sub(r"[^0-9A-Z]", "", str(value or "").upper())


def pick_string(record: dict[str, Any], *keys: str) -> str:
    for key in keys:
        value = record.get(key)
        if isinstance(value, str) and value.strip():
            return value.strip()
    return ""


def collect_cards(record: dict[str, Any]) -> tuple[str, ...]:
    cards: list[str] = []
    for key in ("cards", "uids", "rfids", "cartoes", "cartões", "tags"):
        value = record.get(key)
        if isinstance(value, list):
            values = value
        elif isinstance(value, str):
            values = value.split(",")
        else:
            values = []

        for item in values:
            card = normalize_card(item)
            if card and card not in cards:
                cards.append(card)

    for key in ("uid", "rfid", "card", "cartao", "cartão", "tag"):
        card = normalize_card(record.get(key))
        if card and card not in cards:
            cards.append(card)

    return tuple(cards)


def parse_door_profiles(raw: Any) -> list[DoorProfile]:
    if isinstance(raw, dict):
        for key in ("users", "profiles", "members", "data"):
            if isinstance(raw.get(key), list):
                raw = raw[key]
                break

    if not isinstance(raw, list):
        raise ValueError("Door users JSON must be a list or contain a list under users/profiles/data.")

    profiles: list[DoorProfile] = []
    for index, item in enumerate(raw):
        if not isinstance(item, dict):
            continue

        name = pick_string(item, "name", "nome", "displayName", "fullName")
        if not name:
            continue

        profiles.append(
            DoorProfile(
                index=index,
                name=name,
                role=pick_string(item, "role", "cargo", "position", "title"),
                chapter=pick_string(item, "chapter", "capitulo", "capítulo", "branch", "ramo"),
                photo_id=pick_string(item, "photo_id", "photoId", "photo", "photoPath"),
                cards=collect_cards(item),
                is_admin=bool(item.get("is_admin", False)),
            )
        )

    if len(profiles) > MAX_DOOR_USERS:
        raise ValueError(f"Door returned {len(profiles)} profiles, but firmware supports {MAX_DOOR_USERS}.")
    return profiles


def parse_chapter_roles(record: dict[str, Any]) -> dict[str, str]:
    raw = record.get("chapterRoles")
    if not isinstance(raw, dict):
        return {}

    roles: dict[str, str] = {}
    for chapter, role in raw.items():
        chapter_text = str(chapter or "").strip()
        role_text = str(role or "").strip()
        if chapter_text and role_text:
            roles[chapter_text] = role_text
    return roles


def firebase_user_from_doc(doc: Any) -> FirebaseUser | None:
    record = doc.to_dict() or {}
    name = pick_string(record, "name", "nome", "displayName", "fullName")
    if not name:
        return None

    return FirebaseUser(
        uid=doc.id,
        name=name,
        email=pick_string(record, "email"),
        profile_picture_url=pick_string(record, "profilePictureUrl", "profile_picture_url"),
        chapter_roles=parse_chapter_roles(record),
        cards=collect_cards(record),
    )


def door_url_host(door_url: str) -> str:
    parsed = urlparse(door_url)
    return parsed.netloc or parsed.path


def request_bytes(opener: Any, url: str, method: str = "GET", data: bytes | None = None) -> bytes:
    request = Request(
        url=url,
        data=data,
        method=method,
        headers={
            "User-Agent": "AppRamoDoorFirebaseSync/1.0",
            "Content-Type": "application/x-www-form-urlencoded",
        },
    )
    with opener.open(request, timeout=10) as response:
        return response.read()


def download_door_users(door_url: str, admin_pin: str) -> list[DoorProfile]:
    root = door_url.rstrip("/")
    opener = build_opener(HTTPCookieProcessor())
    login_body = urlencode({"pin": admin_pin}).encode("utf-8")
    request_bytes(opener, f"{root}/login", method="POST", data=login_body)
    payload = request_bytes(opener, f"{root}/storage_users_download")

    try:
        decoded = payload.decode("utf-8-sig")
        raw = json.loads(decoded)
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        sample = payload[:160].decode("utf-8", errors="replace")
        raise RuntimeError(
            "Could not download a valid users.json from the door controller. "
            f"Response starts with: {sample!r}"
        ) from exc

    return parse_door_profiles(raw)


def photo_id_points_to_user(photo_id: str, user: FirebaseUser) -> bool:
    normalized_photo = normalize_text(photo_id)
    if not normalized_photo:
        return False

    uid_text = normalize_text(user.uid)
    if uid_text and uid_text in normalized_photo:
        return True

    picture_text = normalize_text(user.profile_picture_url)
    return bool(picture_text and normalized_photo and normalized_photo in picture_text)


def chapter_overlap_score(door_profile: DoorProfile, user: FirebaseUser) -> float:
    if not door_profile.chapter or not user.chapter_roles:
        return 0.0

    door_chapter = normalize_text(door_profile.chapter)
    for chapter in user.chapter_roles:
        firebase_chapter = normalize_text(chapter)
        if door_chapter and firebase_chapter and (
            door_chapter == firebase_chapter
            or door_chapter in firebase_chapter
            or firebase_chapter in door_chapter
        ):
            return 0.12
    return 0.0


def score_match(door_profile: DoorProfile, user: FirebaseUser) -> tuple[str, float]:
    if door_profile.cards and user.cards and set(door_profile.cards).intersection(user.cards):
        return "card", 1.0

    if photo_id_points_to_user(door_profile.photo_id, user):
        return "photo_id", 0.98

    name_score = SequenceMatcher(None, normalize_text(door_profile.name), normalize_text(user.name)).ratio()
    score = min(1.0, name_score + chapter_overlap_score(door_profile, user))
    return "name_chapter", score


def match_profiles(
    door_profiles: list[DoorProfile],
    firebase_users: list[FirebaseUser],
    threshold: float,
    manual_bindings: dict[str, int] | None = None,
) -> tuple[list[MatchResult], list[DoorProfile], list[FirebaseUser]]:
    matches: list[MatchResult] = []
    used_door_indexes: set[int] = set()
    used_firebase_uids: set[str] = set()

    door_by_index = {profile.index: profile for profile in door_profiles}
    user_by_uid = {user.uid: user for user in firebase_users}
    for firebase_uid, door_index in (manual_bindings or {}).items():
        door_profile = door_by_index.get(door_index)
        user = user_by_uid.get(firebase_uid)
        if door_profile is None or user is None:
            continue
        matches.append(MatchResult(user, door_profile, "manual", 1.0))
        used_door_indexes.add(door_profile.index)
        used_firebase_uids.add(user.uid)

    candidates: list[tuple[float, str, DoorProfile, FirebaseUser]] = []
    for door_profile in door_profiles:
        if door_profile.index in used_door_indexes:
            continue
        for user in firebase_users:
            if user.uid in used_firebase_uids:
                continue
            method, score = score_match(door_profile, user)
            if score >= threshold:
                candidates.append((score, method, door_profile, user))

    candidates.sort(key=lambda item: item[0], reverse=True)
    for score, method, door_profile, user in candidates:
        if door_profile.index in used_door_indexes or user.uid in used_firebase_uids:
            continue
        matches.append(MatchResult(user, door_profile, method, score))
        used_door_indexes.add(door_profile.index)
        used_firebase_uids.add(user.uid)

    unmatched_door = [profile for profile in door_profiles if profile.index not in used_door_indexes]
    unmatched_users = [user for user in firebase_users if user.uid not in used_firebase_uids]
    return matches, unmatched_door, unmatched_users


def load_manual_bindings(path: Path | None) -> dict[str, int]:
    if path is None:
        return {}

    bindings: dict[str, int] = {}
    with path.open("r", encoding="utf-8-sig", newline="") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            firebase_uid = str(row.get("firebase_uid", "")).strip()
            door_index_text = str(row.get("door_profile_index", "")).strip()
            if not firebase_uid or not door_index_text:
                continue
            try:
                bindings[firebase_uid] = int(door_index_text)
            except ValueError:
                continue
    return bindings


def load_firebase_users(service_account: Path, user_collection: str) -> tuple[Any, list[FirebaseUser]]:
    try:
        import firebase_admin
        from firebase_admin import credentials, firestore
    except ImportError as exc:
        raise RuntimeError(
            "The firebase-admin package is required. Install it locally with "
            "`python -m pip install firebase-admin`."
        ) from exc

    cred = credentials.Certificate(str(service_account))
    app = firebase_admin.initialize_app(cred)
    db = firestore.client(app=app)

    users: list[FirebaseUser] = []
    for doc in db.collection(user_collection).stream():
        user = firebase_user_from_doc(doc)
        if user is not None:
            users.append(user)
    return db, users


def report_payload(
    matches: list[MatchResult],
    unmatched_door: list[DoorProfile],
    unmatched_users: list[FirebaseUser],
    door_url: str,
) -> dict[str, Any]:
    return {
        "source": {"door": door_url_host(door_url)},
        "matched_count": len(matches),
        "unmatched_door_count": len(unmatched_door),
        "unmatched_firebase_count": len(unmatched_users),
        "matches": [
            {
                "firebase_uid": match.firebase_user.uid,
                "firebase_name": match.firebase_user.name,
                "door_profile_index": match.door_profile.index,
                "door_name": match.door_profile.name,
                "door_chapter": match.door_profile.chapter,
                "door_role": match.door_profile.role,
                "card_count": len(match.door_profile.cards),
                "match_method": match.method,
                "match_score": round(match.score, 4),
            }
            for match in matches
        ],
        "unmatched_door_profiles": [
            {
                "door_profile_index": profile.index,
                "name": profile.name,
                "chapter": profile.chapter,
                "role": profile.role,
                "card_count": len(profile.cards),
            }
            for profile in unmatched_door
        ],
        "unmatched_firebase_users": [
            {
                "firebase_uid": user.uid,
                "name": user.name,
                "chapters": sorted(user.chapter_roles.keys()),
            }
            for user in unmatched_users
        ],
    }


def write_json(path: Path, payload: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def publish_matches(
    db: Any,
    matches: list[MatchResult],
    door_collection: str,
    user_collection: str,
    door_url: str,
) -> None:
    from firebase_admin import firestore

    batch = db.batch()
    source_host = door_url_host(door_url)

    for match in matches:
        user = match.firebase_user
        door_profile = match.door_profile
        visible_payload = {
            "firebaseUid": user.uid,
            "name": user.name,
            "chapter": door_profile.chapter,
            "role": door_profile.role,
            "doorProfileIndex": door_profile.index,
            "doorProfileName": door_profile.name,
            "doorChapter": door_profile.chapter,
            "doorRole": door_profile.role,
            "cardCount": len(door_profile.cards),
            "hasDoorCards": len(door_profile.cards) > 0,
            "doorSource": source_host,
            "matchMethod": match.method,
            "matchScore": round(match.score, 4),
            "updatedAt": firestore.SERVER_TIMESTAMP,
        }
        private_payload = {
            "doorProfileIndex": door_profile.index,
            "doorProfileName": door_profile.name,
            "doorChapter": door_profile.chapter,
            "doorRole": door_profile.role,
            "doorCardCount": len(door_profile.cards),
            "doorLinkedAt": firestore.SERVER_TIMESTAMP,
            "doorSource": source_host,
        }

        batch.set(db.collection(door_collection).document(user.uid), visible_payload, merge=True)
        batch.set(db.collection(user_collection).document(user.uid), private_payload, merge=True)

    batch.commit()


def require_pin(cli_pin: str | None) -> str:
    admin_pin = cli_pin or os.getenv("DOOR_ADMIN_PIN", "")
    if not admin_pin:
        raise ValueError("Provide the admin PIN with --admin-pin or DOOR_ADMIN_PIN.")
    return admin_pin


def main() -> int:
    parser = argparse.ArgumentParser(description="Links door controller profiles with Firebase users.")
    parser.add_argument("--door-url", default=DEFAULT_DOOR_URL, help="Door admin URL. Default: %(default)s")
    parser.add_argument("--admin-pin", default=None, help="Door admin PIN. Prefer DOOR_ADMIN_PIN.")
    parser.add_argument("--service-account", type=Path, required=True, help="Firebase service account JSON.")
    parser.add_argument("--user-collection", default=DEFAULT_USER_COLLECTION, help="Private users collection.")
    parser.add_argument("--door-collection", default=DEFAULT_DOOR_COLLECTION, help="Sanitized linked profiles collection.")
    parser.add_argument("--match-threshold", type=float, default=0.86, help="Minimum name/chapter match score.")
    parser.add_argument(
        "--bindings",
        type=Path,
        default=None,
        help="Optional CSV with firebase_uid,door_profile_index for exact links.",
    )
    parser.add_argument("--download-output", type=Path, default=Path("build/door/users.from-door.json"))
    parser.add_argument("--report-output", type=Path, default=Path("build/door/door-firebase-link-report.json"))
    parser.add_argument("--apply-firebase", action="store_true", help="Write doorProfiles and private user link fields.")
    args = parser.parse_args()

    try:
        admin_pin = require_pin(args.admin_pin)
        door_profiles = download_door_users(args.door_url, admin_pin)
        write_json(
            args.download_output,
            [
                {
                    "index": profile.index,
                    "name": profile.name,
                    "role": profile.role,
                    "chapter": profile.chapter,
                    "photo_id": profile.photo_id,
                    "card_count": len(profile.cards),
                    "is_admin": profile.is_admin,
                }
                for profile in door_profiles
            ],
        )

        db, firebase_users = load_firebase_users(args.service_account, args.user_collection)
        manual_bindings = load_manual_bindings(args.bindings)
        matches, unmatched_door, unmatched_users = match_profiles(
            door_profiles,
            firebase_users,
            args.match_threshold,
            manual_bindings,
        )
        payload = report_payload(matches, unmatched_door, unmatched_users, args.door_url)
        write_json(args.report_output, payload)

        if args.apply_firebase:
            publish_matches(db, matches, args.door_collection, args.user_collection, args.door_url)

        print(f"Door profiles: {len(door_profiles)}")
        print(f"Firebase users: {len(firebase_users)}")
        print(f"Matches: {len(matches)}")
        print(f"Unmatched door profiles: {len(unmatched_door)}")
        print(f"Unmatched Firebase users: {len(unmatched_users)}")
        print(f"Report: {args.report_output}")
        if args.apply_firebase:
            print(f"Published sanitized links to `{args.door_collection}`.")
        else:
            print("Dry run only. Re-run with --apply-firebase to publish links.")
        return 0
    except (HTTPError, URLError, OSError, RuntimeError, ValueError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
