#!/bin/bash

# --- CONFIGURATIONS ---
# JAR_FILE="java/parser/app/build/libs/app.jar"
JAR_FILE="client.jar"
INPUT_ROOT="./experiments/yamls"
OUTPUT_ROOT="./experiments/results"
CSV_DATA_ORIGINAL="$(pwd)/clouds_data/cloud_machine_types_cost_and_monthly_carbon_footprint.csv"
CSV_DATA="/home/app/clouds_data/cloud_machine_types_cost_and_monthly_carbon_footprint.csv"
URL="http://localhost:8183/milp"

echo "📍 Looking for CSV in: $CSV_DATA"
if [ ! -f "$CSV_DATA_ORIGINAL" ]; then
    echo "❌ ERROR: File does not exist!"
    exit 1
fi

echo "🚀 Initializing multi-directory test suite..."


find "$INPUT_ROOT" -name "*.yaml" | while read -r file; do

    relative_path="${file#$INPUT_ROOT/}"
    
    sub_dir=$(dirname "$relative_path")
    target_output_dir="$OUTPUT_ROOT/$sub_dir"
    mkdir -p "$target_output_dir"
    base_name=$(basename "$file" .yaml)
    output_file="$target_output_dir/res_${base_name}.json"
    
    echo "------------------------------------------------"
    echo "📂 Folder: $sub_dir"
    echo "📄 File:   $base_name"
    java -jar "$JAR_FILE" \
        "$file" \
        "$CSV_DATA" \
        --timeout 600 \
        --url "$URL" \
        -o "$output_file"

    if [ $? -eq 0 ]; then
        echo "✅ Success: Saved to $output_file"
    else
        echo "❌ Failed: $base_name"
    fi

done

echo "------------------------------------------------"
echo "🏁 Every test is concluded!"
