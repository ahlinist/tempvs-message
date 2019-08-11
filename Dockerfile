FROM circleci/openjdk:11-jdk
COPY ./build/libs/*.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8083
CMD exec java -Xms128m -Xmx512m -jar tempvs-message.jar
