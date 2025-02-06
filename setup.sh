original_dir=$(pwd)

cd ./zeugma-evaluation/zeugma-evaluation-tools/BeDivFuzz
mvn -DskipTests install

cd "$original_dir"

cd ./zeugma-evaluation/zeugma-evaluation-tools/JQF-ei
mvn -DskipTests install

cd "$original_dir"
mvn -DskipTests install


