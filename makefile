###############################################################
#
# Constantes a completer pour la remise de votre travail:
#  - CODES_PERMANENTS
#  - FICHIERS_A_REMETTRE
#
###############################################################

### Vous devez completer l'une ou l'autre des definitions.   ###

# Deux etudiants:
# Si vous etes deux etudiants: Indiquer vos codes permanents.
CODES_PERMANENTS='ABCD01020304,GHIJ11121314'


# Un etudiant:
# Si vous etes seul: Supprimer le diese en debut de ligne et
# indiquer votre code permanent (sans changer le nom de la variable).
#CODES_PERMANENTS='ABCD01020304'

#--------------------------------------------------------

### Vous devez aussi modifier cette variable si vous avez des fichiers
### autres que des fichiers Java que vous voulez remettrre.

FICHIERS_A_REMETTRE=*.java

########################################################################
########################################################################

.IGNORE:


# Parametres pour "make run".
NUM_TEST=1
NUM_METHODE=0
NB_THREADS=4


RUNNER=java
PROGRAMME=Distance
DONNEES=test_chaines$(NUM_TEST).donnees
RUN=run_$(RUNNER)
UN_BM=bm_$(RUNNER)



default: compile
#default: $(RUN)
#default: $(UN_BM)
#default: tests
#default: benchmarks
#default: graphiques



#
# Cible pour la compilation: A modifier si necessaire.
#
$(PROGRAMME).class: $(PROGRAMME).java
	@javac $(PROGRAMME).java

compile compile_java: $(PROGRAMME).java
	@javac $(PROGRAMME).java

# 
# Cibles pour l'execution.
#
run: run_java

run_%: compile_%
	cat Tests/$(DONNEES) | $(RUNNER) $(PROGRAMME) $(NUM_METHODE) $(NB_THREADS)

bm_%: compile_%
	cat Tests/$(DONNEES) | $(RUNNER) $(PROGRAMME) $(NUM_METHODE) $(NB_THREADS) 1


##################################
# Cibles pour les tests.
##################################

tests: tests_chaines tests_programmes

tests_chaines: test_chaines1 test_chaines2 test_chaines3 test_chaines4

tests_programmes: programme1 programme2


test_chaines%: compile_$(RUNNER)
	@echo ""
	@echo "$@: methode 0"
	@echo "----------------"
	@cat Tests/$@.donnees | $(RUNNER) $(PROGRAMME) 0 $(NB_THREADS) > obtenu.txt
	@diff Tests/$@.resultats obtenu.txt
	@echo ""
	@echo "$@: methode 1"
	@echo "----------------"
	@cat Tests/$@.donnees | $(RUNNER) $(PROGRAMME) 1 $(NB_THREADS) > obtenu.txt
	@diff Tests/$@.resultats obtenu.txt
	@echo ""
	@echo "$@: methode 2"
	@echo "----------------"
	@cat Tests/$@.donnees | $(RUNNER) $(PROGRAMME) 2 $(NB_THREADS) > obtenu.txt
	@diff Tests/$@.resultats obtenu.txt
	@echo "================================"


programme%:
	@rm -f jetons.txt
	@cat Tests/programme0.rb | ./generer_jetons_ruby.rb > jetons.txt
	@echo "%%%" >> jetons.txt
	@cat Tests/$@.rb | ./generer_jetons_ruby.rb >> jetons.txt
	@echo "%%%" >> jetons.txt
	cat jetons.txt | $(RUNNER) $(PROGRAMME) $(NUM_METHODE) $(NB_THREADS) > obtenu.txt
	@diff Tests/$@.resultats obtenu.txt
	@rm -f jetons.txt obtenu.txt

##################################
# Cibles pour les benchmarks et la generation des graphiques.
##################################

bm benchmarks: benchmark_1 benchmark_2 benchmark_3

benchmark_%: compile_java
	./benchmarks.rb $@ > temps-$@.txt


graphiques: graphique_1 graphique_2 graphique_3

temps-benchmark_1.txt: $(PROGRAMME).class
	make benchmark_1
temps-benchmark_2.txt: $(PROGRAMME).class
	make benchmark_2
temps-benchmark_3.txt: $(PROGRAMME).class
	make benchmark_3

#
# CONSTANTES A MODIFIER pour ajuster/cadrer correctement vos
# graphiques
#

MAX_TEMPS1=2
MAX_ACC1=70
graphique_1: temps-benchmark_1.txt
	./plot-temps.sh benchmark_1 $(MAX_TEMPS1)
	./plot-acc.sh benchmark_1 $(MAX_ACC1)

MAX_TEMPS2=2
MAX_ACC2=70
graphique_2: temps-benchmark_2.txt
	./plot-temps.sh benchmark_2 $(MAX_TEMPS2)
	./plot-acc.sh benchmark_2 $(MAX_ACC2)

MAX_TEMPS3=2
MAX_ACC3=70
graphique_3: temps-benchmark_3.txt
	./plot-temps.sh benchmark_3 $(MAX_TEMPS3)
	./plot-acc.sh benchmark_3 $(MAX_ACC3)


##################################
# Nettoyage.
##################################
clean:
	rm -f *.class
	rm -f *.aux *.dvi *.ps *.log *.bbl *.blg *.pdf *.out
	rm -f *~ *.bak
	rm -f jetons*txt
	rm -f obtenu.txt
	rm -f res_benchmark*txt
	rm -f temps-bench*txt

########################################################################
########################################################################

BOITE=INF5171
remise:
	PWD=$(shell pwd)
	ssh oto.labunix.uqam.ca oto rendre_tp tremblay_gu $(BOITE) $(CODES_PERMANENTS) $(PWD)/$(FICHIERS_A_REMETTRE)
	ssh oto.labunix.uqam.ca oto confirmer_remise tremblay_gu $(BOITE) $(CODES_PERMANENTS)

########################################################################
########################################################################

