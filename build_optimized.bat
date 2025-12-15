@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
echo Building with optimized performance settings...
gradlew.bat clean assembleDebug
