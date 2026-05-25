package ma.atos.billing.invoice.billing_invoice.dtos;

import ma.atos.billing.invoice.billing_invoice.enums.TypeCreancier;

public class CreancierSearchCriteria {

    private String nom;
    private TypeCreancier typeCreancier;
    private String ice;
    private String rc;
    private String rib;
    private String banque;
    private String email;
    private String telephone;
    private String adresse;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeCreancier getTypeCreancier() {
        return typeCreancier;
    }

    public void setTypeCreancier(TypeCreancier typeCreancier) {
        this.typeCreancier = typeCreancier;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getBanque() {
        return banque;
    }

    public void setBanque(String banque) {
        this.banque = banque;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
