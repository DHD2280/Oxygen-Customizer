# QS style packs

A QS style pack is an APK that describes how the Quick Settings tile edge should be
drawn. OxygenOS 17 builds its tile backgrounds in code, so a pack cannot ship drawables
for them the way a notification style pack does. It ships values instead.

Packs are listed under Modifications, QS style packs. Selecting one stores
`<package>/<activity>` in the `qs_style_pack` preference, and the pack owns the tile edge
for as long as it is selected. Reset returns the tiles to the manual controls under
Quick Settings, Tiles customization, Tile edge.

The tile edge needs OxygenOS 17 or newer. On older releases the menu stays, but a
selected pack draws nothing.

## Declaring a style

One activity per style, with the pack action and the default category. The activity is
never started, it only carries the label and names the value array.

```xml
<activity
    android:name="ControlCenter"
    android:label="@string/style_control_center"
    android:exported="true">
    <intent-filter>
        <action android:name="it.dhd.oxygencustomizer.qsstyle" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

A `string-array` named after the activity holds the values:

```xml
<string-array name="ControlCenter">
    <item>border.width=1dp</item>
    <item>border.color.dark=#59FFFFFF</item>
    <item>border.color.light=#1A000000</item>
    <item>border.active=false</item>
</string-array>
```

An optional `packauthor` meta-data on `<application>` names the author, shown under the
style in the list.

## Values

| Key | Type | Default | Meaning |
| --- | --- | --- | --- |
| `border.width` | dp | `1dp` | Stroke width. `0` disables the edge. |
| `border.color.dark` | `#AARRGGBB` | `#59FFFFFF` | Edge color in dark mode. |
| `border.color.light` | `#AARRGGBB` | `#1A000000` | Edge color in light mode. |
| `border.color.dark.bottom` | `#AARRGGBB` | unset | Second stop of a top to bottom gradient in dark mode. Unset draws a flat edge. |
| `border.color.light.bottom` | `#AARRGGBB` | unset | The same in light mode. |
| `border.inset` | dp | `0dp` | Pulls the edge towards the centre of the tile, so it reads as a ring. |
| `border.active` | bool | `false` | Whether active toggles also get an edge. Control Center leaves them borderless. |
| `preview` | drawable name | unset | A preview image for the list. Without one the list draws three tiles from the values themselves. |

Unknown keys are ignored, and a malformed value falls back to the default, so a pack
written against a newer OxygenCustomizer still loads on an older one.

## Notes for pack authors

The edge is painted after the ROM has drawn the tile, following the shape the ROM drew
with, so it tracks whatever tile shape the user picked under Quick Settings. It does not
replace the tile background: the fill, the blur and the icon stay the ROM's own, and the
existing color controls under Tiles customization still apply on top.
