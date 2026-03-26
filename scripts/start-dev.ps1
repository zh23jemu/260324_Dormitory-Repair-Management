Start-Process powershell -ArgumentList '-NoExit','-Command','cd C:\Coding\260324_Dormitory-Repair-Management\server; mvn spring-boot:run'
Start-Process powershell -ArgumentList '-NoExit','-Command','cd C:\Coding\260324_Dormitory-Repair-Management\admin-web; npm run dev'
Start-Process powershell -ArgumentList '-NoExit','-Command','cd C:\Coding\260324_Dormitory-Repair-Management\student-web; npm run dev'
