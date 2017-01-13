Program slúži na konverziu rôznych formátov evolučných histórií na nový formát.
Tento nový formát slúži ako vstup pre program EHDraw, ktorý vizualizuje danú evolučnú históriu, určenú týmto formátom.
Program zatiaľ dokáže skonvertovať dva formáty:
- formát PIVO(Phylogeny by IteratiVe Optimization)
- formát pre program na duplikácie(DUP)
Priečinok obsahuje okrem zdrojových súborov aj ukážkové súbory pre formáty PIVO a DUP.

Ovládanie - cez príkazový riadok:
1. dostaňte sa v príkazovom riadku do priečinku so zdrojovými súbormi
2. použite príkaz [javac *.java] na kompiláciu
3. a) príkaz [java Main pivo candida_tree.txt candida_reconstruction.txt] vytvorí súbor candida_output.history obsahujúci nový formát
   b) príkaz [java Main dup def-200-1e5-00.tree def-200-1e5-00.atoms def-200-1e5-00.history] vytvorí výstupný súbor def-200-1e5-00-output.history