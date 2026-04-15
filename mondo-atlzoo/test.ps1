# Base directory (all metamodels)
$basePath = "..\Evaluation\dataset\metamodels\"

# Get all .ecore files in the folder
$ecoreFiles = Get-ChildItem -Path $basePath -Filter *.ecore

# Custom size list
$sizes = @(
    100
)

foreach ($file in $ecoreFiles) {

    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)

    foreach ($size in $sizes) {
        Write-Host "Running $name with size=$size"

        java -jar instantiator-fatjar.jar `
            -m $file.FullName `
            -n 1 `
            -s $size `
            -g
    }
}