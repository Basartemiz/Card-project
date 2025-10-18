#!/usr/bin/env python3
"""
find_discarded_then_reused.py

Scan a log file and identify cards that:
  (1) were discarded (a line contains "... Survivor plays <card> ... is discarded")
  (2) later appear again as played ("... Survivor plays <card> ...")

Outputs a report with all (discard_line -> reuse_line) pairs per card.
Optionally writes a CSV.

Example:
    python find_discarded_then_reused.py type2_small_seed1001.txt
    python find_discarded_then_reused.py type2_small_seed1001.txt --csv out.csv
"""

import re
import argparse
import csv
from collections import defaultdict, deque
from typing import Dict, Deque, List, Tuple, Optional

# Matches any "Survivor plays <card>" line (captures the card ID and the whole line for context)
PLAY_LINE_RE = re.compile(
    r"Found with priority\s+\d+,\s*Survivor plays\s+(large\d+)\s*,\s*(.*)$"
)

# Specifically identifies a discard event among play lines
DISCARDED_RE = re.compile(r"\bis discarded\b")

def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="Find cards that were discarded and later reused.")
    p.add_argument("logfile", help="Path to the log file (e.g., type2_small_seed1001.txt)")
    p.add_argument("--csv", help="Optional CSV output path", default=None)
    p.add_argument("--max", type=int, default=None,
                   help="Stop after scanning this many lines (debugging/preview)")
    return p.parse_args()

def scan_log(
    path: str,
    max_lines: Optional[int] = None
) -> Tuple[
        Dict[str, List[Tuple[int, int]]],   # card -> list of (discard_line, reuse_line)
        Dict[str, List[Tuple[int, str]]],   # card -> list of (line_no, context_str) for discards
        Dict[str, List[Tuple[int, str]]]    # card -> list of (line_no, context_str) for reuses that matched
    ]:
    """
    Reads the log and returns:
      - reuse_pairs: for each card, a list of (discard_line, reuse_line) pairs
      - discard_contexts: for each card, all discard events with (line_no, context)
      - reuse_contexts: for each card, reuse events that closed a discard with (line_no, context)
    """
    # For each card we keep a queue of "open" discard line numbers (not yet reused)
    open_discards: Dict[str, Deque[int]] = defaultdict(deque)

    reuse_pairs: Dict[str, List[Tuple[int, int]]] = defaultdict(list)
    discard_contexts: Dict[str, List[Tuple[int, str]]] = defaultdict(list)
    reuse_contexts: Dict[str, List[Tuple[int, str]]] = defaultdict(list)

    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line_no, raw in enumerate(f, start=1):
            if max_lines is not None and line_no > max_lines:
                break
        
            line = raw.rstrip("\n")

            # We only care about lines that match the general "Survivor plays ..." pattern
            m = PLAY_LINE_RE.search(line)
            if not m:
                continue

            card = m.group(1)
            rest = m.group(2)  # everything after the comma, for context

            if DISCARDED_RE.search(rest):
                # This is a discard event
                open_discards[card].append(line_no)
                discard_contexts[card].append((line_no, line))
            else:
                # It's a play that isn't explicitly "is discarded" -> counts as a potential reuse
                # If there is any open discard for the same card, we match the earliest one
                if open_discards[card]:
                    discard_line = open_discards[card].popleft()
                    reuse_pairs[card].append((discard_line, line_no))
                    reuse_contexts[card].append((line_no, line))

    return reuse_pairs, discard_contexts, reuse_contexts

def print_report(
    reuse_pairs: Dict[str, List[Tuple[int, int]]],
    discard_contexts: Dict[str, List[Tuple[int, str]]],
    reuse_contexts: Dict[str, List[Tuple[int, str]]]
) -> None:
    total_pairs = sum(len(v) for v in reuse_pairs.values())
    print("\n=== Discarded-then-Reused Report ===")
    print(f"Total cards with at least one discard→reuse: {len(reuse_pairs)}")
    print(f"Total discard→reuse occurrences: {total_pairs}\n")

    if total_pairs == 0:
        print("No discarded cards were later reused.")
        return

    for card in sorted(reuse_pairs.keys()):
        pairs = reuse_pairs[card]
        print(f"{card}: {len(pairs)} occurrence(s)")
        # Build quick lookups for nice printing
        disc_map = {ln: ctx for ln, ctx in discard_contexts.get(card, [])}
        reuse_map = {ln: ctx for ln, ctx in reuse_contexts.get(card, [])}
        for (discard_ln, reuse_ln) in pairs:
            print(f"  - discarded at line {discard_ln}")
            if discard_ln in disc_map:
                print(f"      {disc_map[discard_ln]}")
            print(f"    reused at line    {reuse_ln}")
            if reuse_ln in reuse_map:
                print(f"      {reuse_map[reuse_ln]}")
        print()

def write_csv(
    csv_path: str,
    reuse_pairs: Dict[str, List[Tuple[int, int]]]
) -> None:
    """
    CSV columns: card, discard_line, reuse_line
    """
    rows = []
    for card, pairs in reuse_pairs.items():
        for discard_ln, reuse_ln in pairs:
            rows.append({"card": card, "discard_line": discard_ln, "reuse_line": reuse_ln})

    with open(csv_path, "w", newline="", encoding="utf-8") as fp:
        writer = csv.DictWriter(fp, fieldnames=["card", "discard_line", "reuse_line"])
        writer.writeheader()
        writer.writerows(rows)

def main():
    args = parse_args()
    reuse_pairs, discard_contexts, reuse_contexts = scan_log(args.logfile, max_lines=args.max)
    print_report(reuse_pairs, discard_contexts, reuse_contexts)
    if args.csv:
        write_csv(args.csv, reuse_pairs)
        print(f"\nCSV written to: {args.csv}")

if __name__ == "__main__":
    main()