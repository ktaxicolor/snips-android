
**/!\ Warning : 
This project is ongoing and in its first stages. It is not stable !**

# Android assistant with Snips

Based on https://github.com/snipsco/snips-platform-android-demo  
Snips is an privacy-by-design opensource vocal assistant you can find at https://github.com/snipsco or http://www.snips.ai/

This project is an Android app using Snips vocal assistant.  
Features : 
- Music control : 
    - pause/play, next song, volume controls > compatible with all android media apps (use Android keys)
    - open music app and play artist/song/playlist > only Deezer for now


### Install
- Snips assistant must be created and downloaded on http://www.snips.ai/. You can find the documentation here https://github.com/snipsco/snips-platform-documentation/wiki. Folder snips_android_assistant must then be copied to the phone internal storage root (emulated/0 or similar). 
- Update hotword in src/main/res/values/strings.xml with the value of you downloaded Snips assistant.  
`<string name="hotword">Jarvis</string>`


