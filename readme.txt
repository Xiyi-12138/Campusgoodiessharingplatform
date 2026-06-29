启动后端命令
cd D:\Project\Android\Campusgoodiessharingplatform\backend\springboot
mvn package -DskipTests
java -jar target\springboot-0.0.1-SNAPSHOT.jar

如果 9090 被占用，先查并关闭旧进程：
Get-NetTCPConnection -LocalPort 9090 -ErrorAction SilentlyContinue
Stop-Process -Id <OwningProcess>

停止后端可用：
Stop-Process -Id 33640