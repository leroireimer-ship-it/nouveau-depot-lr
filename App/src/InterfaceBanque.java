import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Interface utilisateur graphique pour l'application de gestion bancaire (Frontend).
 * Utilise Java Swing pour une présentation claire.
 * Cette version inclut le Transfert de Fonds et l'affichage de l'Historique des Transactions.
 */
public class InterfaceBanque extends JFrame {

    private GestionnaireBanque gestionnaire; // Le contrôleur pour la logique métier
    private DefaultListModel<CompteBancaire> listModel; // Modèle pour afficher les comptes
    private JList<CompteBancaire> listeComptes;
    private JTextArea messageArea; // Pour afficher les résultats d'opération

    public InterfaceBanque() {
        super("Application de Gestion Bancaire (Stockage Local)");
        
        // Initialiser le gestionnaire (charge les données existantes)
        gestionnaire = new GestionnaireBanque();

        // Configuration de la fenêtre principale
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // On gère la fermeture pour la sauvegarde
        this.setSize(950, 650); // Taille légèrement augmentée
        this.setLocationRelativeTo(null); // Centrer la fenêtre
        this.setLayout(new BorderLayout(10, 10)); // Marges et espacements

        // --- 1. Panneau des Comptes (Liste à Gauche) ---
        JPanel panelListe = createListeComptePanel();
        
        // --- 2. Panneau des Opérations (Centre) ---
        JPanel panelOperations = createOperationsPanel();
        
        // --- 3. Zone de Message (Bas) ---
        messageArea = new JTextArea(3, 1);
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createTitledBorder("Messages"));
        messageArea.setForeground(Color.BLUE);

        // Ajout des composants au JFrame
        this.add(panelListe, BorderLayout.WEST);
        this.add(panelOperations, BorderLayout.CENTER);
        this.add(new JScrollPane(messageArea), BorderLayout.SOUTH);

