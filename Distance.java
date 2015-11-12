import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Distance {
    
    /* Si un 3e argument est specifie sur la ligne de commande, alors
     * cette variable est mise a true. Dans ce cas, les distances
     * calculees pour les differentes chaines *ne sont pas emises* sur
     * le flux de sortie.  C'est plutot le temps d'execution qui est
     * emis en sortie, et ce pour l'ensemble de l'execution du
     * programme (donc pour l'ensemble des chaines du flux d'entree).
     */
    private static boolean benchmarks = false;


    /* 
     * Methode pour lire la prochaine chaine dans le format specifique
     * utilise pour le devoir.  
     *
     * Les diverses chaines sont obtenues du flux d'entree standard.
     *
     * Une chaine peut etre sur plusieurs lignes. La chaine se termine
     * lorsqu'on rencontre une ligne contenant uniquement '%%%'.  
     *
     * Le flux d'entree, pour etre valide, doit avoir un nombre pair
     * de chaines.  De plus, la derniere chaine doit elle aussi etre
     * suivie par '%%%'.
     */
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
    public static String lireChaine() {
        StringBuffer resultat = new StringBuffer();
        try{
            String ligneLue = br.readLine();
            if ( ligneLue == null ) {
                return null;
            }
            while( ! ligneLue.equals( "%%%" ) ) {
                resultat.append( ligneLue );
                ligneLue = br.readLine();
            }
        } catch(IOException io) {
            io.printStackTrace();
            System.exit(-1);
        }
        return resultat.toString();
    }

    
    
    /*
     * Test l'egalite de deux caracteres
     * Retourne 0 si les deux caracteres sont egaux, 1 sinon.
     */
    public static int egalite(char c1, char c2)
    {
        if (c1 == c2) return 0;
        return 1;
    }

    
    //-------------------------------------------
    // Version sequentielle
    //-------------------------------------------

    public static int distanceSeq( String chaine1, String chaine2 ) {
        
        int size1 = chaine1.length();
        int size2 = chaine2.length();
        int matrice[][] = new int[size1+1][size2+1];
        
        // cas de base
        matrice[0][0] = 0;
        for (int i = 1 ; i <= size1 ; ++i)
            matrice[i][0] = i;
        for (int j = 1 ; j <= size2 ; ++j)
            matrice[0][j] = j;
        
        // cas recursif
        for (int i = 1 ; i <= size1 ; ++i)
        {
            for (int j = 1 ; j <= size2 ; ++j)
            {
                matrice[i][j] = Math.min (matrice[i-1][j] + 1, Math.min (
                        matrice[i][j-1] + 1,
                        matrice[i-1][j-1] + egalite(chaine1.charAt(i-1), chaine2.charAt(j-1))));           
            }
        }
        return matrice[size1][size2];
    }
    
    public static void methodeSeq() {
        String chaine1, chaine2;
        
        while ( (chaine1 = lireChaine()) != null ) {
            chaine2 = lireChaine();
            int distance = distanceSeq(chaine1, chaine2);
            if ( !benchmarks ) {
                System.out.println( distance );
            }
        }
    }

    //-------------------------------------------
    // Premiere version parallele.
    //-------------------------------------------

    public static int distancePar1( String chaine1, String chaine2 ) {
        return 0;
    }
    
    public static void methodePar1( int nbThreads ) {
    }
    
    //-------------------------------------------
    // Deuxieme version parallele.
    //-------------------------------------------

    public static int distancePar2( String chaine1, String chaine2 ) {
        return 0;
    }

    public static void methodePar2( int nbThreads ) {
    }
    
    
    ///////////////////////////////////////////////////////////////////      
    // PROGRAMME PRINCIPAL.
    ///////////////////////////////////////////////////////////////////    
    
    public static void main( String[] args ) {
	// On obtient les arguments de la ligne de commande et on les verifie.
	if (args.length < 2) {
	    System.out.println( "Usage:" );
	    System.out.println( "  java Distance numMethode nbThreads [benchmarks?]" );
	    System.exit(-1);
	}
	int numMethode = Integer.parseInt( args[0] );
	int nbThreads = Integer.parseInt( args[1] );

        if ( numMethode < 0 || numMethode > 2 ) {
            System.out.println( "*** Le 1er argument doit etre 0, 1 ou 2" );
            System.exit(-1);
        }

        if ( nbThreads <= 0 ) {
            System.out.println( "*** Le 2e argument doit etre >= 1" );
            System.exit(-1);
        }


        // Variable pour mesures du temps d'execution...si necessaire.
        benchmarks = args.length == 3;
        long tempsDebut = benchmarks ? System.currentTimeMillis() : 0;

        // On appelle la bonne methode (dispatcher)
        if ( numMethode == 0 ) {
            methodeSeq();
        } else if ( numMethode == 1 ) {
            methodePar1( nbThreads );
        } else {
            methodePar2( nbThreads );
        }

        if ( benchmarks ) {
            long tempsFin = System.currentTimeMillis();
            // On emet le temps en secondes.
            System.out.println( (tempsFin - tempsDebut) / 1000.0 );
        }
    }
}
