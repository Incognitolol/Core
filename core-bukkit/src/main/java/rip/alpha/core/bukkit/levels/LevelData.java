package rip.alpha.core.bukkit.levels;

import lombok.Getter;

public class LevelData {

    @Getter
    private int experience;

    public void addExperience(int experience) {
        this.experience += experience;
        this.checkBounds();
    }

    public void removeExperience(int experience) {
        this.experience -= experience;
        this.checkBounds();
    }

    public int getLevel() {
        return LevelManager.getInstance().getLevelOfTotalExp(this.experience);
    }

    private void checkBounds() {
        if (this.experience < 0) {
            this.experience = 0;
        }
    }

}
