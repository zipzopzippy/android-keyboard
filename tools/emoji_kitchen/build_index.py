#!/usr/bin/env python3
"""Builds the compact Emoji Kitchen index bundled in the app assets.

Input is metadata.json from https://github.com/xsalazar/emoji-kitchen-backend (app/metadata.json),
which lists every emoji pair Gboard's Emoji Kitchen supports and the date folder its sticker lives
in on gstatic.com. Only the latest version of each pair is kept.

Output format (UTF-8 text, one record per line):
    v1
    <space separated date folders>
    <space separated emoji codepoint keys, e.g. "1f600" or "263a-fe0f", in Gboard order>
    <left index>,<right index>,<date index>   (one line per pair, sorted)

The sticker for a pair is at
    https://www.gstatic.com/android/keyboard/emojikitchen/<date>/<L>/<L>_<R>.png
where <L> and <R> are the codepoint keys with "u" before every codepoint ("u263a-ufe0f").

Usage: build_index.py metadata.json out.txt
"""

import json
import sys


def url_segment(codepoint_key):
    return "u" + codepoint_key.replace("-", "-u")


def main(metadata_path, out_path):
    with open(metadata_path, encoding="utf-8") as f:
        data = json.load(f)["data"]

    keys = sorted(data.keys(), key=lambda k: (data[k]["gBoardOrder"], k))
    index = {key: i for i, key in enumerate(keys)}

    pairs = {}
    for entry in data.values():
        for combos in entry["combinations"].values():
            latest = [c for c in combos if c.get("isLatest")] or combos
            combo = max(latest, key=lambda c: c["date"])
            left, right = combo["leftEmojiCodepoint"], combo["rightEmojiCodepoint"]
            if left not in index or right not in index:
                continue
            expected_url = "https://www.gstatic.com/android/keyboard/emojikitchen/{}/{}/{}_{}.png".format(
                combo["date"], url_segment(left), url_segment(left), url_segment(right))
            if combo["gStaticUrl"] != expected_url:
                raise ValueError("Unexpected sticker URL: " + combo["gStaticUrl"])
            pair_key = tuple(sorted((index[left], index[right])))
            pairs[pair_key] = (index[left], index[right], combo["date"])

    dates = sorted({date for _, _, date in pairs.values()})
    date_index = {date: i for i, date in enumerate(dates)}

    with open(out_path, "w", encoding="utf-8", newline="\n") as f:
        f.write("v1\n")
        f.write(" ".join(dates) + "\n")
        f.write(" ".join(keys) + "\n")
        for pair_key in sorted(pairs):
            left, right, date = pairs[pair_key]
            f.write("{},{},{}\n".format(left, right, date_index[date]))

    print("{} emojis, {} pairs, {} dates".format(len(keys), len(pairs), len(dates)))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
