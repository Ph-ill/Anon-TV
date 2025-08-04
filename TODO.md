# TODO - Anon TV

## Video Player Controls Issue
- **Problem**: Video controls are not behaving correctly when navigating between videos
- **Current State**: Controls show automatically and left/right navigation doesn't work as expected
- **Desired Behavior**: 
  - Controls should only show when Down button is pressed
  - When controls are hidden: left/right should navigate between videos
  - When controls are visible: left/right should seek within current video
- **Priority**: Medium - not blocking for 0.1.0 release
- **Files to fix**: `app/src/main/java/com/example/chan/MediaFragment.kt`

## Completed Features for 0.1.0
- ✅ Thread title display in bottom right corner of video player
- ✅ Pagination with loading indicators
- ✅ Clean video navigation (left/right between videos)
- ✅ Black background for video player
- ✅ Loading spinner for initial load
- ✅ Auto-loading when reaching last card
- ✅ Proper focus management

## Future Enhancements
- 🔄 Video controls behavior (see above)
- 🔄 Audio-only video display fixes
- 🔄 Project logo display in UI
- 🔄 Support for multiple boards beyond /wsg/ 