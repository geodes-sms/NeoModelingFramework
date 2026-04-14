param(
    [Parameter(Mandatory=$true)]
    [string]$name
)

# Build full path
$ecoreFile = "..\Evaluation\dataset\metamodels\$name.ecore"

# Custom size list
$sizes = @(
    10, 50,
    100, 500,
    1000, 5000,
    10000, 50000,
    100000, 500000,
    1000000
)

foreach ($size in $sizes) {
    Write-Host "Running $name with size=$size"

    java -jar instantiator-fatjar.jar `
        -m $ecoreFile `
        -n 1 `
        -s $size `
        -g
}