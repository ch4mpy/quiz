#!/bin/sh
echo "***************************************************************************************************************************************"
echo "* To build Spring Boot native images, run with the \"native\" argument: \"sh ./build.sh native\" (images will take much longer to build). *"
echo "*                                                                                                                                     *"
echo "* This build script tries to auto-detect ARM64 (Apple Silicon) to build the appropriate Spring Boot Docker images.                    *"
echo "***************************************************************************************************************************************"
echo ""

if [[ "$OSTYPE" == "darwin"* ]]; then
  SED="sed -i '' -e"
else
  SED="sed -i -e"
fi

MAVEN_PROFILES=()
if [[ `uname -m` == "arm64" ]]; then
  MAVEN_PROFILES+=("arm64")
fi
if [[ " $@ " =~ [[:space:]]native[[:space:]] ]]; then
    MAVEN_PROFILES+=("native")
fi
if [ ${#MAVEN_PROFILES[@]} -eq 0 ]; then
    MAVEN_PROFILE_ARG=""
else
    MAVEN_PROFILE_ARG=-P$(IFS=, ; echo "${MAVEN_PROFILES[*]}")
fi

host=$(hostname)
if [ -z "${host}" ]; then
  host="localhost"
fi

cd api
echo "************************************"
echo "sh ./mvnw clean install -Popenapi,h2"
echo "************************************"
echo ""
sh ./mvnw clean install -Popenapi,h2

echo ""
echo "*****************************************************************************************************************************************"
echo "sh ./mvnw -pl quiz-api spring-boot:build-image -DskipTests $MAVEN_PROFILE_ARG"
echo "*****************************************************************************************************************************************"
echo ""
sh ./mvnw spring-boot:build-image -DskipTests $MAVEN_PROFILE_ARG -pl quiz-api

echo ""
echo "*****************************************************************************************************************"
echo "sh ./mvnw -pl bff spring-boot:build-image -DskipTests $MAVEN_PROFILE_ARG"
echo "*****************************************************************************************************************"
echo ""
sh ./mvnw spring-boot:build-image -DskipTests $MAVEN_PROFILE_ARG -pl bff
cd ..

rm -f "compose-${host}.yml"
cp compose.yml "compose-${host}.yml"
$SED "s/LOCALHOST_NAME/${host}/g" "compose-${host}.yml"
rm -f "compose-${host}.yml''"

rm keycloak/import/quiz-realm.json
cp quiz-realm.json keycloak/import/quiz-realm.json
$SED "s/LOCALHOST_NAME/${host}/g" keycloak/import/quiz-realm.json
rm "keycloak/import/quiz-realm.json''"

cd angular-ui/
rm proxy.conf.json src/app/app.config.ts
cp ../angular-ui.app.config.ts src/app/app.config.ts
$SED "s/LOCALHOST_NAME/${host}/g" src/app/app.config.ts
rm "src/app/app.config.ts''"
cp ../proxy.conf.json proxy.conf.json
$SED "s/LOCALHOST_NAME/${host}/g" proxy.conf.json
rm "proxy.conf.json''"
npm i
npm run build
cd ..

docker build -t quiz-nginx-reverse-proxy ./nginx-reverse-proxy
docker build -t quiz-ui ./angular-ui
docker build -t quiz-well-known ./.well-known

docker compose -f compose-${host}.yml up -d

echo ""
echo "Open the following in a new private navigation window."

echo ""
echo "Keycloak as admin / admin:"
echo "http://${host}/auth/admin/master/console/#/quiz"

echo ""
echo "Sample user author / author"
echo http://${host}/ui/