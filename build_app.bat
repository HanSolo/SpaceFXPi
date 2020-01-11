@ECHO OFF

rem ------ ENVIRONMENT --------------------------------------------------------
rem The script depends on various environment variables to exist in order to
rem run properly. The java version we want to use, the location of the java
rem binaries (java home), and the project version as defined inside the pom.xml
rem file, e.g. 1.0-SNAPSHOT.
rem
rem PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
rem APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

set JAVA_VERSION=13
set MAIN_JAR=SpaceFX-1.0-SNAPSHOT.jar

rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST build\java-runtime rmdir /S /Q  .\build\java-runtime
IF EXIST build\installer rmdir /S /Q target\installer

xcopy /S /Q build\libs\* build\installer\input\libs\
copy build\libs\%MAIN_JAR% build\installer\input\libs\

rem ------ REQUIRED MODULES ---------------------------------------------------
rem Use jlink to detect all modules that are required to run the application.
rem Starting point for the jdep analysis is the set of jars being used by the
rem application.

echo detecting required modules

"%JAVA_HOME%\bin\jdeps" ^
  --multi-release %JAVA_VERSION% ^
  --ignore-missing-deps ^
  --class-path "build\installer\input\libs\*" ^
  --print-module-deps build\classes\java\main\eu\hansolo\spacefx\SpaceFX.class > temp.txt

set /p detected_modules=<temp.txt

echo detected modules: %detected_modules%

rem ------ MANUAL MODULES -----------------------------------------------------
rem jdk.crypto.ec has to be added manually bound via --bind-services or
rem otherwise HTTPS does not work.
rem
rem See: https://bugs.openjdk.java.net/browse/JDK-8221674

set manual_modules=jdk.crypto.ec
echo manual modules: %manual_modules%

rem ------ RUNTIME IMAGE ------------------------------------------------------
rem Use the jlink tool to create a runtime image for our application. We are
rem doing this is a separate step instead of letting jlink do the work as part
rem of the jpackage tool. This approach allows for finer configuration and also
rem works with dependencies that are not fully modularized, yet.

echo creating java runtime image

call "%JAVA_HOME%\bin\jlink" ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2 ^
  --strip-debug ^
  --add-modules %detected_modules%,%manual_modules% ^
  --output build/java-runtime


rem ------ PACKAGING ----------------------------------------------------------
rem A loop iterates over the various packaging types supported by jpackage. In
rem the end we will find all packages inside the target/installer directory.

for %%s in ("app-image" "msi" "exe") do call "%JPACKAGE_HOME%\bin\jpackage" ^
  --type %%s ^
  --dest build/installer ^
  --input build/installer/input/libs ^
  --name SpaceFX ^
  --main-class eu.hansolo.spacefx.Launcher ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image target/java-runtime ^
  --icon src\main\resources\eu\hansolo\spacefx\icon.ico ^
  --app-version %APP_VERSION% ^
  --vendor "Alliance" ^
  --copyright "Copyright © 2019 Alliance"
