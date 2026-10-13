# VibeCam icon

| File | Use |
| --- | --- |
| `vibecam-icon.svg` / `.png` | Rounded square, for websites and documents |
| `vibecam-icon-circle.svg` / `.png` | Circle, as most Android launchers show it |
| `vibecam-icon-square.svg` / `.png` | Full square with no rounding, for stores that apply their own mask |
| `vibecam-icon-foreground.svg` | The artwork alone on a transparent 108 unit canvas, matching `app/src/main/res/drawable/ic_launcher_foreground.xml` |

Colours: background `#1E1B3A`, lines `#FFFFFF`, accent `#C2C7FF`.

The PNGs are 1024 px renders of the SVGs. To render again after editing an SVG:

```bash
qlmanage -t -s 1024 -o . vibecam-icon.svg && mv vibecam-icon.svg.png vibecam-icon.png
```

## Name and icon

The MIT licence in this repository covers the source code. It does not cover the VibeCam name or
this icon, which identify builds published by this project. You are welcome to use them to refer
to VibeCam, for example in an article or an app listing. Please do not use them for a modified
version or for another product: a fork should ship under its own name and icon.
