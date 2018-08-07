up:
	docker-compose up -d
down:
	docker-compose down
install:
	mvn clean install -DskipTests -DskipDocker

docker:
	mvn clean install -DskipTests

deploy:
	mvn clean deploy -DskipTests

release:
	rm -f release.properties pom.xml.releaseBackup
	mvn release:prepare -B
	rm -f release.properties pom.xml.releaseBackup


tag:
	$(eval commit := $(shell git rev-parse --short HEAD))
	$(eval tag := $(shell mvn help:evaluate -Dexpression=project.version | grep -v "^\[" | sed 's/-SNAPSHOT/-$(commit)/'))
	git tag -a $(tag) -m "Branch: `git branch | grep \* | awk '{print $$2}'`"
	git push origin refs/tags/$(tag)