        // --- Gestion de la fermeture (Sauvegarde) ---
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gestionnaire.sauvegarderDonnees();
                JOptionPane.showMessageDialog(null, "Sauvegarde effectuée. Au revoir !");
                dispose();
                System.exit(0);
            }
        });

        this.setVisible(true);
    }
    
    /**
     * Crée le panneau de la liste des comptes avec le bouton d'historique.
     */
    private JPanel createListeComptePanel() {
        JPanel panelListe = new JPanel(new BorderLayout());
        panelListe.setBorder(BorderFactory.createTitledBorder("Comptes Existant"));
        
        listModel = new DefaultListModel<>();
        listeComptes = new JList<>(listModel);
        listeComptes.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        refreshCompteList();
        
        panelListe.add(new JScrollPane(listeComptes), BorderLayout.CENTER);
        
        JButton btnHistorique = new JButton("Voir Historique Compte");
        btnHistorique.addActionListener(e -> {
            CompteBancaire selectedAccount = listeComptes.getSelectedValue();
            if (selectedAccount != null) {
                showTransactionHistoryDialog(selectedAccount);
            } else {
                displayMessage("Veuillez sélectionner un compte pour voir l'historique.", Color.ORANGE);
            }
        });
        
        panelListe.add(btnHistorique, BorderLayout.SOUTH);
        return panelListe;
    }

    /**
     * Crée le panneau contenant les formulaires pour les opérations (Dépôt, Retrait, Création, Transfert).
     */
    private JPanel createOperationsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panneau de Création de Compte ---
        mainPanel.add(createNouveauComptePanel());
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Panneau de Transaction (Dépôt/Retrait) ---
        mainPanel.add(createTransactionPanel());
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Panneau de Transfert ---
        mainPanel.add(createTransferPanel());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // --- Panneau Suppression ---
        mainPanel.add(createSuppressionPanel());

        return mainPanel;
    }

    private JPanel createNouveauComptePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Créer un Nouveau Compte"));

        JTextField fieldNom = new JTextField();
        JTextField fieldNumero = new JTextField();
        JTextField fieldSolde = new JTextField("0.00");
        JButton btnCreer = new JButton("Créer le Compte");
        
        panel.add(new JLabel("Nom du Titulaire:"));
        panel.add(fieldNom);
        panel.add(new JLabel("Numéro de Compte:"));
        panel.add(fieldNumero);
        panel.add(new JLabel("Solde Initial (€):"));
        panel.add(fieldSolde);
        panel.add(new JLabel("")); // Espace vide pour l'alignement
        panel.add(btnCreer);

        btnCreer.addActionListener(e -> {
            try {
                String nom = fieldNom.getText().trim();
                int numero = Integer.parseInt(fieldNumero.getText().trim());
                double solde = Double.parseDouble(fieldSolde.getText().trim());
                
                if (nom.isEmpty() || numero <= 0 || solde < 0) {
                    displayMessage("Erreur: Tous les champs doivent être remplis et valides.", Color.RED);
                    return;
                }

                if (gestionnaire.ajouterCompte(numero, nom, solde)) {
                    displayMessage("Compte N° " + numero + " créé pour " + nom + " avec succès.", Color.BLUE);
                    fieldNom.setText(""); fieldNumero.setText(""); fieldSolde.setText("0.00");
                    refreshCompteList();
                } else {
                    displayMessage("Erreur: Le numéro de compte " + numero + " existe déjà.", Color.RED);
                }

            } catch (NumberFormatException ex) {
                displayMessage("Erreur: Le numéro de compte et le solde doivent être numériques.", Color.RED);
            }
        });

        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Opérations de Dépôt/Retrait"));

        JTextField fieldNumero = new JTextField();
        JTextField fieldMontant = new JTextField();
        JButton btnDepot = new JButton("Déposer");
        JButton btnRetrait = new JButton("Retirer");
        
        panel.add(new JLabel("Numéro de Compte:"));
        panel.add(fieldNumero);
        panel.add(new JLabel(""));
        
        panel.add(new JLabel("Montant (€):"));
        panel.add(fieldMontant);
        panel.add(new JLabel(""));
        
        panel.add(btnDepot);
        panel.add(btnRetrait);
        panel.add(new JLabel(""));

        btnDepot.addActionListener(e -> handleTransaction(fieldNumero, fieldMontant, "DEPOT"));
        btnRetrait.addActionListener(e -> handleTransaction(fieldNumero, fieldMontant, "RETRAIT"));

        return panel;
    }
    
    /**
     * Crée le panneau pour la fonctionnalité de transfert.
     */
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Transfert de Fonds"));

        JTextField fieldSource = new JTextField();
        JTextField fieldCible = new JTextField();
        JTextField fieldMontant = new JTextField();
        JButton btnTransferer = new JButton("Transférer");

        panel.add(new JLabel("Compte Source N°:"));
        panel.add(fieldSource);
        panel.add(new JLabel("Compte Cible N°:"));
        panel.add(fieldCible);
        panel.add(new JLabel("Montant à Transférer (€):"));
        panel.add(fieldMontant);
        panel.add(new JLabel("")); // Espace vide
        panel.add(btnTransferer);

        btnTransferer.addActionListener(e -> handleTransfer(fieldSource, fieldCible, fieldMontant));

        return panel;
    }
    
    private JPanel createSuppressionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Supprimer Compte Sélectionné"));
        
        JButton btnSupprimer = new JButton("Supprimer Compte");
        panel.add(btnSupprimer);

        btnSupprimer.addActionListener(e -> {
            CompteBancaire selectedAccount = listeComptes.getSelectedValue();
            if (selectedAccount == null) {
                displayMessage("Veuillez sélectionner un compte à supprimer dans la liste.", Color.RED);
                return;
            }
            
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer le compte N°" + selectedAccount.getNumeroCompte() + " ?",
                    "Confirmation de Suppression",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                if (gestionnaire.supprimerCompte(selectedAccount)) {
                    displayMessage("Compte N°" + selectedAccount.getNumeroCompte() + " supprimé avec succès.", Color.RED);
                    refreshCompteList();
                } else {
                    displayMessage("Erreur lors de la suppression du compte.", Color.RED);
                }
            }
        });

        return panel;
    }

    /**
     * Logique unifiée pour le dépôt et le retrait.
     */
    private void handleTransaction(JTextField fieldNumero, JTextField fieldMontant, String type) {
        try {
            int numero = Integer.parseInt(fieldNumero.getText().trim());
            double montant = Double.parseDouble(fieldMontant.getText().trim());
            
            if (montant <= 0) {
                displayMessage("Erreur: Le montant doit être positif.", Color.RED);
                return;
            }

            CompteBancaire compte = gestionnaire.trouverCompte(numero);

            if (compte != null) {
                boolean success = false;
                String transactionMessage = "";
                
                if (type.equals("DEPOT")) {
                    compte.deposer(montant);
                    success = true;
                    transactionMessage = "Dépôt de " + montant + " € sur le compte N°" + numero + " effectué.";
                } else if (type.equals("RETRAIT")) {
                    // Utilise la nouvelle signature de retirer avec le type de transaction
                    if (compte.retirer(montant, "RETRAIT")) { 
                        success = true;
                        transactionMessage = "Retrait de " + montant + " € du compte N°" + numero + " effectué.";
                    } else {
                        displayMessage("Erreur: Solde insuffisant pour un retrait de " + montant + " €.", Color.RED);
                    }
                }
                
                if (success) {
                    gestionnaire.sauvegarderDonnees(); // Sauvegarde après chaque transaction
                    refreshCompteList(); // Met à jour la liste pour afficher le nouveau solde
                    
                    // Récupère l'heure réelle de la transaction pour l'affichage du message
                    List<Transaction> historique = compte.getHistoriqueTransactions();
                    String dateHeure = "";
                    if (!historique.isEmpty()) {
                        dateHeure = " (" + historique.get(historique.size() - 1).getDateHeureFormatee() + ")";
                    }
                    displayMessage(transactionMessage + dateHeure, Color.BLUE);
                }
            } else {
                displayMessage("Erreur: Compte N°" + numero + " non trouvé.", Color.RED);
            }
        } catch (NumberFormatException ex) {
            displayMessage("Erreur: Le numéro de compte et le montant doivent être numériques.", Color.RED);
        }
    }

    /**
     * Logique unifiée pour le transfert de fonds.
     */
    private void handleTransfer(JTextField fieldSource, JTextField fieldCible, JTextField fieldMontant) {
        try {
            int numSource = Integer.parseInt(fieldSource.getText().trim());
            int numCible = Integer.parseInt(fieldCible.getText().trim());
            double montant = Double.parseDouble(fieldMontant.getText().trim());

            if (montant <= 0) {
                displayMessage("Erreur: Le montant du transfert doit être positif.", Color.RED);
                return;
            }
            if (numSource == numCible) {
                 displayMessage("Erreur: Les comptes source et cible doivent être différents.", Color.RED);
                return;
            }
            
            // Appel de la méthode de transfert centralisée dans le gestionnaire
            if (gestionnaire.transfererFonds(numSource, numCible, montant)) {
                refreshCompteList();
                
                // Récupère l'heure réelle de la transaction pour l'affichage du message
                CompteBancaire source = gestionnaire.trouverCompte(numSource);
                List<Transaction> historique = source.getHistoriqueTransactions();
                String dateHeure = "";
                if (!historique.isEmpty()) {
                    dateHeure = " (" + historique.get(historique.size() - 1).getDateHeureFormatee() + ")";
                }
                
                displayMessage("Transfert de " + montant + " € de N°" + numSource + " vers N°" + numCible + " réussi." + dateHeure, Color.BLUE);
                fieldSource.setText(""); fieldCible.setText(""); fieldMontant.setText("");
            } else {
                // Le gestionnaire gère déjà si le solde est insuffisant ou si les comptes n'existent pas
                displayMessage("Erreur de transfert : Vérifiez les numéros de compte ou le solde insuffisant sur le compte source.", Color.RED);
            }

        } catch (NumberFormatException ex) {
            displayMessage("Erreur: Les numéros de compte et le montant doivent être numériques.", Color.RED);
        }
    }
    
    /**
     * Affiche une boîte de dialogue avec l'historique des transactions pour un compte.
     */
    private void showTransactionHistoryDialog(CompteBancaire compte) {
        JDialog dialog = new JDialog(this, "Historique du Compte N°" + compte.getNumeroCompte() + " (" + compte.getNomTitulaire() + ")", true);
        dialog.setSize(650, 450);
        dialog.setLayout(new BorderLayout());

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // --- Construction de l'Historique Réel ---
        StringBuilder historyText = new StringBuilder();
        historyText.append(String.format("%-25s %-15s %-15s\n", "Date & Heure", "Type", "Montant"));
        historyText.append("--------------------------------------------------------------------------------\n");
        
        // Afficher les transactions stockées
        List<Transaction> historique = compte.getHistoriqueTransactions();
        
        if (historique.isEmpty()) {
            historyText.append("\nAucune transaction enregistrée pour ce compte.");
        } else {
            for (Transaction transaction : historique) {
                historyText.append(String.format("%-25s %-15s %-15s\n", 
                    transaction.getDateHeureFormatee(), 
                    transaction.getType(), 
                    transaction.getMontantFormate()));
            }
        }
        
        historyArea.setText(historyText.toString());
        
        dialog.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel southPanel = new JPanel();
        southPanel.add(closeButton);
        dialog.add(southPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    /**
     * Met à jour la liste des comptes affichée à gauche.
     */
    private void refreshCompteList() {
        listModel.clear();
        List<CompteBancaire> comptes = gestionnaire.getComptes();
        for (CompteBancaire compte : comptes) {
            listModel.addElement(compte);
        }
    }
    
    /**
     * Affiche un message coloré dans la zone de message.
     */
    private void displayMessage(String message, Color color) {
        messageArea.setForeground(color);
        messageArea.setText(message);
    }

    public static void main(String[] args) {
        // Exécuter l'interface dans le thread de répartition d'événements Swing
        SwingUtilities.invokeLater(InterfaceBanque::new);
    }
}