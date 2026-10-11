[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$JarPath,
    [Parameter(Mandatory)][ValidateSet('1.20.1', '1.21.1', '26.1.2')][string]$MinecraftVersion,
    [Parameter(Mandatory)][ValidatePattern('^[a-fA-F0-9]{64}$')][string]$ExpectedSha256,
    [int]$ProjectId = 1727925,
    [string]$ModVersion = '1.2.2',
    [ValidateSet('release', 'beta', 'alpha')][string]$ReleaseType = 'beta',
    [string]$ResultPath,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$filePath = (Resolve-Path -LiteralPath $JarPath).Path
$actualHash = (Get-FileHash -LiteralPath $filePath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualHash -ne $ExpectedSha256.ToLowerInvariant()) {
    throw 'The JAR hash differs from the validated artifact.'
}
if ([IO.Path]::GetFileName($filePath) -ne "wishing_fountain-$MinecraftVersion-fabric-$ModVersion.jar") {
    throw 'The selected file does not match the requested Minecraft and mod versions.'
}
if ($ResultPath -and (Test-Path -LiteralPath $ResultPath)) {
    $previous = Get-Content -LiteralPath $ResultPath -Raw | ConvertFrom-Json
    if ($previous.id -and $previous.sha256 -eq $actualHash -and $previous.projectId -eq $ProjectId) {
        Write-Output "Already uploaded: Minecraft $MinecraftVersion, file ID $($previous.id)"
        exit 0
    }
    throw 'An upload receipt exists for a different artifact. Choose another ResultPath.'
}

$gameVersionIds = @{ '1.20.1' = 9990; '1.21.1' = 11779; '26.1.2' = 16082 }
$metadata = [ordered]@{
    changelog = @"
## Wishing Fountain $ModVersion - Fabric $MinecraftVersion

Initial Fabric port. Preserves the fountain multiblock, 44 built-in wishing recipes, biome and structure maps, item and weather wishes, three appearances, advancements, and boss drops.

Includes Fabric registration, loot callbacks, properties configuration, production mappings for Minecraft 1.20.1/1.21.1, and version-specific rendering and item resources. Minecraft 26.1.2 also synchronizes custom recipes on join and data pack reload and refreshes JEI when they arrive.

Requires **Fabric API** on the client and server. **JEI** is optional for viewing wishing recipes.

Validation: compiled and packaged with Gradle; checked JAR metadata, entrypoints, mixins, Java compatibility, production mappings, recipes, models, textures, and structure resources. Minecraft was not launched for these checks.

Original mod by poisonsheep and shinobunoinu, used under the MIT license. Fabric port by Lingmu.
"@
    changelogType = 'markdown'
    displayName = "Wishing Fountain $ModVersion - Fabric $MinecraftVersion"
    gameVersions = @($gameVersionIds[$MinecraftVersion], 7499, 9638, 9639)
    releaseType = $ReleaseType
    isMarkedForManualRelease = $false
    relations = @{
        projects = @(
            @{ slug = 'fabric-api'; projectID = 306612; type = 'requiredDependency' }
            @{ slug = 'jei'; projectID = 238222; type = 'optionalDependency' }
        )
    }
}

if ($DryRun) {
    Write-Output ($metadata | ConvertTo-Json -Depth 8)
    Write-Output "Validated artifact: $filePath (SHA256 $actualHash)"
    exit 0
}

$uploadToken = [Environment]::GetEnvironmentVariable('CURSEFORGE_UPLOAD_TOKEN', 'User')
if ([string]::IsNullOrWhiteSpace($uploadToken)) { $uploadToken = $env:CURSEFORGE_UPLOAD_TOKEN }
if ([string]::IsNullOrWhiteSpace($uploadToken)) { throw 'CURSEFORGE_UPLOAD_TOKEN is not available.' }

Add-Type -AssemblyName System.Net.Http
$client = [Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(120)
$client.DefaultRequestHeaders.TryAddWithoutValidation('X-Api-Token', $uploadToken) | Out-Null
$uploadToken = $null
$multipart = [Net.Http.MultipartFormDataContent]::new()
$stream = [IO.File]::OpenRead($filePath)
$response = $null
try {
    $metadataContent = [Net.Http.StringContent]::new(($metadata | ConvertTo-Json -Depth 8 -Compress), [Text.Encoding]::UTF8, 'application/json')
    $multipart.Add($metadataContent, 'metadata')
    $fileContent = [Net.Http.StreamContent]::new($stream)
    $fileContent.Headers.ContentType = [Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/java-archive')
    $multipart.Add($fileContent, 'file', [IO.Path]::GetFileName($filePath))
    $response = $client.PostAsync("https://minecraft.curseforge.com/api/projects/$ProjectId/upload-file", $multipart).GetAwaiter().GetResult()
    if (-not $response.IsSuccessStatusCode) { throw "CurseForge upload failed: HTTP $([int]$response.StatusCode)" }
    $result = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
    if (-not $result.id) { throw 'CurseForge did not return a file ID.' }
    $receipt = [ordered]@{
        projectId = $ProjectId
        id = $result.id
        minecraft = $MinecraftVersion
        loader = 'Fabric'
        version = $ModVersion
        releaseType = $ReleaseType
        file = [IO.Path]::GetFileName($filePath)
        sha256 = $actualHash
        url = "https://www.curseforge.com/minecraft/mc-mods/wishing-fountain-port/files/$($result.id)"
    }
    if ($ResultPath) {
        [IO.File]::WriteAllText([IO.Path]::GetFullPath($ResultPath), ($receipt | ConvertTo-Json -Depth 5), [Text.UTF8Encoding]::new($false))
    }
    Write-Output ($receipt | ConvertTo-Json -Depth 5)
} finally {
    if ($null -ne $response) { $response.Dispose() }
    $multipart.Dispose()
    $stream.Dispose()
    $client.Dispose()
}
