﻿Program slúži na konverziu rôznych formátov evolučných histórií na nový formát.
Tento nový formát slúži ako vstup pre program EHDraw, ktorý vizualizuje danú evolučnú históriu, určenú týmto formátom.
Program dokáže skonvertovať dva formáty:
- formát PIVO(Phylogeny by IteratiVe Optimization)
- formát programu na duplikácie (DUP) (def-200-1e5-00 = vygenerovaná história || amy = predpovedaná programom na reálnom úseku ľudského genómu)
Priečinok obsahuje okrem zdrojových súborov aj ukážkové súbory pre formáty PIVO a DUP.

Ovládanie - cez príkazový riadok:
1. dostaňte sa v príkazovom riadku do priečinku so zdrojovými súbormi
2. použite príkaz [javac *.java] na kompiláciu
3. a) príkaz [java EHConverter pivo candida_tree.txt candida_reconstruction.txt] vytvorí súbor candida_output.history obsahujúci históriu v novom formáte
   b) príkaz [java EHConverter dup-gen def-200-1e5-00.tree def-200-1e5-00.atoms def-200-1e5-00.history] vytvorí výstupný súbor def-200-1e5-00-output.history
   c) príkaz [java EHConverter dup-real amy.tree amy.atoms amy.history 0.02] vytvorí výstupný súbor amy-output.history, kde sa prvá speciácia odohrala v čase 0.02