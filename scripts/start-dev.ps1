$serverPort = 2360
$apiBaseUrl = "http://localhost:$serverPort/api"

Start-Process cmd.exe -ArgumentList '/k',"cd /d C:\Coding\260324_Dormitory-Repair-Management\server && mvn -Dspring-boot.run.arguments=--server.port=$serverPort spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit','-Command',"$env:VITE_API_BASE_URL='$apiBaseUrl'; cd C:\Coding\260324_Dormitory-Repair-Management\admin-web; npm run dev"
