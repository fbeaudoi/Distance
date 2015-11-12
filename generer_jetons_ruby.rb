#!/usr/bin/env ruby

#
# Les mots-cles de Ruby, a tout le moins un petit sous-ensemble de
# ceux utilises dans les deux petits programmes.
#
mots_cles = %w{ require pruby def end
                size new each do min return
              }

ponctuation = %w{ . , ; ( ) [ ] }

#
# Pour determiner le prochain numero d'identificateur
#

$next_id = 0
def next_id
  $next_id += 1
end

# Le hash des identificateurs, pour connaitre leur numero
ids = Hash.new

STDIN.readlines.each do |ligne|
  if pos = (ligne =~ /#.*$/)
    # On supprime les commentaires
    ligne = ligne[0..pos-1]
  end

  # On traite chacun des mots de la ligne.
  mots = ligne.scan /\w+|\d+|\s+|\.|\,|\+|\-|\*|\(|\)|\[|\]|=/
  mots = mots.reject { |m| m == "" || m =~ /\s+/  || ponctuation.include?(m) }
  mots.each do |mot|
    if mot =~ /'.*'|".*"/
      # C'est une chaine: on ignore son contenu.
      puts "STRING"
    elsif mot =~ /^[^\d]+\w+$/
      # C'est un identificateur (pas un nombre!).
      if mots_cles.include? mot
        # C'est un mot-cle: on l'emet tel quel.
        puts mot
      else
        # C'est identificateur: on emet son numero unique
        id = ids[mot] || next_id
        puts "ID#{id}"
      end
    else
      puts mot
    end
  end
end
