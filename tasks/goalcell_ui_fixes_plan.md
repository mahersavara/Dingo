# GoalCell UI Fixes Plan

## Issues Identified
1. **Duplicate sparkle icon** (✨) - cần đổi thành đồng hồ cát cho ACTIVE goals
2. **Màu background hình vuông** trong GoalCell - cần loại bỏ hoàn toàn
3. **Archived banner đè lên xấu** - cần thiết kế banner đẹp hơn và rõ ràng hơn

## Analysis
Cần kiểm tra:
- GoalCell.kt để tìm duplicate sparkle icons
- Background colors nào đang tạo hình vuông
- Archived overlay design hiện tại

## Todo List ✅
- [x] Tạo plan fix các vấn đề GoalCell
- [x] Fix duplicate sparkle icon và đổi thành đồng hồ cát
- [x] Loại bỏ màu background hình vuông trong GoalCell
- [x] Cải thiện archived banner đẹp hơn và rõ hơn
- [x] Test và compile sau khi fix

## ✨ Implementation Summary

### 🔧 **Issues Fixed:**

#### **1. Duplicate Sparkle Icon → Hourglass**
- ✅ **ACTIVE goals**: Thay đổi từ ✨ thành ⏳ (hourglass)
- ✅ **COMPLETED goals**: Giữ nguyên ✨ DONE với gold theme colors
- ✅ **Single unique icon** cho mỗi status

#### **2. Square Background Color Removal**
- ✅ **Consistent glass transparency**: 0.2 alpha cho tất cả status
- ✅ **No background color changes**: Chỉ sử dụng overlay effects
- ✅ **Clean glass appearance**: Loại bỏ hard background colors

#### **3. Beautiful Archived Banner**
- ✅ **Diagonal ribbon design**: Professional diagonal banner thay vì đè overlay xấu
- ✅ **Mountain theme colors**: `MountainShadow` với gradient depth
- ✅ **Better visibility**: White text trên dark ribbon với shadow depth
- ✅ **Elegant appearance**: Diagonal rotation (-12°) cho modern look

### 🎨 **Design Improvements:**
- **Professional ribbon**: Thay thế stamp-style overlay
- **Theme compliance**: Mountain Sunrise colors throughout
- **Visual hierarchy**: Clear distinction between status states
- **Better readability**: White text on dark ribbon for archived

### ⚙️ **Technical Quality:**
- **Compile success**: No errors or warnings
- **Import cleanup**: Added Size geometry import
- **Code consistency**: Maintained existing patterns
- **Theme integration**: Full Mountain Sunrise theme compliance

## Implementation Strategy
1. **Active Icon Fix**: Thay thế ✨ bằng ⏳ hoặc ⌛ (hourglass/sand clock)
2. **Background Fix**: Tìm và loại bỏ các background color gây ra hình vuông
3. **Archived Banner**: Design overlay mới với better visibility và aesthetics
4. **Consistency**: Ensure all changes maintain glass theme và Mountain Sunrise vibe

## Expected Outcome
- Single hourglass icon cho ACTIVE goals
- Clean glass background không có màu vuông lạ
- Beautiful archived banner thể hiện rõ trạng thái archived