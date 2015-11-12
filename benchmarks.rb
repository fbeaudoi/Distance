#!/usr/bin/env ruby

# Fichier de donnees de benchmark a utiliser.
DONNEES=ARGV[0]

# Combien de fois on execute pour calculer le temps moyen.
NB_FOIS=3

# Les diverses nombres de threads a utiliser.
NB_THREADS = [1, 2, 4, 8, 16, 32, 64]

# Les deux methodes a benchmarker.
METHODES=[1, 2]


# Methode pour calcul du temps moyen.
def temps_moyen( donnees, methode, nb_threads, nb_fois )
  temps = 0.0
  nb_fois.times do
    res =  `cat Benchmarks/#{donnees}.donnees | java Distance #{methode} #{nb_threads} 0`
    temps += res.chomp.to_f
  end
  temps = temps / nb_fois
end


# On imprime l'information sur le benchmark utilise.
puts "# BENCHMARKS = #{DONNEES}"

# On imprime les en-tetes de colonnes.
noms_methodes = METHODES.map{ |m| "methodePar#{m}" }
largeur = noms_methodes.map(&:size).max + 2
print "# nb.th."
["methodeSeq", *noms_methodes].each do |x|
  print x.rjust(largeur)
end
puts

# On mesure le temps sequentiel.
temps_seq = temps_moyen DONNEES, 0, 1, NB_FOIS

# On mesure pour les diverses methodes paralleles avec divers nombres
# de threads.
NB_THREADS.each do |nb_threads|
  print "%8d" % nb_threads
  print "%#{largeur}.3f" % temps_seq

  temps_par1 =  temps_moyen DONNEES, 1, nb_threads, NB_FOIS
  print "%#{largeur}.3f" % temps_par1

  temps_par2 =  temps_moyen DONNEES, 2, nb_threads, NB_FOIS
  print "%#{largeur}.3f" % temps_par2

  puts
end
