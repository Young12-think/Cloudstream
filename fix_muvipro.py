import os
import re

def fix_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    changed = False

    # Fix 1: attr("src") to getIframeAttr() in AJAX calls
    old_ajax = r'\.select\("iframe"\)\.attr\("src"\)\.let\s*\{\s*httpsify\(it\)\s*\}'
    new_ajax = r'.selectFirst("iframe")?.getIframeAttr()?.let { httpsify(it) }'
    
    if re.search(old_ajax, content):
        content = re.sub(old_ajax, new_ajax, content)
        changed = True

    # Add getIframeAttr extension if missing
    if 'fun Element?.getIframeAttr()' not in content and 'fun Element.getIframeAttr()' not in content:
        if 'getIframeAttr' in content:
            # needs the function
            fn = """
    private fun Element?.getIframeAttr(): String? = this?.attr("data-litespeed-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("data-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("src")
"""
            # insert before the last closing brace
            content = content[:content.rfind('}')] + fn + content[content.rfind('}'):]
            changed = True
        
    if changed:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {path}")

for root, _, files in os.walk('C:/Users/YOUNG/cloudstream-repo/plugin'):
    for f in files:
        if f.endswith('.kt'):
            fix_file(os.path.join(root, f))
