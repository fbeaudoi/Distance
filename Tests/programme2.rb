require 'pruby'
require 'matrice'

def distance_seq( chaine1, chaine2 )
  taille1, taille2 = chaine1.size, chaine2.size

  d = Matrice.new( taille1+1, taille2+1 )

  # Les cas simple, avec cout = 1
  (0..taille1).each { |i| d[i, 0] = i }
  (0..taille2).each { |j| d[0, j] = j }

  # Les cas complexes, donc recursifs.
  ((1..taille1)*(1..taille2)).each do |i, j|
    cout_ins = d[i-1, j] + 1
    cout_sup = d[i, j-1] + 1
    cout_sub = d[i-1, j-1] + (chaine1[i] == chaine2[j] ? 0 : 1)
    d[i, j] = [cout_ins, cout_sup, cout_sub].min
  end

  return d[taille1, taille2]
end
