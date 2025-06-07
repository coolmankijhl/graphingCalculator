set -e
javac -d bin src/*.java
java -cp bin calculator.GraphingCalculator

