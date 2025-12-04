import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle de données pour un compte bancaire.
 * Implémente Serializable pour permettre la sauvegarde et le chargement des objets (backend local).
 */
public class CompteBancaire implements Serializable {
    // Le numéro de version est important pour la sérialisation
    private static final long serialVersionUID = 1L;

    private int numeroCompte;
    private String nomTitulaire;
    private double solde;
    private List<Transaction> historique; // Nouvelle liste pour l'historique

    /**
     * Constructeur d'un nouveau compte.
     * @param numero Numéro unique du compte.
     * @param nom Nom du titulaire.
     * @param soldeInitial Solde de départ.
     */
    public CompteBancaire(int numero, String nom, double soldeInitial) {
        this.numeroCompte = numero;
        this.nomTitulaire = nom;
        this.solde = soldeInitial;
        this.historique = new ArrayList<>();
        if (soldeInitial > 0) {
            enregistrerTransaction(soldeInitial, "DEPOT_INITIAL");
        }
    }

    /**
     * Ajoute un montant au solde du compte et enregistre la transaction.
     * @param montant Montant à déposer.
     */
    public void deposer(double montant) {
        if (montant > 0) {
            this.solde += montant;
            enregistrerTransaction(montant, "DEPOT");
        }
    }
    
    /**
     * Ajoute un montant au solde du compte suite à un transfert reçu.
     * @param montant Montant à déposer.
     */
    public void recevoirTransfert(double montant) {
        if (montant > 0) {
            this.solde += montant;
            enregistrerTransaction(montant, "TRANSFERT_RECU");
        }
    }

    /**
     * Retire un montant du solde si les fonds sont suffisants et enregistre la transaction.
     * @param montant Montant à retirer.
     * @param typeTransaction Le type d'opération (RETRAIT ou TRANSFERT_EMIS).
     * @return true si le retrait a réussi, false sinon.
     */
    public boolean retirer(double montant, String typeTransaction) {
        if (montant > 0 && this.solde >= montant) {
            this.solde -= montant;
            // Enregistre soit un RETRAIT soit un TRANSFERT_EMIS
            enregistrerTransaction(montant, typeTransaction);
            return true;
        }
        return false;
    }

    /**
     * Méthode interne pour enregistrer une transaction dans l'historique.
     */
    private void enregistrerTransaction(double montant, String type) {
        this.historique.add(new Transaction(montant, type));
    }

    // --- Getters ---

    public int getNumeroCompte() {
        return numeroCompte;
    }

    public String getNomTitulaire() {
        return nomTitulaire;
    }

    public double getSolde() {
        return solde;
    }
    
    public List<Transaction> getHistoriqueTransactions() {
        return historique;
    }

    /**
     * Retourne une chaîne formatée du solde pour l'affichage.
     */
    public String getSoldeFormate() {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(solde) + " €";
    }

    @Override
    public String toString() {
        return "N°: " + numeroCompte + " | Titulaire: " + nomTitulaire + " | Solde: " + getSoldeFormate();
    }
}