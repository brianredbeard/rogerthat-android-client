#!/usr/bin/env bash
/home/lucas/Android/Sdk/platform-tools/adb logcat -v threadtime ActivityManager:I CycledLeScannerForLollipop:V BeaconManager:V MC:V FacebookSDK.WebDialog:D AndroidRuntime:E *:S | perl -pe 's/^.*I MC.*?:/\e[1;32m$&\e[0m/g; s/^.* W MC.*$/\n\e[1;33m$&\e[0m\n/g; s/^.*E MC.*$/\e[1;31m$&\e[0m/g; s/^.*D MC.*?:/\e[1;34m$&\e[0m/g;'
