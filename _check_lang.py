import json
zh = json.load(open(r"D:\mod\1.20.1\forgeborneodyssey\src\main\resources\assets\forgeborneodyssey\lang\zh_cn.json", "r", encoding="utf-8"))
en = json.load(open(r"D:\mod\1.20.1\forgeborneodyssey\src\main\resources\assets\forgeborneodyssey\lang\en_us.json", "r", encoding="utf-8"))
ze = set(zh) - set(en)
ez = set(en) - set(zh)
print("Only in zh_cn (%d):" % len(ze))
for k in sorted(ze):
    print("  " + k)
print("Only in en_us (%d):" % len(ez))
for k in sorted(ez):
    print("  " + k)
