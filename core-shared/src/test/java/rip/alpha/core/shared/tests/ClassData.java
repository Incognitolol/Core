package rip.alpha.core.shared.tests;

public record ClassData(Class<?> classA, Class<?> classB, Class<?> classC) {
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassData other)) {
            return false;
        }
        return other.classA.equals(this.classA) && other.classB.equals(this.classB) && other.classC.equals(this.classC);
    }
}
