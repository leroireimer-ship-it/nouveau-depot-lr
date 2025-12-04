import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la collection de comptes et la persistance des données (sauvegarde/chargement).
 */
public class GestionnaireBanque implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final String FICHIER_SAUVEGARDE = "donnees_banque.ser";

    private List<CompteBancaire> comptes;

    public GestionnaireBanque() {
        this.comptes = new ArrayList<>();
        // Tente de charger les données existantes au démarrage
        chargerDonnees();
    }

    /**
     * Tente d'ajouter un nouveau compte. Vérifie si le numéro existe déjà.
     * @return true si l'ajout a réussi, false si le numéro est déjà pris.
     */
    public boolean ajouterCompte(int numero, String nom, double soldeInitial) {
        if (trouverCompte(numero) == null) {
            comptes.add(new CompteBancaire(numero, nom, soldeInitial));
            sauvegarderDonnees();
            return true;
        }
        return false;
    }
    
    /**
     * Effectue un transfert de fonds entre deux comptes.
     * @return true si le transfert a réussi, false sinon.
     */
    public boolean transfererFonds(int numSource, int numCible, double montant) {
        CompteBancaire source = trouverCompte(numSource);
        CompteBancaire cible = trouverCompte(numCible);
        
        if (source == null || cible == null || montant <= 0 || numSource == numCible) {
            return false; // Erreur: comptes non trouvés, montant invalide ou même compte
        }
        
        // 1. Tenter le retrait (avec enregistrement du type TRANSFERT_EMIS)
        if (source.retirer(montant, "TRANSFERT_EMIS")) {
            // 2. Effectuer le dépôt sur le compte cible (avec enregistrement du type TRANSFERT_RECU)
            cible.recevoirTransfert(montant); 
            sauvegarderDonnees();
            return true;
        }
        return false; // Retrait impossible (solde insuffisant)
    }


    /**
     * Recherche un compte par son numéro.
     * @param numero Le numéro du compte à trouver.
     * @return L'objet CompteBancaire ou null si non trouvé.
     */
    public CompteBancaire trouverCompte(int numero) {
        for (CompteBancaire compte : comptes) {
            if (compte.getNumeroCompte() == numero) {
                return compte;
            }
        }
        return null;
    }

    /**
     * Supprime un compte.
     * @param compte Le compte à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean supprimerCompte(CompteBancaire compte) {
        boolean removed = comptes.remove(compte);
        if (removed) {
            sauvegarderDonnees();
        }
        return removed;
    }

    // --- Persistance des Données (Backend Local) ---

    /**
     * Sauvegarde la liste des comptes dans un fichier binaire (Sérialisation).
     */
    public void sauvegarderDonnees() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER_SAUVEGARDE))) {
            oos.writeObject(comptes);
            System.out.println("Données sauvegardées localement dans " + FICHIER_SAUVEGARDE);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Charge la liste des comptes à partir du fichier binaire.
     */
    @SuppressWarnings("unchecked")
    public void chargerDonnees() {
        File file = new File(FICHIER_SAUVEGARDE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.comptes = (List<CompteBancaire>) ois.readObject();
                System.out.println("Données chargées depuis " + FICHIER_SAUVEGARDE + " (" + comptes.size() + " comptes)");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erreur lors du chargement des données. Nouveau gestionnaire créé.");
                // Si le fichier est corrompu, on repart d'une liste vide
                this.comptes = new ArrayList<>();
            }
        }
    }

    public List<CompteBancaire> getComptes() {
        return comptes;
    }
}