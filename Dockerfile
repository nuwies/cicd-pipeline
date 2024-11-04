FROM tomcat:10-jdk11-openjdk

COPY build/quizapp.war /usr/local/tomcat/webapps/
COPY WEB-INF/db.properties /usr/local/tomcat/webapps/WEB-INF/
COPY WEB-INF/lib /usr/local/tomcat/webapps/WEB-INF/lib/

EXPOSE 8080

CMD ["catalina.sh", "run"]
