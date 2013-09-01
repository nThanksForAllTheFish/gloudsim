set term post eps enh "Arial" 30 color
set output "prob.eps"
set datafile missing "-"
set key inside top left Left 

#set xtic 2
#set ytic auto
set auto x

set style line 1 lt 1 lc rgb "blue" lw 4
set style line 2 lt 2 lc rgb "red" lw 4
set style line 3 lt 3 lc rgb "green" lw 4
set style line 4 lt 4 lc rgb "purple" lw 4
set style line 5 lt 5 lc rgb "cyan" lw 4
set style line 6 lt 6 lc rgb "orange" lw 4
set style line 7 lt 7 lc rgb "black" lw 4
set style line 8 lt 8 lc rgb "brown" lw 4
set style line 9 lt 9 lc rgb "yellow" lw 4

#set border 100
set xlabel "Workload-Processing-Ratio"
set ylabel "CDF"

set yrang [0:1]
#set xrang [0:30]
#set xrang [0:0.5]
#set yrang [0:60]

set style data lines

set style fill pattern border -1
set boxwidth 0.9
#set xtic rotate by -45
plot 'cdf/log-Di_static-0.5-data.txt_4.cdf' using 1:2 ti "C/R with Formula (3)" ls 1, 'cdf/log-Young-0.5-data.txt_4.cdf' using 1:2 ti "C/R with Young's Formula" ls 2