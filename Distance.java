import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

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
                resultat.append( '\n' );
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
    private static int cout_subst(char c1, char c2)
    {
        if (c1 == c2) return 0;
        return 1;
    }
    
    /*
    * Retourne la valeur minimale de trois valeurs
    */
    private static int minimum(int a, int b, int c)
    {
        return Math.min(a, Math.min(b,c));
    }

    
    //-------------------------------------------
    // Version sequentielle
    //-------------------------------------------

    public static int distanceSeq_old( String chaine1, String chaine2 ) {
        
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
                matrice[i][j] = Math.min (matrice[i-1][j] + 1, Math.min (matrice[i][j-1] + 1,
                        matrice[i-1][j-1] + cout_subst(chaine1.charAt(i-1), chaine2.charAt(j-1))));           
            }
        }
        return matrice[size1][size2];
    }

    public static int distanceSeq(String chaine1, String chaine2)
    {
        int size1 = chaine1.length();
        int size2 = chaine2.length();
        
        int[] col1 = new int[size1 + 1];
        int[] col2 = new int[size1 + 1];
        int temp[] = null;
        
        for (int i = 0 ; i <= size1 ; ++i)
            col1[i] = i;
        
        for (int i = 1 ; i <= size2 ; ++i)
        {
            col2[0] = i;
            
            for (int j = 1 ; j <= size1 ; ++j)
            {
                col2[j] = minimum(col2[j-1]+1, col1[j]+1, col1[j-1] + 
                        cout_subst(chaine1.charAt(j-1), chaine2.charAt(i-1)));
            }
            
            temp = col1;
            col1 = col2;
            col2 = temp;
        }
        
        return col1[size1];  
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
    private static int seuil = 1;
    
    private static int tailleBloc(int nbThreads, int tailleDiag)
    {
        return tailleDiag / nbThreads;
    }
    
    private static int bInf(int numThread, int tailleBloc, int reste)
    {
        int inf = numThread * tailleBloc;
        
        if (numThread < reste)
        {
            inf += numThread;
        } else if (reste > 0)
        {
            inf += reste;
        }
        
        return inf+1;
        
    }
    
    private static int bSup(int numThread, int tailleBloc, int reste, int tailleDiag)
    {
        int sup = bInf(numThread+1, tailleBloc, reste);
        sup = Math.min(sup, tailleDiag);
        
        return sup;
    }
   
    private static void calculerTranche(int bInf, int bSup, int diagCourante[], int diagPrecedente[], int diagDerniere[], String chaine1, String chaine2,int x)
    {
        System.out.println ("bInf ="+bInf+" bSup="+bSup);
        for (int k = bInf ; k <= bSup ; ++k)
        {
            diagCourante[k] = minimum(diagPrecedente[k-1]+1, diagPrecedente[k]+1, diagDerniere[k-1] + 
                        cout_subst(chaine1.charAt(k-1), chaine2.charAt(x-1)));
            System.out.println("diag2[k]" +diagCourante[k]);
            
            System.out.println("chaine1.charAt(k-1)="+chaine1.charAt(k-1)+" chaine2.charAt(x-1)="+chaine2.charAt(x-1)+"  coutSubst="+cout_subst(chaine1.charAt(k-1), chaine2.charAt(x-1)));
            
            --x;
        }
    }
    
    public static int distancePar1( String chaine1, String chaine2 ) {
        int size1 = chaine1.length();
        int size2 = chaine2.length();
        
        // En s assurant que la plus petite chaine est toujours representee
        // sur le meme axe de la matrice, l algorithme est simplifiee
        if (size1 > size2)
        {
            int sizeTemp = size1;
            size1 = size2;
            size2 = sizeTemp;
            
            String chaineTemp = chaine1;
            chaine1 = chaine2;
            chaine2 = chaineTemp;
        }
        
        int diagCourante[] = new int[size1 + 1];
        int diagPrecedente[] = new int[size1 + 1];
        int diagDerniere[] = new int[size1 + 1];
        int temp[] = null;
        
        int nbThreadsDiag;
        int tailleBloc;
        int reste;
        
        Thread threads[];
        
        diagDerniere[0] = 0;
        diagPrecedente[0] = 1;
        diagPrecedente[1] = 1;
        
        for (int x = 2 ; x <3 ; ++x)
        {
            
            diagCourante[0] = x; // matrice[0][x]
            diagCourante[x] = x; // matrice[x][0]
             
            nbThreadsDiag = Math.min( x-1 , seuil);
            tailleBloc = tailleBloc(nbThreadsDiag, x-1);
            System.out.println("taillebloc="+tailleBloc+ " nbThreadsDiag="+nbThreadsDiag);
            reste = (x-1) % nbThreadsDiag;
            System.out.println("reste="+reste);
            threads = new Thread[nbThreadsDiag];
            
            final int _x = x-1;
            final String c1 = chaine1;
            final String c2 = chaine2;
            final int dc[] = diagCourante;
            final int dp[] = diagPrecedente;
            final int dd[] = diagDerniere;
            
            for (int t = 0 ; t < nbThreadsDiag ; ++t)
            {
                final int bInf = bInf( t, tailleBloc, reste );
                final int bSup = bSup( t, tailleBloc, reste, x-1);
                
                threads[t] = new Thread (
                    () -> calculerTranche( bInf, bSup, dc ,dp ,dd ,c1, c2 , _x)
                );
                threads[t].start();
            }
            
            for (int k =0 ; k < nbThreadsDiag ; ++k)
            {
                try { threads[k].join(); } catch (Exception e ) {};
            }
        }
            
            
           
           System.out.println("DiagCourant : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.println(diagCourante[i]);
        }
        System.out.println("\ndiagPrecedent : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.println(diagPrecedente[i]);
        }
        System.out.println("\ndiagDerniere : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.println(diagDerniere[i]);
       
        }
        return diagCourante[0];
    }
    
    public static void methodePar1( int nbThreads ) {
        String chaine1, chaine2;
        seuil = nbThreads;
        
        while ( (chaine1 = lireChaine()) != null ) {
            chaine2 = lireChaine();
            int distance = distancePar1(chaine1, chaine2);
            if ( !benchmarks ) {
                System.out.println( distance );
            }
        }
    }
    
    //-------------------------------------------
    // Deuxieme version parallele.
    //-------------------------------------------

    public static int distancePar2( String chaine1, String chaine2 ) {
        return 0;
    }

    public static void methodePar2( int nbThreads ) {
        String chaine1, chaine2;
        
        while ( (chaine1 = lireChaine()) != null ) {
            chaine2 = lireChaine();
            int distance = distancePar2(chaine1, chaine2);
            if ( !benchmarks ) {
                System.out.println( distance );
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////      
    // PROGRAMME PRINCIPAL.
    ///////////////////////////////////////////////////////////////////    
    
    public static void main( String[] args ) {
        /**
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
        * */
        String s1 = "alpha";
        String s2 = "blphaalpha";
       
        
        int cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost +"\n");
        
        
        s1 = "abc";
        s2 = "abc";
        

        
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost +"\n");
        
        s2 = "alpha";
        s1 = "blphaalpha";

        
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost +"\n");
        
        s2 = "abc";
        s1 = "abq";

        
        cost = distancePar1(s1,s2);
        //System.out.println("distanceSeq = " + cost +"\n");
        
        
    }
}
