require 'pruby'
require 'matrice'

def cout_subst( c1, c2 )
  c1 == c2 ? 0 : 1
end

def distance_seq( ch1, ch2 )
  n1 = ch1.size
  n2 = ch2.size
  d = Matrice.new( n1+1, n2+1 )

  # Cas de base (couts unitaires).
  d[0,0] = 0
  (1..n1).each do |i|
    d[i, 0] = i
  end
  (1..n2).each do |j|
    d[0, j] = j
  end

  # Cas recursifs.
  ((1..n1)*(1..n2)).each do |i, j|
    d[i, j] = [ d[i-1, j] + 1,
                d[i, j-1] + 1,
                d[i-1, j-1] + cout_subst( ch1[i], ch2[j] )
              ].min
  end

  d[n1, n2]
end
