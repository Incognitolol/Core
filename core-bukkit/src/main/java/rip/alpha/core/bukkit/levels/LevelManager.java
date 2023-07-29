package rip.alpha.core.bukkit.levels;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import redempt.crunch.CompiledExpression;
import redempt.crunch.functional.EvaluationEnvironment;
import rip.alpha.core.bukkit.CoreConfig;
import rip.alpha.core.bukkit.util.math.MathematicalExpression;

public class LevelManager {

    @Getter
    private static final LevelManager instance = new LevelManager();

    private final Int2ObjectMap<Range<Integer>> lvlToExperienceMap = new Int2ObjectOpenHashMap<>();
    private final RangeMap<Integer, Integer> experienceToLvlMap = TreeRangeMap.create();

    private LevelManager() {
        MathematicalExpression expression = CoreConfig.getInstance().getLevelExpression();
        EvaluationEnvironment environment = new EvaluationEnvironment();
        environment.setVariableNames("lvl");
        CompiledExpression compiledExpression = expression.compileWith(environment);
        for (int lvl = 1; lvl <= 100; lvl++) {
            int prev = lvl == 1 ? 0 : (int) compiledExpression.evaluate(lvl - 1);
            int curr = (int) compiledExpression.evaluate(lvl);
            Range<Integer> range = Range.closedOpen(prev, curr);
            this.lvlToExperienceMap.put(lvl, range);
            this.experienceToLvlMap.put(range, lvl);
        }
    }

    public int getTotalExpToLevel(int lvl) {
        if (lvl <= 0 || lvl > 100) {
            throw new IllegalArgumentException("Dont have information for lvl " + lvl);
        }
        Range<Integer> range = this.lvlToExperienceMap.get(lvl);
        return range.lowerEndpoint();
    }

    public int getLevelOfTotalExp(int exp) {
        Integer lvl = this.experienceToLvlMap.get(exp);
        if (lvl == null) {
            throw new IllegalArgumentException("Could not fetch lvl for exp " + exp);
        }
        return lvl;
    }

}
