$serverPort = 2460
$frontPort = 5183
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$serverDir = Join-Path $projectRoot "server"
$frontDir = Join-Path $projectRoot "admin-web"
$apiBaseUrl = "http://localhost:$serverPort/api"

# 使用脚本所在目录自动定位项目根目录，避免客户解压到不同路径后仍然写死本机路径。
# 后端 Maven 参数通过 cmd /c 包裹，规避 PowerShell 对 -Dspring-boot.run.arguments 的解析问题。
Start-Process cmd.exe -ArgumentList '/k',"cd /d `"$serverDir`" && mvn -Dspring-boot.run.arguments=--server.port=$serverPort spring-boot:run"

# 前端只启动统一入口 admin-web，学生端、宿管端、维修员端、管理员端都通过该入口访问。
Start-Process powershell -ArgumentList '-NoExit','-Command',"$env:VITE_API_BASE_URL='$apiBaseUrl'; cd `"$frontDir`"; npm run dev -- --host 0.0.0.0 --port $frontPort"
