# Mountain Sunrise Style Guide

## Design Philosophy

The Mountain Sunrise app design embodies the spirit of early-morning mountain exploration combined with 19th-century European aesthetics. The design evokes feelings of:

- **Adventure**: Inspiring exploration and discovery
- **Nostalgia**: Reminiscent of vintage travel journals and postcards
- **Romance**: Capturing the emotional connection to beautiful landscapes
- **Warmth**: Conveying the cozy feeling of sunrise light
- **Timelessness**: Drawing from classical design principles
- **Rustic Elegance**: Balancing rugged adventure with refined details

## Key Design Elements

### Sharp Borders & Crisp Edges

```
┌─────────────────────┐    ┌─────────────────────┐
│                     │    │                     │
│     CORRECT         │    │     INCORRECT       │
│  (Sharp corners)    │    │  (Rounded corners)  │
│                     │    │                     │
└─────────────────────┘    └─────────────────────┘
```

- All UI elements feature sharp 90° corners
- No rounded corners or border-radius
- Crisp, precise edges on all containers, buttons, and cards
- Thin (1px) borders with high contrast

### Typography Hierarchy

```
ALPINE EXPLORER
─────────────────────────
Playfair Display, 20sp, #1F2937

Journey Title
─────────────────────────
Playfair Display, 18sp, #1F2937

SECTION HEADERS
─────────────────────────
Cormorant Garamond, 16sp, #553C9A

Body text and descriptions
─────────────────────────
Lora, 14sp, #4A5568
```

- Serif fonts exclusively for vintage European feel
- Generous letter-spacing for headers (tracking: +5%)
- Proper hierarchy with clear size differentiation
- Ornamental capitals for special features

### Vintage-Inspired Decorative Elements

```
┌─────────────────────────────────────┐
│                                     │
│  ─────────── § ───────────          │
│                                     │
│  ────────── ✧❋✧ ──────────          │
│                                     │
│  ───────── ❈❈❈❈❈ ─────────          │
│                                     │
└─────────────────────────────────────┘
```

- Subtle ornamental dividers between sections
- Classic symbols and glyphs as accents
- Filigree-inspired decorative corners for special cards
- Vintage compass rose motifs for navigation elements

### Texture & Layering

- Subtle paper texture overlay (5-10% opacity)
- Gentle noise texture in background elements
- Layered elements with thin borders for depth
- Vintage map-like patterns for background elements

### Iconography

```
┌─────────────────────────────────────┐
│                                     │
│    ⛰️    ☀️    🧭    🏔️    📜       │
│                                     │
│  Mountain  Sun  Compass  Peak  Map  │
│                                     │
└─────────────────────────────────────┘
```

- Simple, classic line-based icons
- Vintage explorer and cartography symbols
- Consistent stroke weight (1.5px)
- No filled icons - outline style only

## Layout Principles

### White Space & Composition

- Generous margins (16dp minimum)
- Consistent vertical rhythm
- Golden ratio (1:1.618) for key layout proportions
- Asymmetrical balance for visual interest

### Grid System

```
┌─────┬─────┬─────┬─────┐
│     │     │     │     │
├─────┼─────┼─────┼─────┤
│     │     │     │     │
├─────┼─────┼─────┼─────┤
│     │     │     │     │
├─────┼─────┼─────┼─────┤
│     │     │     │     │
└─────┴─────┴─────┴─────┘
```

- 8dp base grid
- 4-column layout for content organization
- Consistent spacing between elements (8dp, 16dp, 24dp)
- Alignment to grid for all elements

## Animation & Interaction

### Transitions

- Subtle fade transitions (300ms)
- Gentle scaling (95% to 100%)
- No bouncy or playful animations
- Dignified, elegant motion

### Button States

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│                 │  │                 │  │                 │
│     Normal      │  │     Hover       │  │     Pressed     │
│                 │  │                 │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
   Base color         5% lighter          10% darker
```

- Subtle brightness changes for hover states
- Slight darkening for pressed states
- No scaling or transformation animations
- Optional subtle border highlight

## Photography & Imagery

- Sunrise/sunset mountain landscapes
- Vintage color grading (slightly desaturated, warm)
- Subtle vignetting on photos
- Film grain texture overlay (5% opacity)

## Accessibility Considerations

- Minimum 4.5:1 contrast ratio for all text
- Clear visual hierarchy for readability
- Sufficient touch targets (minimum 48x48dp)
- Alternative visual indicators beyond color

## Implementation Notes

- Use crisp 1x, 2x, and 3x assets for sharp rendering
- Implement custom font rendering for serif typefaces
- Ensure consistent border rendering across devices
- Use vector assets where possible for ornamental elements 