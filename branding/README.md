# VibeCam icon

| File | Use |
| --- | --- |
| `vibecam-icon.svg` / `.png` | Rounded square, for websites and documents |
| `vibecam-icon-circle.svg` / `.png` | Circle, as most Android launchers show it |
| `vibecam-icon-square.svg` / `.png` | Full square with no rounding, for stores that apply their own mask |
| `vibecam-icon-foreground.svg` | The artwork alone on a transparent 108 unit canvas, matching `app/src/main/res/drawable/ic_launcher_foreground.xml` |
| `vibecam-social-preview.svg` / `.png` | 1280 x 640 card with the icon, name and the line "It's a vibe.", for the repository's social preview |

Colours: background `#1E1B3A`, lines `#FFFFFF`, accent `#C2C7FF`.

The PNGs are 1024 px renders of the SVGs. To render again after editing an SVG:

```bash
qlmanage -t -s 1024 -o . vibecam-icon.svg && mv vibecam-icon.svg.png vibecam-icon.png
```

`qlmanage` only renders squares. For the social preview, render a square copy and crop it:

```bash
sed 's|width="1280" height="640" viewBox="0 0 1280 640"|width="1280" height="1280" viewBox="0 -320 1280 1280"|' \
  vibecam-social-preview.svg > /tmp/square.svg
qlmanage -t -s 1280 -o /tmp /tmp/square.svg
sips -c 640 1280 /tmp/square.svg.png --out vibecam-social-preview.png
```

## Name and icon

The MIT licence in this repository covers the source code. It does not cover the VibeCam name or
this icon, which identify builds published by this project. You are welcome to use them to refer
to VibeCam, for example in an article or an app listing. Please do not use them for a modified
version or for another product: a fork should ship under its own name and icon.
