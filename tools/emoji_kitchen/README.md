# Emoji Kitchen index

`java/assets/emoji_kitchen/index.txt` lists which emoji pairs have an Emoji Kitchen
sticker and where it is hosted. It is generated from `app/metadata.json` of
[xsalazar/emoji-kitchen-backend](https://github.com/xsalazar/emoji-kitchen-backend) (MIT):

```sh
curl -LO https://raw.githubusercontent.com/xsalazar/emoji-kitchen-backend/main/app/metadata.json
python3 build_index.py metadata.json ../../java/assets/emoji_kitchen/index.txt
```

The sticker images themselves are made by Google and are not bundled. The keyboard downloads them
from `www.gstatic.com` when the Emoji Kitchen row is shown, which needs the network permission.
