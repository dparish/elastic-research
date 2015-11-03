package dparish.model;

/**
 * @author dparish
 */
public class Medicine {

    private String proprietaryName;
    private String nonProprietaryName;
    private String routeName;
    private String labelerName;
    private String substanceName;
    private String activeIngredientUnit;

    public String getProprietaryName() {
        return proprietaryName;
    }

    public void setProprietaryName(String proprietaryName) {
        this.proprietaryName = proprietaryName;
    }

    public String getNonProprietaryName() {
        return nonProprietaryName;
    }

    public void setNonProprietaryName(String nonProprietaryName) {
        this.nonProprietaryName = nonProprietaryName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getLabelerName() {
        return labelerName;
    }

    public void setLabelerName(String labelerName) {
        this.labelerName = labelerName;
    }

    public String getSubstanceName() {
        return substanceName;
    }

    public void setSubstanceName(String substanceName) {
        this.substanceName = substanceName;
    }

    public String getActiveIngredientUnit() {
        return activeIngredientUnit;
    }

    public void setActiveIngredientUnit(String activeIngredientUnit) {
        this.activeIngredientUnit = activeIngredientUnit;
    }
}
