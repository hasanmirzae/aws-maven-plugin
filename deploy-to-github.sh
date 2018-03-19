version=1.0.0-SNAPSHOT

echo "Building plugin ..."
mvn clean install

rm repository -Rf && mkdir repository

echo "Installing build into local repository ..."
mvn install:install-file -DgroupId=com.epam -DartifactId=aws-maven-plugin -Dversion=$version -Dfile=target/aws-maven-plugin-$version.jar -Dpackaging=jar -DgeneratePom=false -DpomFile=pom.xml -DlocalRepositoryPath=repository -DcreateChecksum=true

echo "Checkout mvn-repo branch"
git checkout mvn-repo


echo "Committing changes on mvn-repo ..."
git add repository && git commit -m "release $version"

echo "Publishing to github ..."
git push origin mvn-repo -f
echo "Checkout to master"
git checkout master

