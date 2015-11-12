#!

hostname=$(hostname)
if [ $hostname == "japet" ]; then
  echo "*** La production des graphiques avec gnuplot ne doit pas se faire sur japet"
  exit -1
fi

benchmark=$1 
max=$2
fich=temps-$benchmark.txt

gnuplot -persist <<EOF
set logscale x
set xlabel "Nombre de threads"
set ylabel "Temps d'execution (en sec)"
set title "Temps d'execution en fonction du nombre de threads pour $benchmark"
set xtics (1, 2, 4, 8, 16, 32, 64)
plot [0.9:70][0:$max] \
	 "$fich" using 1:2 title "methodeSeq" with linespoints,\
	 "$fich" using 1:3 title "methodePar1" with linespoints,\
	 "$fich" using 1:4 title "methodePar2" with linespoints
EOF

