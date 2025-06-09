# Mountain Sunrise UI Mockup

## Home Screen

```
┌─────────────────────────────────────┐
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ALPINE EXPLORER             │    │
│  │                         ⋮   │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │  Good morning, Adventurer   │    │
│  │                             │    │
│  │  Today's sunrise: 6:24 AM   │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ FEATURED JOURNEY            │    │
│  │                             │    │
│  │ [Mountain Silhouette Image] │    │
│  │                             │    │
│  │ Fansipan Sunrise Trek       │    │
│  │ Elevation: 3,143m           │    │
│  │ Duration: 2 days            │    │
│  │                             │    │
│  │ ┌───────────┐  ┌───────────┐│    │
│  │ │ DETAILS   │  │ SAVE      ││    │
│  │ └───────────┘  └───────────┘│    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ UPCOMING ADVENTURES         │    │
│  │                             │    │
│  │ ┌─────────────┐ ┌──────────┐│    │
│  │ │             │ │          ││    │
│  │ │ Mont Blanc  │ │ Matterhorn││   │
│  │ │ July 15     │ │ Aug 3    ││    │
│  │ │             │ │          ││    │
│  │ └─────────────┘ └──────────┘│    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────┐    ┌─────┐    ┌─────┐     │
│  │ HOME│    │EXPLORE│   │PROFILE│   │
│  └─────┘    └─────┘    └─────┘     │
└─────────────────────────────────────┘
```

## Color & Style Implementation

