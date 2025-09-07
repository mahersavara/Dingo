# Glass UI Design Implementation Plan

## Requirements
- Thêm transparency/opacity cho HomeScreen background
- Áp dụng glass effect cho các goal items
- Thiết kế dạng glass morphism
- Khi complete goal, hiệu ứng đẹp hơn nhưng không đổi màu background
- Giữ nguyên opacity như goal bình thường khi complete

## Analysis ✅
### Current UI Structure:
**HomeScreen.kt**:
- Uses DingoAppScaffold with useGradientBackground = true
- Currently opaque surface containers
- Existing completion celebration animations

**GoalCell.kt**: 
- Uses Card with MaterialTheme.colorScheme.surface for normal goals
- Completion overlay với overlay background Color(0x55FFFFFF)
- Status-based color changes: COMPLETED (light green), FAILED (light red), ARCHIVED (light gray)

### Glass Effect Design Parameters:
- **Background Alpha**: 0.15-0.25 for subtle transparency
- **Border**: 1-2px white/light gray with 0.3-0.5 alpha
- **Shadow**: Multiple layers with different blur radius
- **Backdrop Filter**: Blur effect behind glass elements
- **Completion**: Keep opacity same as normal goals, enhance with glow/scale effects

## Todo List ✅
- [x] Phân tích cấu trúc UI hiện tại của HomeScreen
- [x] Kiểm tra GoalCell design và completion states
- [x] Thiết kế glass effect parameters (opacity, blur, border)
- [x] Implement transparency cho HomeScreen background
- [x] Áp dụng glass morphism cho GoalCell
- [x] Cải thiện completion animation mà không đổi background color
- [x] Test và fine-tune glass effects
- [x] Đảm bảo code compile thành công

## ✨ Implementation Summary

### HomeScreen Changes:
- Added **transparency container** với `Color.White.copy(alpha = 0.08f)` để tạo glass effect subtile
- Import thêm blur và draw utilities cho future enhancements

### GoalCell Glass Morphism:
- **Container Transparency**: Sử dụng `MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)` cho glass effect
- **Glass Border**: Gradient border từ trắng mờ đến trong suốt hoàn toàn
- **Enhanced Shadow**: Tăng elevation từ 4dp lên 6dp (12dp khi drag) để tạo độ sâu
- **Completion Glow**: Thay thế màu background bằng radial gradient với green glow effect

### Status-Specific Transparency:
- **ACTIVE Goals**: 0.2 alpha - glass effect mạnh nhất
- **COMPLETED Goals**: 0.75 alpha - giữ visibility nhưng vẫn có glass
- **FAILED Goals**: 0.75 alpha - tương tự completed
- **ARCHIVED Goals**: 0.5 alpha - mờ hơn nhưng vẫn readable

### Completion Animation Enhancement:
- **Không thay đổi background color** như requirement
- **Radial gradient overlay** tạo hiệu ứng glow từ center
- **Green glow border** với gradient để highlight completion
- **Giữ nguyên opacity** như goal bình thường

## Implementation Strategy
1. Sử dụng Compose Modifier với alpha, blur effects
2. Glassmorphism với border, background blur
3. Completion animation với scale/glow effects thay vì color change
4. Maintain existing functionality while enhancing visual design