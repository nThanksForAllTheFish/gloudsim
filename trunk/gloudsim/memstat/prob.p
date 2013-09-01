set term post eps enh "Arial" 30 color
set output "prob.eps"
set datafile missing "-"
set key inside bottom right Right 

set xtic 200
#set ytic auto
set auto x

set style line 1 lt 1 lc rgb "blue" lw 5
set style line 2 lt 2 lc rgb "red" lw 5
set style line 3 lt 4 lc rgb "green" lw 5

#set border 100
set xlabel "Memory Size (MB)"
set ylabel "CDF"

set yrang [0:1]
set xrang [0:1000]

set style data lines

set style fill pattern border -1
set boxwidth 0.9
#set xtic rotate by -45
plot 'pdf/single-mem.txt_0.cdf' using 1:2 ti "ST job" ls 1, 'pdf/batch-mem.txt_0.cdf' using 1:2 ti "BoT job" ls 2, 'pdf/mix-mem.txt_0.cdf' using 1:2 ti "mixture of both" ls 3