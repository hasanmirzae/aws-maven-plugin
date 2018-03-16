# aws-maven-plugin

## How to create a maven repository in github

####  1 - build the plugin project to have the latest jar

####  2 - create a directory (here named repository)

####  3 - add this folder into .gitignore

#### 4 - Install the jar in that directory (repository)

`
mvn install:install-file -DgroupId=com.epam -DartifactId=aws-maven-plugin -Dversion=1.0.0-SNAPSHOT -Dfile=target/aws-maven-plugin-1.0.0-SNAPSHOT.jar -Dpackaging=jar -DgeneratePom=false -DpomFile=pom.xml -DlocalRepositoryPath=repository  -DcreateChecksum=true
`

####  5 - Create a new branch (here named mvn-repo) and switch into the new branch

`git checkout -b mvn-repo`

####  6 - remove src, pom.xml, ... except than repository

####  7 - commit the changes on branch mvn-repo and push to origin

`git add . && git commit -m "release x.y.z"`

`git push origin mvn-repo`

###  Repository url

`
https://raw.github.com/GITHUB_USERNAME/GITHUB_REPOSITORY/BRANCH_NAME/repository/
https://raw.github.com/hasanmirzae/aws-maven-plugin/mvn-repo/repository
`
####  Example of adding repository in pom.xml
```xml
	<pluginRepositories>
		<pluginRepository>
			<id>hasanmirzae-github-aws-maven-plugin-repo</id>
			<url>https://raw.github.com/hasanmirzae/aws-maven-plugin/mvn-repo/repository</url>
			<layout>default</layout>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
			<releases>
				<enabled>true</enabled>
			</releases>
		</pluginRepository>
	</pluginRepositories>
```
