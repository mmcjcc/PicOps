# Build stage: compile the 2005-era sources with JDK 8, the oldest maintained
# JDK that still accepts pre-generics code (-source 1.6 is its practical floor).
# Tomcat's own lib/ supplies servlet-api/jsp-api for compilation.
FROM tomcat:9.0-jdk8-temurin AS build
WORKDIR /app
COPY lib/ lib/
COPY src/ src/
COPY web/ web/
RUN mkdir -p web/WEB-INF/classes web/WEB-INF/lib && \
    find lib -name '*.jar' -exec cp {} web/WEB-INF/lib/ \; && \
    find src/java -name '*.java' > /tmp/sources.txt && \
    javac -source 1.6 -target 1.6 -encoding ISO-8859-1 \
      -cp "$(find /usr/local/tomcat/lib web/WEB-INF/lib -name '*.jar' | paste -sd:)" \
      -d web/WEB-INF/classes @/tmp/sources.txt && \
    cp src/java/*.xml web/WEB-INF/classes/

# Runtime: Tomcat 9 is the newest Tomcat that still serves javax.* webapps
# (Tomcat 10+ is jakarta.* only).
FROM tomcat:9.0-jdk8-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/web /usr/local/tomcat/webapps/ROOT
EXPOSE 8080
CMD ["catalina.sh", "run"]
