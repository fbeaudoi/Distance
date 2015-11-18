import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                        matrice[i-1][j-1] + coutSubst(chaine1.charAt(i-1), chaine2.charAt(j-1))));           
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
    private static int seuil = 4;
    
  
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
           // System.out.print("bInf="+i+ " bSup="+i+ " traité : ("+x+","+y+")");
            
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
                   //System.out.print("ajustement="+ajustement+" ");
                   
                   diagCourante[i] = minimum (diagPrecedente[y1] +1, 
                                 diagPrecedente[y1+1] +1,
                                 diagDerniere[k+1] + coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
                   //System.out.println("["+diagCourante[i]+"] check");
                   if (x ==2 && y == 2)
                   {
                       /*System.out.println("(diagPrecedente[y1]  " + y1+
"\ndiagPrecedente[y1+1]  " + (y1+1)+
"\ndiagDerniere[k+1] "+(k+1)+"\n coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1))"+coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
                  */
                  // System.out.println("pre "+(diagPrecedente[y1] +1) +"\n pre "+ 
                                // (diagPrecedente[y1+1] +1) + "\ndern "+
                                // (diagDerniere[k+1])+" coutsub= " + coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
                   }
               } else
               {
               
                   diagCourante[i] = minimum (diagPrecedente[y-1] +1, 
                                 diagPrecedente[y] +1,
                                 diagDerniere[y-1] + coutSubst(chaine1.charAt(x-1), chaine2.charAt(y-1)));
               //System.out.println("["+diagCourante[i]+"] check");
               }
               
           }
          // System.out.println();
           --x;
           ++y;
           
        }
    }
    public static int distancePar1( String chaine1, String chaine2 ) {
       // if (chaine1.length() > 0) return 0;
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
        int xFin; //matrice[x][y] fin diagonal
        int yFin;
        int nbTache;
        
        Thread threads[];
        
        diagDerniere[0] = 0;
        diagPrecedente[0] = 1;
        diagPrecedente[1] = 1;
        
        for (int iter = 2 ; iter <= size1 + size2 ; ++iter)
        {
            xDebut = Math.min ( iter, size1);
            xFin = Math.max (0 , iter - size2);
            
            yDebut = Math.max ( 0, iter - size1 );
            yFin = Math.min ( iter, size2);
            
           // System.out.println("\niter="+iter+"--------------");
           // System.out.println("debut=("+xDebut+","+yDebut+") fin=("+xFin+","+yFin+")");
            
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
            //System.out.println("nbTache="+nbTache);
            
            nbThreadsDiag = Math.min( nbTache , seuil);
            tailleBloc = nbTache / nbThreadsDiag;
            reste = nbTache % nbThreadsDiag;
            final int dc[] = diagCourante;
            final int dp[] = diagPrecedente;
            final int dd[] = diagDerniere;
            
            final String c1 = chaine1;
            final String c2 = chaine2;
            System.out.println("nbThreads="+nbThreadsDiag+" tailleBloc="+tailleBloc);
            
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
            
         /* System.out.println("\n\nDiagCourant : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagCourante[i]);
        }
        
        System.out.println("!! iter= "+iter);
        
        
        System.out.println("\ndiagPrecedent : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagPrecedente[i]);
        }
        System.out.println("\ndiagDerniere : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagDerniere[i]);
        }*/
            //rotation des tableaux
            diagCourante = dd;
            diagPrecedente = dc;
            diagDerniere = dp;
            
            /*try {
                sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Distance.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
            
           
          /* System.out.println("\n\nDiagCourant : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagCourante[i]);
        }
        System.out.println("\ndiagPrecedent : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagPrecedente[i]);
        }
        System.out.println("\ndiagDerniere : ");
        for (int i = 0 ; i <= size1 ; ++i){
            System.out.print(diagDerniere[i]);
        }*/
        
        return diagPrecedente[0];
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
        /*
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
        */
        String s2 = "alpha";
        String s1 = "blphaalpha";
       
       int cost = distanceSeq_old(s1,s2);
        System.out.println("distanceSeq_old = " + cost);
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost );
        cost = distancePar1(s1,s2);
        System.out.println("distancePar1 = " + cost +"\n");
       
        
        s1 = "abc";
        s2 = "abc";
        

        cost = distanceSeq_old(s1,s2);
        System.out.println("distanceSeq_old = " + cost );
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost );
        cost = distancePar1(s1,s2);
        System.out.println("distancePar1 = " + cost +"\n");
       
        s2 = "chaton";
       s1 = "ckarolnyht";

        cost = distanceSeq_old(s1,s2);
        System.out.println("distanceSeq_old = " + cost );
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost );
        cost = distancePar1(s1,s2);
        System.out.println("distancePar1 = " + cost +"\n");
        
        s2 = "abc";
        s1 = "abcc";

       cost = distanceSeq_old(s1,s2);
        System.out.println("distanceSeq_old = " + cost );
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost );
        cost = distancePar1(s1,s2);
        System.out.println("distancePar1 = " + cost +"\n");
        
        
        s2 = "abcjfjddddkvvvvkffkkviivinvvnfvvfnfvfvfvvvklnvnklvnklfnklfvnklvmmmaaaaaaaaaaaaaaaaaaalddddddddkekffffffffffff";
        s1 = "abcceeeeeeeeeeeeeeeeeeeesssssssssssshhhhhhhhhhhhtddxxxxxxxxxxxxxbbbbbbbbbbbbbbtfg";

       cost = distanceSeq_old(s1,s2);
        System.out.println("distanceSeq_old = " + cost );
        cost = distanceSeq(s1,s2);
        System.out.println("distanceSeq = " + cost );
        cost = distancePar1(s1,s2);
        System.out.println("distancePar1 = " + cost +"\n");
        
        
        
    }
}
