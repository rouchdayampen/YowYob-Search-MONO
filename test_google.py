import urllib.request
import re

url = "https://www.google.cm/search?q=tchokos+sarl"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
try:
    html = urllib.request.urlopen(req).read().decode('utf-8')
    def extract(pattern, name):
        match = re.search(pattern, html)
        print(f"{name}: {match.group(1) if match else 'Not found'}")
    
    extract(r'Tchokos SARL', 'Found Name')
    
    # Dump HTML to a file so we can view it.
    with open('google_dump.html', 'w') as f:
        f.write(html)
    print("HTML dumped to google_dump.html")
except Exception as e:
    print("Error:", e)
