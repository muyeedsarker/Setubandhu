$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root
if (Test-Path "release-key.jks") { throw "release-key.jks already exists. Refusing to overwrite it." }
$pass = Read-Host "New keystore password" -AsSecureString
$pass2 = Read-Host "Confirm keystore password" -AsSecureString
$b1=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($pass)
$b2=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($pass2)
try {
  $p1=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($b1)
  $p2=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($b2)
  if ($p1 -ne $p2) { throw "Passwords do not match." }
  & keytool -genkeypair -v -keystore release-key.jks -alias setubandhu -keyalg RSA -keysize 2048 -validity 10000 -storepass $p1 -keypass $p1 -dname "CN=Setubandhu, OU=Mobile, O=Setubandhu, L=Gazipur, ST=Dhaka, C=BD"
  @"
storeFile=release-key.jks
storePassword=$p1
keyAlias=setubandhu
keyPassword=$p1
"@ | Set-Content -NoNewline keystore.properties
  Write-Host "Created release-key.jks and keystore.properties. Keep both private and back them up securely."
} finally {
  [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b1)
  [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b2)
}
