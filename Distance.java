import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    private static int coutSubst(char c1, char c2)
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

    public static int distanceSeq(String chaine1, String chaine2)
    {
        int size1 = chaine1.length();
        int size2 = chaine2.length();
        
        int[] col1 = new int[size1 + 1];
        int[] col2 = new int[size1 + 1];
        int temp[];
        
        for (int i = 0 ; i <= size1 ; ++i)
            col1[i] = i;
        
        for (int i = 1 ; i <= size2 ; ++i)
        {
            col2[0] = i;
            
            for (int j = 1 ; j <= size1 ; ++j)
            {
                col2[j] = minimum(col2[j-1]+1, col1[j]+1, col1[j-1] + 
                        coutSubst(chaine1.charAt(j-1), chaine2.charAt(i-1)));
               
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
  
    private static int bInf (int numThread, int tailleBloc, int reste) 
    {
        int bInf = numThread * tailleBloc;
        
        if (numThread < reste)
        {
            bInf += numThread;
        } else if (reste > 0)
        {
            bInf += reste;
        }
        
        return  bInf;
    }
    
    private static int bSup (int numThread, int tailleBloc, int reste, int nbTache)
    {
        int sup = bInf(numThread+1, tailleBloc, reste);
        sup = Math.min(sup, nbTache-1);
        
        return sup;
    }
    
    private static void calculerTranche( int bInf, int bSup, int diagCourante[], int diagPrecedente[], int diagDerniere[], String chaine1, String chaine2, int x, int y, int ajustement)
    {
        for (int i = bInf ; i <= bSup; ++i)
        {
           if (x != 0 && y != 0)
           {
               
               if (ajustement > 0)
               {    // cas lorsque l'index de l'iteration en cours est plus grande que
                    // la taille de chaine1. k permet de pallier au changement de structure
                   int y1 = y - ajustement;
                   int k = y1;
                   
                   if (ajustement <= 1)
                   {
                       --k;
                   }
                   
                    diagCourante[i] = minimum (diagPrecedente[y1] +1, 
                                 diagPrecedente[y1+1] +1,
                                 diagDerniere[k+1] + coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
               } else
               {
               
                   diagCourante[i] = minimum (diagPrecedente[y-1] +1, 
                                 diagPrecedente[y] +1,
                                 diagDerniere[y-1] + coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
               }
               
           }
           --x;
           ++y;
           
        }
    }
    public static int distancePar1( String chaine1, String chaine2, int nbThreads ) {
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
        
        // chaque tableau represente une diagonale de la "matrice"
        // courante devient precedente, precedente devient derniere, derniere devient courante
        // Ainsi, les valeurs qui ne seront plus utiles sont supprimée de la "matrice"
        int diagCourante[] = new int[size1 + 1];
        int diagPrecedente[] = new int[size1 + 1];
        int diagDerniere[] = new int[size1 + 1];
        
        int nbThreadsDiag;
        int tailleBloc;
        int reste;
        
        int xDebut; //matrice[x][y] debut diagonal
        int yDebut;
        int nbTache;
        
        Thread threads[];
        
        diagDerniere[0] = 0;
        diagPrecedente[0] = 1;
        diagPrecedente[1] = 1;
        
        int nbIter = size1 + size2;
        
        for (int iter = 2 ; iter <= nbIter ; ++iter)
        {
            xDebut = Math.min ( iter, size1);
            yDebut = Math.max ( 0, iter - size1 );
            
            if (iter <= size1)
            {
                nbTache = iter +1;
                diagCourante[0] = iter;
                diagCourante[xDebut] = iter;
            } else if ( iter <= size2)
            {
                nbTache = size1 +1;
                diagCourante[xDebut] = iter;
            } else
            {
                nbTache = size1 + size2 - iter +1;
            }
            
            nbThreadsDiag = Math.min( nbTache , nbThreads);
            tailleBloc = nbTache / nbThreadsDiag;
            reste = nbTache % nbThreadsDiag;
            final int dc[] = diagCourante;
            final int dp[] = diagPrecedente;
            final int dd[] = diagDerniere;
            
            final String c1 = chaine1;
            final String c2 = chaine2;
            
            
            //permet d'ajuster l'index des tableaux une fois que iter > size1
            final int ajustement = iter - size1; 
            
            threads = new Thread[nbThreadsDiag];
            for ( int t = 0 ; t < nbThreadsDiag ; ++t )
            {
                final int bInf = bInf( t, tailleBloc, reste);
                final int bSup = bSup( t, tailleBloc, reste, nbTache);
                
                final int xd = xDebut - bInf;
                final int yd = yDebut + bInf;
                
                threads[t] = new Thread (
                    () -> calculerTranche( bInf, bSup, dc, dp, dd, c1, c2, xd, yd, ajustement)
                );
                threads[t].start();
            }
            
            for (int t = 0 ; t < nbThreadsDiag ; ++t)
            {
                try { threads[t].join(); } catch (Exception e ) {};
            }
            
            //rotation des tableaux
            diagCourante = dd;
            diagPrecedente = dc;
            diagDerniere = dp;
        }
        
        return diagPrecedente[0];
    }
    
    public static void methodePar1( int nbThreads ) {
        String chaine1, chaine2;
        
        while ( (chaine1 = lireChaine()) != null ) {
            chaine2 = lireChaine();
            int distance = distancePar1(chaine1, chaine2, nbThreads);
            if ( !benchmarks ) {
                System.out.println( distance );
            }
        }
    }

    

    
    //-------------------------------------------
    // Deuxieme version parallele.
    //-------------------------------------------
    
    private static class Travailleur implements Runnable {
        private final int bInf;
        private final int bSup;
        private final int xd;
        private final int yd;
        private final int ajustement;
        private final int diagCourante[];
        private final int diagPrecedente[];
        private final int diagDerniere[];
        private final String chaine1;
        private final String chaine2;
        
        
        Travailleur(int bInf, int bSup, int diagCourante[], int diagPrecedente[], 
                int diagDerniere[], String chaine1, String chaine2, int xd, 
                int yd, int ajustement)
        {
            this.bInf = bInf;
            this.bSup = bSup;
            this.diagCourante = diagCourante;
            this.diagPrecedente = diagPrecedente;
            this.diagDerniere = diagDerniere;
            this.chaine1 = chaine1;
            this.chaine2 = chaine2;
            this.xd = xd;
            this. yd = yd;
            this.ajustement = ajustement;
        }
        
        @Override
        public void run()
        {
            calculerTranche (bInf, bSup, diagCourante, diagPrecedente, diagDerniere, chaine1, chaine2, xd, yd, ajustement);
        }
    }
  
    
    public static int distancePar2( String chaine1, String chaine2, int nbThreads ) {
        ExecutorService threadPool;
        ArrayList<Future> futures;
        
        int size1 = chaine1.length();
        int size2 = chaine2.length();
        int nbIter = size1 + size2;
        
        if (size1 > size2)
        {
            int sizeTemp = size1;
            size1 = size2;
            size2 = sizeTemp;
            
            String chaineTemp = chaine1;
            chaine1 = chaine2;
            chaine2 = chaineTemp;
        }
        
        int diagCourante[] = new int[size1+1];
        int diagPrecedente[] = new int[size1+1];
        diagPrecedente[0] = 1;
        diagPrecedente[1] = 1;
        int diagDerniere[] = new int [size1+1];
        
        int nbTache;
        
        int xDebut;
        int yDebut;
        
        for (int iter = 2 ; iter <= nbIter ; ++iter)
        {
            xDebut = Math.min ( iter, size1);
            yDebut = Math.max ( 0, iter - size1 );
            
            if (iter <= size1)
            {
                nbTache = iter +1;
                diagCourante[0] = iter;
                diagCourante[xDebut] = iter;
            } else if ( iter <= size2)
            {
                nbTache = size1 +1;
                diagCourante[xDebut] = iter;
            } else
            {
                nbTache = size1 + size2 - iter +1;
            }
            
            int nbThreadsDiag = Math.min( nbTache , nbThreads);
            int tailleBloc = nbTache / nbThreadsDiag;
            int reste = nbTache % nbThreadsDiag;
            
            //permet d'ajuster l'index des tableaux une fois que iter > size1
            int ajustement = iter - size1; 
            int bInf;
            int bSup;
            int xd;
            int yd;
            threadPool = Executors.newFixedThreadPool(nbThreads);
            futures = new ArrayList<Future>();
            
            for (int i = 0 ; i < nbThreadsDiag ; ++i)
            {
            
                bInf = bInf( i, tailleBloc, reste);
            
                bSup = bSup( i, tailleBloc, reste, nbTache);
                
                xd = xDebut - bInf;
                yd = yDebut + bInf;
                
                Runnable travailleur = new Travailleur(bInf, bSup, diagCourante, diagPrecedente, diagDerniere, chaine1, chaine2, xd, yd, ajustement);
                futures.add(threadPool.submit(travailleur));
            }
            
            //attendre que tout les Threads aient terminé
            for (Future f: futures)
            {
                try 
                {
                    if (f.get() != null)
                    {
                        throw new RuntimeException();
                    }
                    
                } catch (InterruptedException ex) 
                {
                    throw new RuntimeException();
                } catch (ExecutionException ex) 
                {
                    throw new RuntimeException();
                }
                
            }
            threadPool.shutdown();
           
            int temp[];
            temp = diagCourante;
            diagCourante = diagDerniere;
            diagDerniere = diagPrecedente;
            diagPrecedente = temp;
        }
        
        return diagPrecedente[0];
    }

    public static void methodePar2( int nbThreads ) {
        String chaine1, chaine2;
        
        while ( (chaine1 = lireChaine()) != null ) {
            chaine2 = lireChaine();
            int distance = distancePar2(chaine1, chaine2, nbThreads);
            if ( !benchmarks ) {
                System.out.println( distance );
            }
        }
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


// une différence