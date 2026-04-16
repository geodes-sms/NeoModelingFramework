$models = @(
    'erlang', 'LLVM_IR', 'models', 'timing-diagram', 'IFML', 'esb', 'NetModel', 'Simulator', 'reviews', 'hockeyleague', 'FlatQVT', 'JDTAST', 'java5', 'SqliteModel', 'MDD',  'genericity_dsl', 'StaticScript', 'plsql', 'AthenaDSL', 'Glsl', 'FractalItf', 'CoreDsl', 'AtsDsl', 'OseeDsl','OCL', 'JavaVMTypes', 'Iptables', 'Transformation', 'business', 'ApplauseDsl'

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