$serverPort = 2360
$apiBaseUrl = "http://localhost:$serverPort/api"

Start-Process powershell -ArgumentList '-NoExit','-Command',"cd C:\Coding\260324_Dormitory-Repair-Management\server; mvn spring-boot:run ""-Dspring-boot.run.arguments=--server.port=$serverPort"""
Start-Process powershell -ArgumentList '-NoExit','-Command',"$env:VITE_API_BASE_URL='$apiBaseUrl'; cd C:\Coding\260324_Dormitory-Repair-Management\admin-web; npm run dev"
Start-Process powershell -ArgumentList '-NoExit','-Command',"$env:VITE_API_BASE_URL='$apiBaseUrl'; cd C:\Coding\260324_Dormitory-Repair-Management\student-web; npm run dev"
