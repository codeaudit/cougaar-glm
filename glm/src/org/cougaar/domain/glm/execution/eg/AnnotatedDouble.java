package org.cougaar.domain.glm.execution.eg;

public class AnnotatedDouble {
    public double value;
    public Object annotation;
    public AnnotatedDouble(double value) {
        this.value = value;
        this.annotation = null;
    }
    public AnnotatedDouble(double value, Object annotation) {
        this.value = value;
        this.annotation = annotation;
    }
}
