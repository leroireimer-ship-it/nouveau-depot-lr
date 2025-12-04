import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

/**
 * Modèle de données pour une transaction bancaire spécifique.
 * Utilisé pour l'historique du compte.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 3L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private double montant;
    private String type; // Ex: DEPOT, RETRAIT, TRANSFERT_EMIS, TRANSFERT_RECU
    private LocalDateTime dateHeure;

    public Transaction(double montant, String type) {
        this.montant = montant;
        this.type = type;
        this.dateHeure = LocalDateTime.now(); // Enregistre l'heure actuelle
    }

    // --- Getters ---

    public double getMontant() {
        return montant;
    }

    public String getType() {
        return type;
    }

    public String getDateHeureFormatee() {
        return dateHeure.format(DATE_FORMATTER);
    }

    /**
     * Retourne le montant avec le signe (+ ou -) et le format €.
     */
    public String getMontantFormate() {
        DecimalFormat df = new DecimalFormat("#.00");
        String prefix = "";
        // Ajoute un signe pour l'affichage de l'historique
        if (type.equals("DEPOT") || type.equals("TRANSFERT_RECU") || type.equals("DEPOT_INITIAL")) {
            prefix = "+ ";
        } else if (type.equals("RETRAIT") || type.equals("TRANSFERT_EMIS")) {
            prefix = "- ";
        }
        return prefix + df.format(montant) + " €";
    }

    @Override
    public String toString() {
        return "[" + getDateHeureFormatee() + "] " + getType() + " : " + getMontantFormate();
    }
}