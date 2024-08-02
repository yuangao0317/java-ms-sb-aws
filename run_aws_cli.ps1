# Load environment variables from .env file
$envFile = ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^(.*?)=(.*)$") {
            [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
}
else {
    Write-Host ".env file not found!"
    exit 1
}

# Run Docker with AWS CLI
docker run --rm `
    --env-file $envFile `
    --hostname $env:HOSTNAME `
    --env "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" `
    --network "bridge" `
    --workdir "/aws" `
    --restart "no" `
    --runtime "runc" `
    "amazon/aws-cli:2.9.22" `
    "$args"
