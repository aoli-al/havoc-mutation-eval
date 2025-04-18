original_dir=$(pwd)

cd BeDivFuzz
mvn -DskipTests install

cd "$original_dir"

cd JQF-ei
mvn -DskipTests install

cd "$original_dir"
mvn -DskipTests install


