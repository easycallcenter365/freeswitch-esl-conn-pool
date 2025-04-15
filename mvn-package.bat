pushd   %~dp0
mvn  clean package -Dmaven.test.skip=true
pause