n=1
folder=B
testfile="../full/"$folder"/testfile"
inputfile="../full/"$folder"/input"
examplefile="../full/"$folder"/output"
outputfile="./tmp/output/output"

echo "" >./tmp/error.log
echo "" >./tmp/diff.log
rm -r tmp
mkdir tmp
mkdir tmp/output

while [ $n -le 30 ]; do
  echo $n " begins..."
  cat $testfile$n".txt" >./testfile.txt
  java Compiler
  cat ./llvm_ir.txt >./tmp/llvm_ir.ll
  llvm-link ./tmp/llvm_ir.ll ./lib.ll -S -o ./tmp/out.ll
  echo "testfile" $n "error:" >>./tmp/error.log
  cat $inputfile$n".txt" | lli ./tmp/out.ll >$outputfile$n".txt" 2>>./tmp/error.log
  echo "testfile" $n "diff:" >>./tmp/diff.log
  diff -c $examplefile$n".txt" $outputfile$n".txt" >>./tmp/diff.log
  echo $n "ends..."
  ((n++))
done
