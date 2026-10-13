$recipeDir = 'D:\mod\1.20.1\forgeborneodyssey\src\main\resources\data\minecraft\recipes'

$conditionBlock = @'
  "conditions": [
    {
      "type": "forge:not",
      "value": {
        "type": "forge:mod_loaded",
        "modid": "minecraft"
      }
    }
  ],
'@

$count = 0
Get-ChildItem $recipeDir -Filter *.json | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    if ($content -match '"conditions"') {
        Write-Host "SKIP (already has conditions): $($_.Name)"
        return
    }
    $newContent = $content -replace "(\{\r?\n)(  ""type"")", "`$1$conditionBlock`r`n`$2"
    [System.IO.File]::WriteAllText($_.FullName, $newContent, [System.Text.UTF8Encoding]::new($false))
    $count++
    Write-Host "OK: $($_.Name)"
}

Write-Host "Done. Modified $count files."