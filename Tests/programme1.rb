require 'pruby'
require 'matrice'

def cout_pour_sub( c1, c2 )
  c1 == c2 ? 0 : 1
end

def distance_seq( chaine1, chaine2 )
  taille1 = chaine1.size
  taille2 = chaine2.size
  d = Matrice.new( taille1+1, taille2+1 )

  # Les cas simple, avec cout = 1
  d[0,0] = 0
  (1..taille1).each do |i|
    d[i, 0] = i
  end
  (1..taille2).each do |j|
    d[0, j] = j
  end

  # Les cas complexes, donc recursifs.
  ((1..taille1)*(1..taille2)).each do |i, j|
    d[i, j] = [ d[i-1, j] + 1,
                d[i, j-1] + 1,
                d[i-1, j-1] + cout_pour_sub( chaine1[i], chaine2[j] )
              ].min
  end

  return d[taille1, taille2]
end
