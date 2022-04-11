FROM openjdk:11
EXPOSE 8085
ADD build/libs/account-0.0.1-SNAPSHOT.jar account
ENTRYPOINT ["java", "-jar", "account"]