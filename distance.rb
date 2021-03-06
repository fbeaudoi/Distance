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
  (1..n1).each { |i| d[i, 0] = i }
  (1..n2).each { |j| d[0, j] = j }

  # Cas recursifs.
  ((1..n1)*(1..n2)).each do |i, j|
    d[i, j] = [ d[i-1, j] + 1,
                d[i, j-1] + 1,
                d[i-1, j-1] + cout_subst( ch1[i-1], ch2[j-1] )
              ].min
  end

  d[n1, n2]
end

def get_chaine( chaines, k )
  ch = ""
  loop do
    break if chaines[k] =~ /^\%\%\%$/
    ch << chaines[k]
    k += 1
  end
  [ch, k+1]
end

if $0 == __FILE__
  DBC.require( ARGV.size == 2,
               "*** Il doit y avoir deux arguments" )
  DBC.require( (0..2).include?(ARGV[0].to_i),
               "*** Le premier argument doit etre 0, 1 ou 2" )
  DBC.require( ARGV[1].to_i != 0,
               "*** Le deuxieme argument doit etre un entier positif" )

  chaines = STDIN.readlines
  k = 0
  loop do
    ch1, k = get_chaine( chaines, k )
    ch2, k = get_chaine( chaines, k )
    puts distance_seq ch1, ch2
    break if k == chaines.size
  end
end