### App Bar
- Background: Deep Indigo (#1F2937)
- Title: "ALPINE EXPLORER" in Cloud Gray (#CBD5E0) using Playfair Display
- Menu icon (⋮): Rustic Gold (#D69E2E)
- Sharp bottom edge with 1px Mountain Shadow (#4A5568) border

### Welcome Card
- Background: White (#FFFFFF)
- Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
- Heading: "Good morning, Adventurer" in Deep Indigo (#1F2937) using Playfair Display
- Subtext: "Today's sunrise: 6:24 AM" in Mountain Shadow (#4A5568) using Lora
- Top accent: 2px Rustic Gold (#D69E2E) border

### Featured Journey Card
- Background: White (#FFFFFF)
- Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
- Section Title: "FEATURED JOURNEY" in Deep Purple (#553C9A) using Cormorant Garamond
- Image: Mountain silhouette with sunrise gradient overlay
- Journey Title: "Fansipan Sunrise Trek" in Deep Indigo (#1F2937) using Playfair Display
- Details: "Elevation: 3,143m" and "Duration: 2 days" in Mountain Shadow (#4A5568) using Lora
- Buttons:
  - "DETAILS": White background, Deep Indigo (#1F2937) text, 1px border
  - "SAVE": Rustic Gold (#D69E2E) background, white text, no border

### Upcoming Adventures Section
- Section Title: "UPCOMING ADVENTURES" in Deep Purple (#553C9A) using Cormorant Garamond
- Adventure Cards:
  - Background: White with subtle Amber Horizon (#F6AD55) gradient overlay
  - Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
  - Adventure Name: "Mont Blanc" / "Matterhorn" in Deep Indigo (#1F2937) using Playfair Display
  - Date: "July 15" / "Aug 3" in Mountain Shadow (#4A5568) using Lora

### Bottom Navigation
- Background: Midnight Blue (#2D3748)
- Text and Icons:
  - Active: Rustic Gold (#D69E2E)
  - Inactive: Cloud Gray (#CBD5E0)
- Active Tab Indicator: 2px Rustic Gold (#D69E2E) top border, sharp edges
- No dividers between tabs

## Detail Screen

```
┌─────────────────────────────────────┐
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ←     JOURNEY DETAILS       │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │ [Sunrise Mountain Panorama] │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │  Fansipan Sunrise Trek      │    │
│  │  ★★★★☆ (4.8)                │    │
│  │                             │    │
│  │  Northern Vietnam           │    │
│  │                             │    │
│  │  ────────────────────────   │    │
│  │                             │    │
│  │  JOURNEY DETAILS            │    │
│  │                             │    │
│  │  Elevation: 3,143m          │    │
│  │  Duration: 2 days           │    │
│  │  Difficulty: Moderate       │    │
│  │  Best Season: Sep-Nov       │    │
│  │                             │    │
│  │  ────────────────────────   │    │
│  │                             │    │
│  │  DESCRIPTION                │    │
│  │                             │    │
│  │  Experience the magic of    │    │
│  │  sunrise from Vietnam's     │    │
│  │  highest peak. This trek    │    │
│  │  offers breathtaking views  │    │
│  │  of misty mountains and     │    │
│  │  valleys below.             │    │
│  │                             │    │
│  │  ┌───────────────────────┐  │    │
│  │  │      BOOK JOURNEY     │  │    │
│  │  └───────────────────────┘  │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Detail Screen Implementation

#### App Bar
- Background: Deep Indigo (#1F2937)
- Back arrow (←): Cloud Gray (#CBD5E0)
- Title: "JOURNEY DETAILS" in Cloud Gray (#CBD5E0) using Playfair Display
- Sharp bottom edge with 1px Mountain Shadow (#4A5568) border

#### Hero Image
- Full-width panorama of sunrise mountains
- Subtle gradient overlay: Deep Purple (#553C9A) to transparent

#### Content Card
- Background: White (#FFFFFF)
- Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
- Journey Title: "Fansipan Sunrise Trek" in Deep Indigo (#1F2937) using Playfair Display, 20sp
- Rating: Stars in Rustic Gold (#D69E2E)
- Location: "Northern Vietnam" in Mountain Shadow (#4A5568) using Lora
- Dividers: 1px lines in Mountain Shadow (#4A5568) at 30% opacity with ornamental ends

#### Section Headers
- "JOURNEY DETAILS" and "DESCRIPTION" in Deep Purple (#553C9A) using Cormorant Garamond, 16sp
- Details text in Mountain Shadow (#4A5568) using Lora, 14sp
- Description text in Mountain Shadow (#4A5568) using Lora, 14sp

#### Book Button
- Background: Rustic Gold (#D69E2E)
- Text: White (#FFFFFF) using Playfair Display, 16sp
- Border: None, sharp corners (0px radius)
- Full width with adequate padding (16dp vertical)

## Profile Screen

```
┌─────────────────────────────────────┐
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ADVENTURER PROFILE      ⚙   │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │          [Avatar]           │    │
│  │                             │    │
│  │       Alpine Explorer       │    │
│  │       Member since 2022     │    │
│  │                             │    │
│  │  ┌───────────┐ ┌───────────┐│    │
│  │  │ 24 TREKS  │ │ 8 BADGES  ││    │
│  │  └───────────┘ └───────────┘│    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ JOURNEY LOG                 │    │
│  │                             │    │
│  │ ┌─────────────────────────┐ │    │
│  │ │ Mont Blanc              │ │    │
│  │ │ July 2023               │ │    │
│  │ │ ★★★★★                    │ │    │
│  │ └─────────────────────────┘ │    │
│  │                             │    │
│  │ ┌─────────────────────────┐ │    │
│  │ │ Fansipan                │ │    │
│  │ │ March 2023              │ │    │
│  │ │ ★★★★☆                    │ │    │
│  │ └─────────────────────────┘ │    │
│  │                             │    │
│  │ ┌─────────────────────────┐ │    │
│  │ │ Everest Base Camp       │ │    │
│  │ │ October 2022            │ │    │
│  │ │ ★★★★★                    │ │    │
│  │ └─────────────────────────┘ │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────┐    ┌─────┐    ┌─────┐     │
│  │ HOME│    │EXPLORE│   │PROFILE│   │
│  └─────┘    └─────┘    └─────┘     │
└─────────────────────────────────────┘
```

### Profile Screen Implementation

#### App Bar
- Background: Deep Indigo (#1F2937)
- Title: "ADVENTURER PROFILE" in Cloud Gray (#CBD5E0) using Playfair Display
- Settings icon (⚙): Rustic Gold (#D69E2E)

#### Profile Card
- Background: White (#FFFFFF) with subtle Amber Horizon (#F6AD55) gradient
- Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
- Name: "Alpine Explorer" in Deep Indigo (#1F2937) using Playfair Display, 18sp
- Subtitle: "Member since 2022" in Mountain Shadow (#4A5568) using Lora, 14sp
- Stats Boxes:
  - Background: White (#FFFFFF)
  - Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
  - Text: Deep Purple (#553C9A) using Cormorant Garamond

#### Journey Log Section
- Section Title: "JOURNEY LOG" in Deep Purple (#553C9A) using Cormorant Garamond, 16sp
- Journey Cards:
  - Background: White (#FFFFFF)
  - Border: 1px solid Mountain Shadow (#4A5568), no rounded corners
  - Journey Name: Deep Indigo (#1F2937) using Playfair Display, 16sp
  - Date: Mountain Shadow (#4A5568) using Lora, 14sp
  - Rating: Stars in Rustic Gold (#D69E2E)

#### Bottom Navigation
- Same as Home Screen
- "PROFILE" tab active with Rustic Gold (#D69E2E) text and top indicator 