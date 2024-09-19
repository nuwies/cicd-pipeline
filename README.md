# Assignment 1 for COMP 3940

## Setup

1. Download [tomcat 10](https://tomcat.apache.org/download-10.cgi).

2. Unzip tomcat to a known location.

3. For ease of use, rename the unzipped tomcat to **tomcat**.

4. In the `~/tomcat/webapps` directory clone this repository.

5. In the `WEB-INF` directory, add a file named `db.properties`

   - Inside the `db.properties` file, add the following:

   ```
       db.url=your_database_url
       db.username=your_username
       db.password=your_password
   ```

6. Compile all '.java' files in the '/classes' directory using `javac -cp ".;path/to/tomcat/lib/servlet-api.jar;path/to/mysql-connector-j-x.x.x.jar;path/to/jbcrypt.jar" *.java`

7. Open the `~/tomcat/bin directory` in **CMD** or **Terminal** and run the command: `startup.bat` for Windows and `startup.sh` for Mac.

## Contributors

- Luke Chung
- Sarah Liu
