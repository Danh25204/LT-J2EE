@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM mvnw.cmd — Maven Wrapper for Windows
@REM Tự động tải Maven 3.9.9 nếu chưa có trong ~/.m2/wrapper/dists/

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __ MVNW_CMD__=%~f0
@SET __MVNW_ERROR__=

@SETLOCAL

@SET MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

@SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@IF EXIST "%MAVEN_WRAPPER_JAR%" GOTO skipDownload

@SET MVNW_REPOURL=https://repo.maven.apache.org/maven2
FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

echo Downloading: %DOWNLOAD_URL%
"%JAVA_HOME%\bin\java.exe" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" ^
    org.apache.maven.wrapper.DefaultDownloader ^
    "%DOWNLOAD_URL%" "%MAVEN_WRAPPER_JAR%" 2>NUL

IF ERRORLEVEL 1 (
    @REM Try curl fallback
    curl.exe -L "%DOWNLOAD_URL%" -o "%MAVEN_WRAPPER_JAR%"
)

:skipDownload
@SET WRAPPER_LAUNCHER_CLASSPATH=%MAVEN_WRAPPER_JAR%

"%JAVA_HOME%\bin\java.exe" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  "-Dmaven.wrapper.propertiesFile=%MAVEN_WRAPPER_PROPERTIES%" ^
  "-classpath" "%WRAPPER_LAUNCHER_CLASSPATH%" ^
  %WRAPPER_LAUNCHER% %*

@ENDLOCAL
