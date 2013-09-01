set term post eps enh "Arial" 30 color
#set key bmargin horizontal left Right box
set key inside bottom right Right
set output "combine.eps"
set xtic 1
set ytic auto
set boxwidth 0.2 absolute
set xrange [ 0.00000 : 7.0000 ] noreverse nowriteback
set yrange [ 0.00000 : 1.0000 ] noreverse nowriteback
set ylabel "Workload-Processing Ratio"
#set boxwidth 0.9
set style line 1 lt 1 lc rgb "red" lw 3
set style line 2 lt 2 lc rgb "blue" lw 3
set style line 3 lt 3 lc rgb "black" lw 1

plot 'Di.dat' using 1:3:2:6:5 with candlesticks ls 1 title "C/R with Formula (3)" whiskerbars, ''  using 1:4:4:4:4 with candlesticks lt -1 lw 2 notitle, 'Young.dat' using 1:3:2:6:5 with candlesticks ls 2 lw 2 title "C/R with Young's Formula" whiskerbars, ''  using 1:4:4:4:4 with candlesticks lt -1 lw 2 notitle