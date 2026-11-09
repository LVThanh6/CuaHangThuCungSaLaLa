import glob
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

pattern = re.compile(r'[\u2000-\u3300\U0001F000-\U0001F9FF]')
keep = {'₫', '—', '–', '“', '”', '…', '•', '\u00a0', '%'}

found = 0
for f in glob.glob('src/GUI/**/*.java', recursive=True):
    with open(f, 'r', encoding='utf-8') as file:
        lines = file.readlines()
    matches = []
    for i, line in enumerate(lines):
        m = pattern.findall(line)
        icons = [c for c in m if c not in keep]
        if icons:
            matches.append((i+1, ''.join(set(icons)), line.strip()))
    if matches:
        found += len(matches)
        print(f"=== {f} ({len(matches)} matches) ===")
        for line_no, ic, text in matches:
            print(f"  L{line_no}: [{ic}] {text[:90]}")

print(f"\nTotal remaining icon symbols found: {found}")
