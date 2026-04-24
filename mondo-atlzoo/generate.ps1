$models = @(
   'erlang', 'LLVM_IR', 'FlatQVT', 'timing-diagram', 'business', 'SqliteModel', 'widget', 'esb', 'NetModel', 'RobotTask', 'Iptables', 'reviews', 'hockeyleague', 'java5', 'MDD', 'StaticScript', 'AthenaDSL', 'Glsl', 'FractalItf', 'CoreDsl', 'AtsDsl', 'OseeDsl', 'JavaVMTypes', 'Transformation', 'ApplauseDsl', 'modelica', 'DialogScript', 'EventOrientedLanguage', 'environment', 'CssExtDsl'
)

$ecoreBasePath = "..\Evaluation\dataset\metamodels"
$outputBasePath = "generated_models"

$sizes = @(
    10000, 50000,
    100000, 500000,
    1000000
)

New-Item -ItemType Directory -Force -Path $outputBasePath | Out-Null

foreach ($name in $models) {

    $ecoreFile = Join-Path $ecoreBasePath "$name.ecore"
    $modelOutDir = Join-Path $outputBasePath $name

    New-Item -ItemType Directory -Force -Path $modelOutDir | Out-Null

    if (!(Test-Path $ecoreFile)) {
        Write-Host "Skipping $name (file not found)"
        continue
    }

    foreach ($size in $sizes) {
        Write-Host "Running $name with size=$size"

        java -jar instantiator-fatjar.jar `
            -m $ecoreFile `
            -n 1 `
            -s $size `
            -g `
            -o $modelOutDir
    }
}