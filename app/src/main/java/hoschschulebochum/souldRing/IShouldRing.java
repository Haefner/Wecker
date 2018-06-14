package hoschschulebochum.souldRing;

interface IShouldRing {

    /**
     * Die Person kann Orte angeben, an denen der Wecker klingeln darf.
     * @return true = an diesen Ort darf der Wecker klingeln
     */
    public boolean doesLocationFit();

    /**
     * Gibt an, ob eine Person aufgestanden ist und sich bewegt
     * @return true = Person hat angefangen sich zu bewegen
     */
    public boolean doesPersonStartToMove();

    /**
     * Gibt wieder ob die Umgebung heller geworden ist.
     */
    public boolean doesLigthGetOn();


}
