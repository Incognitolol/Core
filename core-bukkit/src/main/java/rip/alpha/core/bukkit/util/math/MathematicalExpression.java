package rip.alpha.core.bukkit.util.math;

import lombok.Data;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;

@Data
public class MathematicalExpression {

    private final String original;

    public MathematicalExpression(String expressionString) {
        this.original = expressionString;
    }

    public CompiledExpression compileWith(EvaluationEnvironment environment) {
        return Crunch.compileExpression(this.original, environment);
    }

}
